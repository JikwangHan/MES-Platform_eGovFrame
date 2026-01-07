// 파일 목적: Edge Gateway 메인 실행 로직
// 기능: 설정된 센서 목록을 순회하며 Modbus 데이터를 수집하고 MQTT로 표준 규격 전송
// 유지보수: 수집 로직이나 전송 규격 변경 시 이 파일의 루프와 publishData 함수를 수정함
package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/goburrow/modbus"

	cfgpkg "mes-iot-edge/internal/config"
)

func main() {
	fmt.Println("🚀 MES IoT Edge Gateway (Go version) Starting...")

	setupDirectories()

	cfg, err := cfgpkg.LoadConfig("config/config.json")
	if err != nil {
		log.Fatalf("❌ 설정 로드 실패: %v", err)
	}

	// Graceful Shutdown 준비 (Ctrl+C 신호 감지)
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	// MQTT 클라이언트 초기화
	opts := mqtt.NewClientOptions().AddBroker(cfg.MQTT.Broker)
	if cfg.MQTT.ClientID != "" {
		opts.SetClientID(cfg.MQTT.ClientID)
	}
	client := mqtt.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		log.Printf("❌ MQTT 연결 실패: %v", token.Error())
	}

	// Modbus 핸들러 설정
	handler := modbus.NewTCPClientHandler(cfg.Modbus.Address)
	handler.SlaveId = byte(cfg.Modbus.SlaveID)
	handler.Timeout = time.Duration(cfg.Modbus.Timeout) * time.Second
	if err := handler.Connect(); err != nil {
		log.Fatalf("❌ Modbus 연결 실패: %v", err)
	}
	defer handler.Close()
	mbClient := modbus.NewClient(handler)

	fmt.Println("📡 데이터 수집 루프 시작...")

loop:
	for {
		select {
		case <-ctx.Done():
			fmt.Println("🛑 종료 신호 수신, 안전 종료를 시작합니다...")
			break loop
		default:
			// 1. 설정된 센서 목록을 순회하며 데이터 읽기 (하드코딩 제거)
			var sensorData []map[string]any
			for _, s := range cfg.Modbus.Sensors {
				// 각 센서의 주소에서 1개의 레지스터(16비트) 데이터를 읽어옴
				results, err := mbClient.ReadHoldingRegisters(s.Address, 1)
				if err != nil {
					log.Printf("⚠️ 센서 %s(주소 %d) 읽기 오류: %v", s.Name, s.Address, err)
					saveToQuarantine(fmt.Sprintf("sensor_%s_read_err: %v", s.Name, err))
					continue
				}

				// 읽어온 바이트(2bytes)를 uint16 숫자로 변환
				val := uint16(results[0])<<8 | uint16(results[1])
				sensorData = append(sensorData, map[string]any{
					"name": s.Name,
					"val":  val,
				})
			}

			// 2. 수집된 데이터가 있다면 표준 규격(Envelope)으로 전송
			if len(sensorData) > 0 {
				if err := publishData(cfg, client, sensorData); err != nil {
					log.Printf("⚠️ 데이터 전송 오류: %v", err)
					saveToQuarantine(err.Error())
				}
			}

			time.Sleep(5 * time.Second) // 5초 대기
		}
	}

	// 리소스 안전 정리
	client.Disconnect(250)
	fmt.Println("✅ Edge Gateway 종료 완료")
}

// publishData는 수집된 데이터를 표준 Envelope JSON 형식으로 변환하여 전송합니다.
func publishData(cfg *cfgpkg.Config, client mqtt.Client, sensors []map[string]any) error {
	payload := map[string]any{
		"version":     "1.0",
		"gateway_id":  cfg.GatewayID,
		"device_info": cfg.DeviceName,
		"timestamp":   time.Now().Format(time.RFC3339),
		"sensors":     sensors,
	}

	js, err := json.Marshal(payload)
	if err != nil {
		return err
	}

	fmt.Printf("📥 전송 데이터: %s\n", string(js))
	token := client.Publish(cfg.MQTT.Topic, 0, false, js)
	token.Wait()
	return token.Error()
}

func setupDirectories() {
	for _, dir := range []string{"logs", "quarantine"} {
		if _, err := os.Stat(dir); os.IsNotExist(err) {
			os.Mkdir(dir, 0755)
		}
	}
}

func saveToQuarantine(reason string) {
	ts := time.Now().Format("20060102_150405")
	_ = os.WriteFile(fmt.Sprintf("quarantine/fail_%s.log", ts), []byte(reason), 0644)
}
