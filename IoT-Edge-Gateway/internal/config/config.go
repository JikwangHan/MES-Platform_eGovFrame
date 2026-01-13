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

// Config는 게이트웨이 전체 설정의 루트 구조체입니다.
// 목적: 장비/전송/포트 설정을 한 곳에서 일관되게 관리합니다.
type Config struct {
	GatewayID  string         `json:"gateway_id"`
	DeviceName string         `json:"device_name"`
	Devices    []DeviceConfig `json:"devices"`
	MQTT       MQTTConfig     `json:"mqtt"`
	LogAPI     LogAPIConfig   `json:"log_api"`
	Ports      PortsConfig    `json:"ports"`
	LoadedFrom string
}

// DeviceConfig는 장비별 통신 정보와 센서 목록을 정의합니다.
// 목적: 장비마다 주소/타임아웃/주기를 독립적으로 관리하기 위함입니다.
type DeviceConfig struct {
	Name         string         `json:"name"`
	Type         string         `json:"type"`
	Address      string         `json:"address"`
	SlaveID      int            `json:"slave_id"`
	Timeout      int            `json:"timeout"`
	ScanInterval int            `json:"scan_interval"` // 장비별 기본 수집 주기 (초)
	Sensors      []SensorConfig `json:"sensors"`
}

// SensorConfig는 센서별 주소와 스케일/주기를 정의합니다.
// 목적: 동일 장비 내에서도 센서별 주기를 분리하여 성능을 최적화합니다.
type SensorConfig struct {
	Name     string  `json:"name"`
	Address  uint16  `json:"address"`
	Scale    float64 `json:"scale"`
	Interval int     `json:"interval"` // 센서별 개별 수집 주기 (초)
}

// MQTTConfig는 전송 브로커 정보를 정의합니다.
// 유지보수 관점: 인증/TLS 등의 확장 필드를 추가해도 구조를 유지합니다.
type MQTTConfig struct {
	Broker   string `json:"broker"`
	Topic    string `json:"topic"`
	ClientID string `json:"client_id"`
	Username string `json:"username"` // 브로커 인증용 사용자명
	Password string `json:"password"` // 브로커 인증용 비밀번호
}

// PortsConfig는 외부 노출 포트를 정의합니다.
// 목적: 운영 환경별 포트 충돌을 쉽게 회피하도록 설정화합니다.
type PortsConfig struct {
	HealthCheck int `json:"health_check"`
}

// LogAPIConfig는 사용 로그 수집 API 연동 설정입니다.
// 목적: 가상/실서버 전환과 전송 정책을 설정으로 분리합니다.
type LogAPIConfig struct {
	Enabled         bool   `json:"enabled"`
	BaseURL         string `json:"base_url"`
	Mode            string `json:"mode"` // mock | prod
	CrtfcKey        string `json:"crtfc_key"`
	UseSe           string `json:"use_se"`            // DO6001~DO6999
	SourceIP        string `json:"source_ip"`         // 미지정 시 자동 추정
	MinIntervalSec  int    `json:"min_interval_sec"`  // 최소 전송 간격(초)
	RequestTimeoutS int    `json:"request_timeout_s"` // 전송 타임아웃(초)
}

// LoadConfig는 파일/환경/CLI를 순서대로 병합하여 최종 설정을 만듭니다.
// 목적: 운영 환경별 설정 변경을 코드 수정 없이 적용하기 위함입니다.
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
