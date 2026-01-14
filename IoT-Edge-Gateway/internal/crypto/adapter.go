package crypto

// CryptoAdapter는 암호 경계(Crypto Boundary)를 고정하기 위한 인터페이스입니다.
// 목적: 개발/상용 단계에서 구현체만 교체하고 포맷/정책은 동일 유지합니다.
type CryptoAdapter interface {
	// Encrypt는 평문을 암호문 컨테이너로 변환합니다.
	// 이유: 컨테이너 포맷을 고정하여 KCMVP 전환 시 데이터 호환성을 확보합니다.
	Encrypt(plain, aad []byte, meta Meta) ([]byte, error)
	// Decrypt는 암호문 컨테이너를 복호화합니다.
	// 목적: Retired 키 복호 허용 등 호환성을 보장하기 위함입니다.
	Decrypt(container []byte, aad []byte) ([]byte, error)
}

// Meta는 암호문 컨테이너에 포함될 키 식별 정보를 정의합니다.
// 유지보수 관점: key_id/key_version을 고정하여 회전/폐기 추적을 가능하게 합니다.
type Meta struct {
	KeyID      string
	KeyVersion string
	Alg        string
}
