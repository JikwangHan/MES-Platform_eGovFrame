package crypto

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"errors"
	"io"
)

// FreeCryptoImpl는 개발/PoC 단계에서 사용하는 무료 구현체입니다.
// 목적: KCMVP 전환 전에도 동일 포맷/정책으로 암호화를 검증합니다.
type FreeCryptoImpl struct {
	key []byte
}

// NewFreeCryptoImpl는 개발용 암호 모듈을 생성합니다.
// 유지보수 관점: 상용 전환 시 이 생성부는 그대로 두고 구현체만 교체합니다.
func NewFreeCryptoImpl(key []byte) (*FreeCryptoImpl, error) {
	if len(key) != 16 && len(key) != 24 && len(key) != 32 {
		return nil, errors.New("invalid AES key length")
	}
	return &FreeCryptoImpl{key: key}, nil
}

// Encrypt는 AES-GCM으로 암호문 컨테이너를 생성합니다.
// 목적: AEAD 포맷을 고정하여 KCMVP 모듈로 쉽게 교체할 수 있게 합니다.
func (c *FreeCryptoImpl) Encrypt(plain, aad []byte, meta Meta) ([]byte, error) {
	block, err := aes.NewCipher(c.key)
	if err != nil {
		return nil, err
	}
	aead, err := cipher.NewGCM(block)
	if err != nil {
		return nil, err
	}
	nonce := make([]byte, aead.NonceSize())
	if _, err := io.ReadFull(rand.Reader, nonce); err != nil {
		return nil, err
	}
	ciphertext := aead.Seal(nil, nonce, plain, aad)
	tagLen := aead.Overhead()
	if len(ciphertext) < tagLen {
		return nil, errors.New("ciphertext too short")
	}
	body := ciphertext[:len(ciphertext)-tagLen]
	tag := ciphertext[len(ciphertext)-tagLen:]

	container := Container{
		Version:    1,
		Alg:        meta.Alg,
		KeyID:      meta.KeyID,
		KeyVersion: meta.KeyVersion,
		Nonce:      b64(nonce),
		AAD:        b64(aad),
		Ciphertext: b64(body),
		Tag:        b64(tag),
	}
	return container.Encode()
}

// Decrypt는 컨테이너를 복호화합니다.
// 목적: Retired 키 복호 지원 등 호환성 검증을 위한 기본 경로를 제공합니다.
func (c *FreeCryptoImpl) Decrypt(containerRaw []byte, aad []byte) ([]byte, error) {
	container, err := Decode(containerRaw)
	if err != nil {
		return nil, err
	}
	nonce, err := b64d(container.Nonce)
	if err != nil {
		return nil, err
	}
	body, err := b64d(container.Ciphertext)
	if err != nil {
		return nil, err
	}
	tag, err := b64d(container.Tag)
	if err != nil {
		return nil, err
	}
	if container.AAD != "" {
		if aad, err = b64d(container.AAD); err != nil {
			return nil, err
		}
	}

	block, err := aes.NewCipher(c.key)
	if err != nil {
		return nil, err
	}
	aead, err := cipher.NewGCM(block)
	if err != nil {
		return nil, err
	}
	ciphertext := append(body, tag...)
	return aead.Open(nil, nonce, ciphertext, aad)
}
