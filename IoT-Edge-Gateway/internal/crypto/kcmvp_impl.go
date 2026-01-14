package crypto

import "errors"

// KcmvpCryptoImpl는 상용화 단계에서 검증필 모듈로 교체되는 구현체입니다.
// 목적: 개발 단계에서는 placeholder로 두고, 상용 전환 시 구현체만 교체합니다.
type KcmvpCryptoImpl struct{}

// NewKcmvpCryptoImpl는 KCMVP 구현체를 생성합니다.
// 유지보수 관점: 실제 모듈 연동 시 이 생성부를 교체합니다.
func NewKcmvpCryptoImpl() *KcmvpCryptoImpl {
	return &KcmvpCryptoImpl{}
}

// Encrypt는 KCMVP 모듈 연동이 필요하므로 현재는 미구현입니다.
func (k *KcmvpCryptoImpl) Encrypt(plain, aad []byte, meta Meta) ([]byte, error) {
	return nil, errors.New("kcmvp crypto not implemented")
}

// Decrypt는 KCMVP 모듈 연동이 필요하므로 현재는 미구현입니다.
func (k *KcmvpCryptoImpl) Decrypt(container []byte, aad []byte) ([]byte, error) {
	return nil, errors.New("kcmvp crypto not implemented")
}
