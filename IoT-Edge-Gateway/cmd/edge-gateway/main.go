/*
[파일 목적]
상용 배포 수준의 운영 가시성과 로그 최적화가 적용된 최종 메인 로직입니다.

[최종 고도화 사항]
1. 로그 억제(Log Suppression): 동일 에러 반복 시 로그 생략, 상태 변화 시에만 기록하여 로그 폭주 방지.
2. 상세 헬스체크: /health 호출 시 업타임, 고루틴 수, 장비별 실시간 상태를 JSON으로 제공.
3. 데이터 유효성 검사: 비정상 범위 데이터(-999 등)에 대한 필터링 기반 마련.
*/

package main

import (
	"context"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"net"
	"net/http"
	"net/url"
	"os"
	"os/signal"
	"runtime"
	"strings"
	"sync"
	"syscall"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/fsnotify/fsnotify"
	"github.com/goburrow/modbus"

	"mes-iot-edge/internal/config"
	"mes-iot-edge/internal/logger"
	"mes-iot-edge/internal/storage"
)

// 운영 상태와 헬스체크 응답에 필요한 전역 상태를 보관합니다.
var (
	startTime    time.Time
	deviceStatus sync.Map // 장비별 실시간 상태 저장 (thread-safe)
)

// main은 게이트웨이의 전체 라이프사이클을 관리합니다.
// 목적: 설정 로드, 수집/전송, 복구, 헬스체크를 안전하게 시작하고 종료합니다.
func main() {
	startTime = time.Now()
	setupDirectories()
	logger.InitLogger()
	if logger.Log != nil {
		defer func() { _ = logger.Log.Sync() }()
	}

	logger.Log.Info("🚀 MES IoT Edge Gateway (Production Mode) 가동")

	// 종료 신호를 받아 워커를 정상 종료시키기 위한 컨텍스트입니다.
	mainCtx, mainCancel := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer mainCancel()

	// 장애 복구(Store & Forward)용 로컬 DB를 초기화합니다.
	db, err := storage.InitDB("gateway_local.db")
	if err != nil {
		logger.Log.Fatalf("❌ DB 연결 실패: %v", err)
	}
	defer storage.CloseDB(db)

	// 설정 파일 변경을 감지하여 Hot Reload를 수행합니다.
	watcher, _ := fsnotify.NewWatcher()
	defer watcher.Close()

	var currentCancel context.CancelFunc
	var globalClient mqtt.Client

	// runCollectors는 설정 변경 시 워커를 재기동합니다.
	// 이유: 변경된 설정을 반영하되 기존 워커를 안전하게 종료하기 위함입니다.
	runCollectors := func() {
		if currentCancel != nil {
			logger.Log.Info("🔄 설정 변경 감지: 시스템 리로드 중...")
			currentCancel()
			time.Sleep(1 * time.Second)
		}

		var reloadCtx context.Context
		reloadCtx, currentCancel = context.WithCancel(mainCtx)

		// 설정 파일을 다시 읽어 장비/센서 구성을 갱신합니다.
		cfg, err := config.LoadConfig("config/config.json")
		if err != nil {
			logger.Log.Errorf("❌ 설정 로드 실패: %v", err)
			return
		}

		// MQTT 연결은 공용으로 사용하므로 재사용합니다.
		if globalClient == nil || !globalClient.IsConnected() {
			opts := mqtt.NewClientOptions().AddBroker(cfg.MQTT.Broker)
			globalClient = mqtt.NewClient(opts)
			_ = globalClient.Connect()
		}

		// 장비별 수집 워커를 분리하여 장애 영향 범위를 최소화합니다.
		for _, device := range cfg.Devices {
			go startCollector(reloadCtx, cfg, device, globalClient, db)
		}
		// 전송 실패 데이터 복구 루프를 함께 기동합니다.
		go retryUnsentData(reloadCtx, globalClient, db, cfg.MQTT.Topic)

		logger.Log.Infof("✅ 운영 엔진 가동 완료 (장치: %d대)", len(cfg.Devices))
	}

	runCollectors()
	_ = watcher.Add("config/config.json")

	// 운영 상태를 외부에서 확인할 수 있도록 헬스체크 서버를 기동합니다.
	initialCfg, _ := config.LoadConfig("config/config.json")
	go startHealthServer(mainCtx, initialCfg.Ports.HealthCheck)

	// 이벤트 루프: 설정 변경 또는 종료 신호를 처리합니다.
	for {
		select {
		case event, _ := <-watcher.Events:
			if event.Has(fsnotify.Write) {
				runCollectors()
			}
		case <-mainCtx.Done():
			if currentCancel != nil {
				currentCancel()
			}
			if globalClient != nil {
				globalClient.Disconnect(250)
			}
			return
		}
	}
}

