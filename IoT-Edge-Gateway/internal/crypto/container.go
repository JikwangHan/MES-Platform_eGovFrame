package crypto

import (
	"encoding/base64"
	"encoding/json"
	"errors"
)

// Container는 암호문 포맷을 고정하는 구조체입니다.
// 목적: 개발/상용 모듈 교체 시에도 동일 포맷으로 해석되도록 합니다.
type Container struct {
	Version    int    `json:"version"`
	Alg        string `json:"alg"`
	KeyID      string `json:"kid"`
	KeyVersion string `json:"key_version"`
	Nonce      string `json:"nonce"`
	AAD        string `json:"aad,omitempty"`
	Ciphertext string `json:"ciphertext"`
	Tag        string `json:"tag"`
}

// Encode는 컨테이너를 JSON 바이트로 직렬화합니다.
// 이유: 메시지/DB 저장 시 포맷을 고정하기 위함입니다.
func (c Container) Encode() ([]byte, error) {
	return json.Marshal(c)
}

// Decode는 JSON 바이트를 컨테이너로 복원합니다.
// 목적: 암호문 컨테이너 포맷을 단일 경로로 파싱합니다.
func Decode(raw []byte) (Container, error) {
	var c Container
	if err := json.Unmarshal(raw, &c); err != nil {
		return Container{}, err
	}
	if c.Version == 0 || c.Alg == "" || c.Nonce == "" || c.Ciphertext == "" || c.Tag == "" {
		return Container{}, errors.New("crypto container invalid")
	}
	return c, nil
}

// b64는 base64 인코딩을 통일합니다.
func b64(data []byte) string {
	return base64.StdEncoding.EncodeToString(data)
}

// b64d는 base64 디코딩을 통일합니다.
func b64d(value string) ([]byte, error) {
	return base64.StdEncoding.DecodeString(value)
}
