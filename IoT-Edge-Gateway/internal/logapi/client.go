package logapi

import (
	"bytes"
	"context"
	"encoding/json"
	"net"
	"net/http"
	"strings"
	"sync"
	"time"
)

// Client는 스마트공장 로그 수집 API 전송을 담당합니다.
// 목적: 가상/실서버 전송을 동일한 포맷으로 검증하기 위함입니다.
type Client struct {
	enabled        bool
	baseURL        string
	crtfcKey       string
	useSe          string
	sourceIP       string
	minInterval    time.Duration
	requestTimeout time.Duration

	mu       sync.Mutex
	lastSend time.Time
}

// Config는 로그 연동 설정 값을 전달받기 위한 입력 구조체입니다.
// 유지보수 관점: 내부 config 패키지와 결합도를 낮춥니다.
type Config struct {
	Enabled         bool
	BaseURL         string
	CrtfcKey        string
	UseSe           string
	SourceIP        string
	MinIntervalSec  int
	RequestTimeoutS int
}

// NewClient는 전송 클라이언트를 생성합니다.
// 목적: 설정값을 고정해 전송 로직을 단순화합니다.
func NewClient(cfg Config) *Client {
	minInterval := time.Duration(cfg.MinIntervalSec) * time.Second
	if minInterval <= 0 {
		minInterval = 10 * time.Minute
	}
	timeout := time.Duration(cfg.RequestTimeoutS) * time.Second
	if timeout <= 0 {
		timeout = 5 * time.Second
	}
	useSe := cfg.UseSe
	if useSe == "" {
		useSe = "DO6003"
	}
	return &Client{
		enabled:        cfg.Enabled,
		baseURL:        strings.TrimRight(cfg.BaseURL, "/"),
		crtfcKey:       cfg.CrtfcKey,
		useSe:          useSe,
		sourceIP:       cfg.SourceIP,
		minInterval:    minInterval,
		requestTimeout: timeout,
	}
}

// TrySend는 전송 간격을 확인한 뒤 로그 전송을 시도합니다.
// 목적: 최소 10분 전송 규칙을 지키며 과도한 전송을 방지합니다.
func (c *Client) TrySend(ctx context.Context, logDt, sysUser string, dataUsgqty int) (bool, error) {
	if c == nil || !c.enabled || c.baseURL == "" || c.crtfcKey == "" {
		return false, nil
	}

	c.mu.Lock()
	if time.Since(c.lastSend) < c.minInterval {
		c.mu.Unlock()
		return false, nil
	}
	c.lastSend = time.Now()
	c.mu.Unlock()

	req := logRequest{
		CrtfcKey:  c.crtfcKey,
		LogDt:     logDt,
		UseSe:     c.useSe,
		SysUser:   sysUser,
		ConectIP:  c.resolveIP(),
		DataUsgqty: dataUsgqty,
	}

	return true, c.send(ctx, req)
}

// logRequest는 API 입력 규격(JSON)입니다.
type logRequest struct {
	CrtfcKey   string `json:"crtfcKey"`
	LogDt      string `json:"logDt"`
	UseSe      string `json:"useSe"`
	SysUser    string `json:"sysUser"`
	ConectIP   string `json:"conectIp"`
	DataUsgqty int    `json:"dataUsgqty"`
}

func (c *Client) send(ctx context.Context, req logRequest) error {
	body, err := json.Marshal(req)
	if err != nil {
		return err
	}
	ctx, cancel := context.WithTimeout(ctx, c.requestTimeout)
	defer cancel()

	url := c.baseURL + "/apisvc/sendLogData.json"
	httpReq, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewReader(body))
	if err != nil {
		return err
	}
	httpReq.Header.Set("Content-Type", "application/json")

	resp, err := http.DefaultClient.Do(httpReq)
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	return nil
}

func (c *Client) resolveIP() string {
	if c.sourceIP != "" {
		return c.sourceIP
	}
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		return "127.0.0.1"
	}
	for _, addr := range addrs {
		ipNet, ok := addr.(*net.IPNet)
		if !ok || ipNet.IP == nil || ipNet.IP.IsLoopback() {
			continue
		}
		if ip4 := ipNet.IP.To4(); ip4 != nil {
			return ip4.String()
		}
	}
	return "127.0.0.1"
}