// startCollector는 단일 장비에 대해 센서 워커들을 초기화합니다.
// 목적: 장비별 연결/타임아웃을 분리해 장애 전파를 최소화합니다.
func startCollector(ctx context.Context, cfg *config.Config, dev config.DeviceConfig, mqttClient mqtt.Client, db *sql.DB) {
	deviceStatus.Store(dev.Name, "Connecting") // 초기 상태 기록

	var handler modbus.ClientHandler
	if dev.Type == "modbus_rtu" {
		h := modbus.NewRTUClientHandler(dev.Address)
		h.BaudRate = 9600
		h.SlaveId = byte(dev.SlaveID)
		h.Timeout = time.Duration(dev.Timeout) * time.Second
		if err := h.Connect(); err != nil {
			deviceStatus.Store(dev.Name, fmt.Sprintf("Error: %v", err))
		} else {
			deviceStatus.Store(dev.Name, "Connected")
		}
		defer h.Close()
		handler = h
	} else {
		h := modbus.NewTCPClientHandler(dev.Address)
		h.SlaveId = byte(dev.SlaveID)
		h.Timeout = time.Duration(dev.Timeout) * time.Second
		if err := h.Connect(); err != nil {
			deviceStatus.Store(dev.Name, fmt.Sprintf("Error: %v", err))
		} else {
			deviceStatus.Store(dev.Name, "Connected")
		}
		defer h.Close()
		handler = h
	}

	mbClient := modbus.NewClient(handler)
	var mu sync.Mutex

	// 센서별 주기(Interval)를 적용하여 독립 스케줄링을 보장합니다.
	for _, s := range dev.Sensors {
		interval := s.Interval
		if interval <= 0 {
			interval = dev.ScanInterval
		}
		if interval <= 0 {
			interval = 5
		}
		go sensorWorker(ctx, &mu, dev.Name, s, mbClient, mqttClient, db, cfg, interval)
	}

	<-ctx.Done()
}

// sensorWorker는 단일 센서의 수집/전송 루프를 담당합니다.
// 목적: 센서별 주기를 지키면서 오류를 개별적으로 격리합니다.
func sensorWorker(ctx context.Context, mu *sync.Mutex, devName string, s config.SensorConfig, mb modbus.Client, mqtt mqtt.Client, db *sql.DB, cfg *config.Config, interval int) {
	ticker := time.NewTicker(time.Duration(interval) * time.Second)
	defer ticker.Stop()

	var lastErrMsg string // [로그 억제용] 직전 에러 메시지 저장

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			mu.Lock()
			results, err := mb.ReadHoldingRegisters(s.Address, 1)
			mu.Unlock()

			if err != nil {
				currentErrMsg := err.Error()
				// [로그 억제] 이전과 동일한 에러라면 로그를 남기지 않음
				if currentErrMsg != lastErrMsg {
					logger.Log.Errorf("❌ [%s:%s] 수집 실패: %v", devName, s.Name, err)
					lastErrMsg = currentErrMsg
					deviceStatus.Store(devName, "Error: "+currentErrMsg)
				}
				continue
			}

			// 성공 시 에러 상태 초기화 및 로그 출력
			if lastErrMsg != "" {
				logger.Log.Infof("✅ [%s:%s] 연결 복구됨", devName, s.Name)
				lastErrMsg = ""
				deviceStatus.Store(devName, "Running")
			}

			rawVal := uint16(results[0])<<8 | uint16(results[1])
			scale := s.Scale
			if scale == 0 {
				scale = 1.0
			}
			scaledVal := float64(rawVal) * scale

			// [데이터 유효성 검사 예시]
			// if scaledVal < -50 || scaledVal > 150 { continue }

			// 전송 실패 시 DB에 저장하여 데이터 유실을 방지합니다.
			publishData(cfg, mqtt, devName, []map[string]any{{"name": s.Name, "val": scaledVal}}, db)
		}
	}
}

