package logger

import (
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

// Log는 게이트웨이 전역에서 사용하는 로거 인스턴스입니다.
var Log *zap.SugaredLogger

// InitLogger는 로거를 초기화합니다.
// 목적: 파일/콘솔에 동일한 포맷으로 로그를 남겨 운영 가시성을 확보합니다.
func InitLogger() {
	cfg := zap.NewProductionConfig()
	cfg.EncoderConfig.EncodeTime = zapcore.ISO8601TimeEncoder
	cfg.OutputPaths = []string{"stdout", "logs/gateway.log"}

	logger, err := cfg.Build()
	if err != nil {
		Log = nil
		return
	}
	Log = logger.Sugar()
}

// Sync는 버퍼에 남은 로그를 안전하게 플러시합니다.
// 유지보수 관점: 종료 시 로그 유실을 줄이기 위해 호출합니다.
func Sync() error {
	if Log == nil {
		return nil
	}
	return Log.Sync()
}
