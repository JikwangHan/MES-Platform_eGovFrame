// 파일 목적: Edge Gateway의 설정 정보를 담는 구조체 정의
// 기능: config.json 파일의 내용을 Go 언어의 객체로 변환하여 프로그램 전체에서 사용함
// 유지보수: 새로운 설정 항목이 추가될 경우 이 파일의 구조체에 필드를 추가해야 함
package config

import (
	"encoding/json"
	"os"
)

type Config struct {
	GatewayID  string       `json:"gateway_id"`
	DeviceName string       `json:"device_name"`
	Modbus     ModbusConfig `json:"modbus"`
	MQTT       MQTTConfig   `json:"mqtt"`
	Ports      PortsConfig  `json:"ports"`
}

type ModbusConfig struct {
	Address string         `json:"address"`
	SlaveID int            `json:"slave_id"`
	Timeout int            `json:"timeout"`
	Sensors []SensorConfig `json:"sensors"` // 추가: 센서 목록을 담는 슬라이스
}

// SensorConfig는 개별 센서의 이름과 Modbus 주소 정보를 담습니다.
type SensorConfig struct {
	Name    string `json:"name"`    // 센서 이름 (예: Temperature)
	Address uint16 `json:"address"` // Modbus 레지스터 주소
}

type MQTTConfig struct {
	Broker   string `json:"broker"`
	Topic    string `json:"topic"`
	ClientID string `json:"client_id"`
}

type PortsConfig struct {
	HealthCheck int `json:"health_check"`
}

// LoadConfig는 설정 파일을 읽어 메모리에 로드하는 핵심 함수입니다.
func LoadConfig(path string) (*Config, error) {
	f, err := os.Open(path)
	if err != nil {
		return nil, err
	}
	defer f.Close()

	var cfg Config
	dec := json.NewDecoder(f)
	if err := dec.Decode(&cfg); err != nil {
		return nil, err
	}
	return &cfg, nil
}