// startHealthServer는 운영 상태를 외부에 제공하는 헬스체크 서버입니다.
// 목적: 모니터링 시스템이 상태와 리소스를 쉽게 확인하도록 합니다.
func startHealthServer(ctx context.Context, port int) {
	mux := http.NewServeMux()
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")

		// 장비 상태 수집
		statuses := make(map[string]any)
		deviceStatus.Range(func(key, value any) bool {
			statuses[key.(string)] = value
			return true
		})

		resp := map[string]any{
			"status":        "UP",
			"uptime":        time.Since(startTime).String(),
			"goroutines":    runtime.NumGoroutine(),
			"device_status": statuses,
			"mqtt_broker":   "Check Logs",
		}
		json.NewEncoder(w).Encode(resp)
	})

	srv := &http.Server{Addr: fmt.Sprintf(":%d", port), Handler: mux}
	go func() { <-ctx.Done(); _ = srv.Shutdown(context.Background()) }()
	_ = srv.ListenAndServe()
}

// publishData는 MQTT 전송과 장애 시 로컬 저장(Store & Forward)을 책임집니다.
// 목적: 네트워크 장애에서도 데이터 유실 없이 복구 가능하도록 합니다.
func publishData(cfg *config.Config, client mqtt.Client, devName string, sensors []map[string]any, db *sql.DB) {
	payload := map[string]any{"version": "1.5", "device": devName, "timestamp": time.Now().Format(time.RFC3339), "sensors": sensors}
	js, _ := json.Marshal(payload)
	if !brokerReachable(cfg.MQTT.Broker) {
		for _, s := range sensors {
			storage.SaveSensorData(db, devName, s["name"].(string), s["val"].(float64))
		}
		return
	}
	if client.IsConnected() {
		token := client.Publish(cfg.MQTT.Topic, 0, false, js)
		if token.Wait() && token.Error() == nil {
			logger.Log.Infof("📥 [%s] 전송 완료", devName)
			return
		}
	}
	for _, s := range sensors {
		storage.SaveSensorData(db, devName, s["name"].(string), s["val"].(float64))
	}
}

// retryUnsentData는 DB에 저장된 데이터를 복구 전송합니다.
// 목적: 전송 실패 데이터를 FIFO 순서로 재전송하여 정합성을 보장합니다.
func retryUnsentData(ctx context.Context, client mqtt.Client, db *sql.DB, topic string) {
	ticker := time.NewTicker(10 * time.Second)
	defer ticker.Stop()
	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			// 연결이 끊긴 경우 주기적으로 재연결을 시도합니다.
			if !client.IsConnected() {
				token := client.Connect()
				if !token.WaitTimeout(2*time.Second) || token.Error() != nil {
					continue
				}
			}
			for client.IsConnected() {
				data, err := storage.GetOldestData(db)
				if err != nil {
					if errors.Is(err, sql.ErrNoRows) {
						// 전송할 데이터가 없으면 다음 주기까지 대기합니다.
						break
					}
					logger.Log.Errorf("DB 조회 오류: %v", err)
					break
				}
				payload := map[string]any{
					"version":      "1.5",
					"device":       data.DeviceName,
					"timestamp":    data.Timestamp,
					"is_recovered": true,
					"sensors":      []map[string]any{{"name": data.SensorName, "val": data.Value}},
				}
				js, _ := json.Marshal(payload)
				if token := client.Publish(topic, 0, false, js); token.Wait() && token.Error() == nil {
					storage.DeleteData(db, data.ID)
					logger.Log.Infof("🔁 복구 전송 완료: device=%s sensor=%s", data.DeviceName, data.SensorName)
					time.Sleep(100 * time.Millisecond)
				} else {
					break
				}
			}
		}
	}
}

// setupDirectories는 로그/격리 디렉터리를 보장합니다.
// 목적: 초기 실행 시 파일 경로 누락으로 인한 오류를 방지합니다.
func setupDirectories() {
	for _, dir := range []string{"logs", "quarantine"} {
		if _, err := os.Stat(dir); os.IsNotExist(err) {
			_ = os.MkdirAll(dir, 0755)
		}
	}
}

// brokerReachable는 브로커의 TCP 포트가 열려 있는지 확인합니다.
// 목적: 브로커 단절 시 로컬 저장으로 전환하기 위함입니다.
func brokerReachable(broker string) bool {
	addr := broker
	if parsed, err := url.Parse(broker); err == nil && parsed.Host != "" {
		addr = parsed.Host
	} else if parsed != nil && parsed.Host == "" && parsed.Path != "" && parsed.Scheme != "" {
		addr = parsed.Path
	}

	if !strings.Contains(addr, ":") {
		addr = net.JoinHostPort(addr, "1883")
	}

	conn, err := net.DialTimeout("tcp", addr, 2*time.Second)
	if err != nil {
		return false
	}
	_ = conn.Close()
	return true
}
