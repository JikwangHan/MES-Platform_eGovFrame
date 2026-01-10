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
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"runtime"
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

// 운영 상태 관리를 위한 전역 변수
var (
	startTime    time.Time
	deviceStatus sync.Map // 장비별 실시간 상태 저장 (thread-safe)
)

func main() {
	startTime = time.Now()
	setupDirectories()
	logger.InitLogger()
	defer func() { _ = logger.Log.Sync() }()

	logger.Log.Info("🚀 MES IoT Edge Gateway (Production Mode) 가동")

	mainCtx, mainCancel := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer mainCancel()

	db, err := storage.InitDB("gateway_local.db")
	if err != nil {
		logger.Log.Fatalf("❌ DB 연결 실패: %v", err)
	}
	defer storage.CloseDB(db)

	watcher, _ := fsnotify.NewWatcher()
	defer watcher.Close()

	var currentCancel context.CancelFunc
	var globalClient mqtt.Client

	runCollectors := func() {
		if currentCancel != nil {
			logger.Log.Info("🔄 설정 변경 감지: 시스템 리로드 중...")
			currentCancel()
			time.Sleep(1 * time.Second)
		}

		var reloadCtx context.Context
		reloadCtx, currentCancel = context.WithCancel(mainCtx)

		cfg, err := config.LoadConfig("config/config.json")
		if err != nil {
			logger.Log.Errorf("❌ 설정 로드 실패: %v", err)
			return
		}

		if globalClient == nil || !globalClient.IsConnected() {
			opts := mqtt.NewClientOptions().AddBroker(cfg.MQTT.Broker)
			globalClient = mqtt.NewClient(opts)
			_ = globalClient.Connect()
		}

		for _, device := range cfg.Devices {
			go startCollector(reloadCtx, cfg, device, globalClient, db)
		}
		go retryUnsentData(reloadCtx, globalClient, db, cfg.MQTT.Topic)

		logger.Log.Infof("✅ 운영 엔진 가동 완료 (장치: %d대)", len(cfg.Devices))
	}

	runCollectors()
	_ = watcher.Add("config/config.json")

	initialCfg, _ := config.LoadConfig("config/config.json")
	go startHealthServer(mainCtx, initialCfg.Ports.HealthCheck)

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

			publishData(cfg, mqtt, devName, []map[string]any{{"name": s.Name, "val": scaledVal}}, db)
		}
	}
}

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

// 나머지 함수(publishData, retryUnsentData, setupDirectories)는 이전과 동일
func publishData(cfg *config.Config, client mqtt.Client, devName string, sensors []map[string]any, db *sql.DB) {
	payload := map[string]any{"version": "1.5", "device": devName, "timestamp": time.Now().Format(time.RFC3339), "sensors": sensors}
	js, _ := json.Marshal(payload)
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

func retryUnsentData(ctx context.Context, client mqtt.Client, db *sql.DB, topic string) {
	ticker := time.NewTicker(10 * time.Second)
	defer ticker.Stop()
	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			for client.IsConnected() {
				data, err := storage.GetOldestData(db)
				if err != nil {
					break
				}
				payload := map[string]any{"device": data.DeviceName, "sensor_name": data.SensorName, "val": data.Value, "timestamp": data.Timestamp, "is_recovered": true}
				js, _ := json.Marshal(payload)
				if token := client.Publish(topic, 0, false, js); token.Wait() && token.Error() == nil {
					storage.DeleteData(db, data.ID)
					time.Sleep(100 * time.Millisecond)
				} else {
					break
				}
			}
		}
	}
}

func setupDirectories() {
	for _, dir := range []string{"logs", "quarantine"} {
		if _, err := os.Stat(dir); os.IsNotExist(err) {
			_ = os.Mkdir(dir, 0755)
		}
	}
}
