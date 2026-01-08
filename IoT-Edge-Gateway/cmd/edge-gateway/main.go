// 파일 목적: Edge Gateway의 결함 허용(Fault-Tolerance) 기능을 강화한 메인 프로그램
// 주요 기능:
// 1. MQTT 자동 재연결: 네트워크가 끊겨도 브로커 서버가 복구되면 자동으로 다시 연결함
// 2. 헬스체크 API 서버: 18080 포트를 통해 시스템이 정상 작동 중인지 외부(L4/L7 스위치 등)에 알림
// 3. 센서 데이터 수집: 설정 파일(config.json)에 정의된 센서 목록을 순회하며 데이터 수집
// 4. 안전한 종료: 프로그램 종료 시 사용 중인 통신 자원을 깨끗하게 정리

package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/goburrow/modbus"

	cfgpkg "mes-iot-edge/internal/config"
)

func main() {
	fmt.Println("🚀 MES IoT Edge Gateway (Fault-Tolerance) 버전 시작 중...")

	// 1. 필수 폴더(로그, 격리폴더) 준비
	setupDirectories()

	// 2. 외부 설정 파일(config.json) 로드
	cfg, err := cfgpkg.LoadConfig("config/config.json")
	if err != nil {
		log.Fatalf("❌ 설정 파일 읽기 실패: %v", err)
	}

	// 3. [기능 추가] 헬스체크 API 서버 실행 (고루틴 사용)
	// 메인 로직과 별개로 18080 포트에서 "나 살아있어요"라고 응답하는 서버를 띄웁니다.
	go startHealthServer(cfg.Ports.HealthCheck)

	// 4. 안전한 종료(Graceful Shutdown)를 위한 신호 감지 설정
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	// 5. [기능 고도화] MQTT 클라이언트 설정 및 자동 재연결 활성화
	opts := mqtt.NewClientOptions().AddBroker(cfg.MQTT.Broker)
	opts.SetAutoReconnect(true)                   // 네트워크가 끊기면 자동으로 다시 연결 시도
	opts.SetMaxReconnectInterval(1 * time.Minute) // 재연결 시도 간격을 최대 1분으로 설정
	opts.SetConnectRetry(true)                    // 시작 시 서버가 꺼져 있어도 포기하지 않고 계속 시도

	if cfg.MQTT.ClientID != "" {
		opts.SetClientID(cfg.MQTT.ClientID)
	}

	client := mqtt.NewClient(opts)
	// 초기 연결 시도 (실패해도 자동 재연결 설정 덕분에 프로그램은 계속 돌아갑니다)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		log.Printf("⚠️ MQTT 초기 연결 대기 중: %v", token.Error())
	} else {
		fmt.Printf("✅ MQTT 브로커 연결 성공: %s\n", cfg.MQTT.Broker)
	}

	// 6. Modbus 통신 설정
	handler := modbus.NewTCPClientHandler(cfg.Modbus.Address)
	handler.SlaveId = byte(cfg.Modbus.SlaveID)
	handler.Timeout = time.Duration(cfg.Modbus.Timeout) * time.Second

	// Modbus 서버 연결
	if err := handler.Connect(); err != nil {
		log.Printf("⚠️ Modbus 초기 연결 실패(서버를 확인하세요): %v", err)
		// 연결 실패 시 격리 폴더에 기록하지만, 재시도를 위해 종료하지는 않습니다.
		saveToQuarantine("modbus_initial_connect_fail")
	}
	defer handler.Close()
	mbClient := modbus.NewClient(handler)

	fmt.Println("📡 데이터 수집 루프 시작... (종료하시려면 Ctrl+C를 누르세요)")

loop:
	for {
		select {
		case <-ctx.Done():
			// 사용자가 Ctrl+C를 누르면 이쪽으로 들어옵니다.
			fmt.Println("🛑 종료 신호를 받았습니다. 안전하게 종료합니다...")
			break loop
		default:
			// 7. 설정된 센서 목록을 하나씩 순회하며 데이터를 읽어옵니다.
			var sensorResults []map[string]any
			for _, sensor := range cfg.Modbus.Sensors {
				// 각 센서의 주소에서 데이터 1개(16비트)를 읽음
				results, err := mbClient.ReadHoldingRegisters(sensor.Address, 1)
				if err != nil {
					log.Printf("⚠️ 센서 [%s] 읽기 실패: %v", sensor.Name, err)
					saveToQuarantine(fmt.Sprintf("read_fail_%s", sensor.Name))
					continue
				}

				// 읽어온 데이터(Byte)를 숫자(uint16)로 변환
				value := uint16(results[0])<<8 | uint16(results[1])
				sensorResults = append(sensorResults, map[string]any{
					"name": sensor.Name,
					"val":  value,
				})
			}

			// 수집된 데이터가 있으면 MQTT로 전송합니다.
			if len(sensorResults) > 0 {
				publishData(cfg, client, sensorResults)
			}

			// 수집 주기 (5초) 대기
			time.Sleep(5 * time.Second)
		}
	}

	// 8. 종료 전 자원 정리
	fmt.Println("🔻 MQTT 연결 해제 및 리소스 정리 중...")
	client.Disconnect(250)
	fmt.Println("✅ Edge Gateway가 안전하게 종료되었습니다.")
}

// startHealthServer는 외부 모니터링 시스템을 위해 헬스체크 API를 제공합니다.
func startHealthServer(port int) {
	if port == 0 {
		port = 18080 // 설정값이 없으면 기본값 사용
	}

	// /health 경로로 접속하면 현재 상태를 JSON으로 반환합니다.
	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		fmt.Fprintf(w, `{"status": "UP", "time": "%s"}`, time.Now().Format(time.RFC3339))
	})

	fmt.Printf("🏥 헬스체크 서버 가동 중: http://localhost:%d/health\n", port)
	if err := http.ListenAndServe(fmt.Sprintf(":%d", port), nil); err != nil {
		log.Printf("❌ 헬스체크 서버 시작 실패: %v", err)
	}
}

// publishData는 데이터를 전송 규격(Envelope)에 맞춰 MQTT로 보냅니다.
func publishData(cfg *cfgpkg.Config, client mqtt.Client, sensors []map[string]any) {
	payload := map[string]any{
		"version":     "1.1", // fault-tolerance 기능 반영 버전
		"gateway_id":  cfg.GatewayID,
		"device_info": cfg.DeviceName,
		"timestamp":   time.Now().Format(time.RFC3339),
		"sensors":     sensors,
	}

	js, _ := json.Marshal(payload)

	// MQTT가 연결된 상태인지 확인 후 전송
	if client.IsConnected() {
		token := client.Publish(cfg.MQTT.Topic, 0, false, js)
		token.Wait()
		fmt.Printf("📥 데이터 전송 성공: %s\n", string(js))
	} else {
		log.Println("⚠️ MQTT 미연결 상태: 연결이 복구될 때까지 전송을 대기합니다.")
		saveToQuarantine("mqtt_not_connected")
	}
}

// setupDirectories는 필요한 작업 폴더를 생성합니다.
func setupDirectories() {
	for _, dir := range []string{"logs", "quarantine"} {
		if _, err := os.Stat(dir); os.IsNotExist(err) {
			_ = os.Mkdir(dir, 0755)
		}
	}
}

// saveToQuarantine은 오류 로그를 파일로 남겨 나중에 원인을 분석할 수 있게 합니다.
func saveToQuarantine(reason string) {
	timestamp := time.Now().Format("20060102_150405")
	filename := fmt.Sprintf("quarantine/fail_%s.log", timestamp)
	_ = os.WriteFile(filename, []byte(reason), 0644)
}
