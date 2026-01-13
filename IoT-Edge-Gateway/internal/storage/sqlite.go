package storage

import (
	"database/sql"

	"mes-iot-edge/internal/logger"
	_ "modernc.org/sqlite"
)

// UnsentData는 재전송 대상 레코드를 표현합니다.
// 목적: MQTT 장애 시 저장된 데이터를 FIFO로 복구 전송하기 위함입니다.
type UnsentData struct {
	ID         int
	DeviceName string
	SensorName string
	Value      float64
	Timestamp  string
}

// InitDB는 로컬 저장소를 초기화하고 DB 핸들을 반환합니다.
// 목적: 장애 복구(Store & Forward)를 위한 최소 스키마를 보장합니다.
func InitDB(dbPath string) (*sql.DB, error) {
	db, err := sql.Open("sqlite", dbPath)
	if err != nil {
		return nil, err
	}
	query := `
	CREATE TABLE IF NOT EXISTS sensor_data (
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		device_name TEXT,
		sensor_name TEXT,
		value REAL,
		timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
	);`
	if _, err := db.Exec(query); err != nil {
		return nil, err
	}
	if logger.Log != nil {
		logger.Log.Info("SQLite storage initialized")
	}
	return db, nil
}

// SaveSensorData는 전송 실패 데이터를 로컬에 저장합니다.
// 유지보수 관점: 스키마 확장 시 INSERT 컬럼을 함께 업데이트합니다.
func SaveSensorData(db *sql.DB, deviceName string, sensorName string, value float64) {
	query := `INSERT INTO sensor_data (device_name, sensor_name, value) VALUES (?, ?, ?)`
	if _, err := db.Exec(query, deviceName, sensorName, value); err != nil {
		if logger.Log != nil {
			logger.Log.Errorf("DB save failed [%s]: %v", deviceName, err)
		}
	}
}

// GetOldestData는 가장 오래된 레코드를 반환합니다(FIFO).
// 목적: 시간 순서대로 재전송하여 데이터 정합성을 유지합니다.
func GetOldestData(db *sql.DB) (*UnsentData, error) {
	query := `SELECT id, device_name, sensor_name, value, timestamp FROM sensor_data ORDER BY id ASC LIMIT 1`
	row := db.QueryRow(query)

	var d UnsentData
	if err := row.Scan(&d.ID, &d.DeviceName, &d.SensorName, &d.Value, &d.Timestamp); err != nil {
		return nil, err
	}
	return &d, nil
}

// DeleteData는 전송 성공 후 레코드를 삭제합니다.
// 목적: 중복 전송을 방지하고 저장소를 정리합니다.
func DeleteData(db *sql.DB, id int) {
	_, _ = db.Exec("DELETE FROM sensor_data WHERE id = ?", id)
}

// CloseDB는 DB 핸들을 안전하게 종료합니다.
// 유지보수 관점: 종료 시 리소스 누수를 방지합니다.
func CloseDB(db *sql.DB) {
	if db != nil {
		_ = db.Close()
	}
}
