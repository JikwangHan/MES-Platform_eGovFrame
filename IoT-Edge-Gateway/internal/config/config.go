/*
[파일 목적]
멀티 티커(센서별 개별 주기) 설정을 지원하는 고도화된 설정 관리 모듈입니다.

[주요 수정 사항]
1. 필드 추가: DeviceConfig에 ScanInterval, SensorConfig에 Interval 필드 반영.
2. 패닉 방지: flag.String 정의를 전역 변수로 이동하여 Hot Reload 시 중복 정의 에러 해결.
*/

package config

import (
	"encoding/json"
	"flag"
	"fmt"
	"os"
)

// Hot Reload 시 flag redefined 패닉을 방지하기 위해 전역 변수로 선언합니다.
var (
	brokerFlag = flag.String("mqtt-broker", "", "MQTT 브로커 주소 (예: tcp://localhost:1883)")
)

type Config struct {
	GatewayID  string         `json:"gateway_id"`
	DeviceName string         `json:"device_name"`
	Devices    []DeviceConfig `json:"devices"`
	MQTT       MQTTConfig     `json:"mqtt"`
	Ports      PortsConfig    `json:"ports"`
	LoadedFrom string
}

type DeviceConfig struct {
	Name         string         `json:"name"`
	Type         string         `json:"type"`
	Address      string         `json:"address"`
	SlaveID      int            `json:"slave_id"`
	Timeout      int            `json:"timeout"`
	ScanInterval int            `json:"scan_interval"` // 장비별 기본 수집 주기 (초)
	Sensors      []SensorConfig `json:"sensors"`
}

type SensorConfig struct {
	Name     string  `json:"name"`
	Address  uint16  `json:"address"`
	Scale    float64 `json:"scale"`
	Interval int     `json:"interval"` // 센서별 개별 수집 주기 (초)
}

type MQTTConfig struct {
	Broker   string `json:"broker"`
	Topic    string `json:"topic"`
	ClientID string `json:"client_id"`
}

type PortsConfig struct {
	HealthCheck int `json:"health_check"`
}

func LoadConfig(path string) (*Config, error) {
	cfg := &Config{LoadedFrom: "Default(File)"}

	f, err := os.Open(path)
	if err == nil {
		defer f.Close()
		if err := json.NewDecoder(f).Decode(cfg); err != nil {
			return nil, fmt.Errorf("설정 파일 파싱 실패: %w", err)
		}
	} else {
		cfg.LoadedFrom = "No File (Env/CLI Only)"
	}

	// 플래그 파싱 (이미 파싱된 경우 건너뜀)
	if !flag.Parsed() {
		flag.Parse()
	}

	if *brokerFlag != "" {
		cfg.MQTT.Broker = *brokerFlag
		cfg.LoadedFrom += " + CLI(MQTT)"
	}

	if envBroker := os.Getenv("MQTT_BROKER"); envBroker != "" {
		cfg.MQTT.Broker = envBroker
		cfg.LoadedFrom += " + ENV(MQTT)"
	}

	return cfg, nil
}
