CRYPTO_ARCH_v0_1

목적
- 암호 경계(crypto boundary), 암호문 컨테이너 포맷, nonce 정책, 키 계층, 장애 정책(Fail-Closed)을 고정 설계합니다.

범위
- IoT-Edge-Gateway, AI Middleware, MES Web Service

목표
- 개발/PoC 단계: 비용 0원(오픈소스 또는 KISA 표준 알고리즘 소스) 기반 암호 적용
- 상용화 단계: KCMVP 검증필 모듈(+ 패턴 C는 HSM)로 “암호모듈만 교체”
- 데이터 호환을 위해 컨테이너 포맷과 정책은 처음부터 고정

서비스별 패턴(확정)
- IoT-Edge-Gateway: 패턴 A(검증필 SW 모듈 내장형)
- AI Middleware: 패턴 C(핵심 키 HSM + 나머지 SW)
- MES Web Service: 패턴 C(핵심 키 HSM + 나머지 SW)
  - 개발 단계: HSM 인터페이스 유지, 실제 장비 연동 비활성

IoT-Edge-Gateway 적용 현황(Go)
- CryptoAdapter 인터페이스 및 Free/Kcmvp 구현체 골격 추가 완료
- 컨테이너 포맷(JSON) 고정: version/alg/kid/key_version/nonce/aad/ciphertext/tag

암호 경계(Crypto Boundary) 설계
- Edge Gateway(Go): CryptoAdapter 인터페이스 + 구현체 2개 분리
  - FreeCryptoImpl: 개발 단계 무료 구현(오픈소스 또는 KISA C 소스 cgo 연동)
  - KcmvpCryptoImpl: 상용화 전환용 구현체(검증필 모듈 교체)
- Edge Gateway(Java/Android): JCE Provider 또는 CryptoService 레이어로 캡슐화
  - 개발 단계: FreeCryptoProviderImpl(오픈소스/표준 알고리즘 기반)
  - 상용화 단계: KcmvpProviderImpl로 교체(검증필 모듈)
- AI/MES(Java): CryptoService 레이어로 캡슐화
  - 개발 단계: FreeCryptoProviderImpl(오픈소스/표준 알고리즘 기반)
  - 상용화 단계: KcmvpProviderImpl로 교체(검증필 모듈)
  - 암호 경계와 컨테이너 포맷은 교체 전후 동일 유지

FreeCryptoProviderImpl 적용 범위(개발/PoC)
- 대상: AI Middleware, MES Web Service(Java 계열)
- 적용 위치: CryptoService 경계 내부(직접 호출 금지)
- 제공 기능:
  - AEAD 암복호화(GCM/CCM) 및 nonce 생성/관리
  - 해시/SHA-2 계열(예: payloadHash)
  - 키 래핑/언래핑(KEK/DEK 구조 유지)
- 적용 대상 데이터:
  - 저장 데이터 암호화 필드 범위는 `docs/AI_MIDDLEWARE_KCMVP_ENCRYPTION_POLICY.md` 기준을 따른다.
  - 로그/무결성 관련 해시/서명 범위는 운영 정책 문서 기준을 따른다.
- 제한 사항:
  - HSM 인터페이스는 유지하되 실제 장비 연동은 비활성
  - 컨테이너 포맷/nonce 정책은 상용화 단계와 동일 유지

암호문 컨테이너 포맷(고정)
- AEAD(GCM 또는 CCM) 기반
- 포맷 필드(바이너리 또는 JSON/CBOR 중 택1):
  - version (예: 1)
  - alg (예: ARIA-128-GCM, LEA-128-GCM)
  - kid(키 ID), key_version
  - nonce(iv) (절대 재사용 금지)
  - aad(옵션)
  - ciphertext
  - tag
- 모든 암호문에 key_id/key_version 포함(회전/폐기/재암호화 추적)

nonce/IV 정책(강제)
- GCM/CCM에서 nonce 재사용 금지
- 예시 규칙:
  - nonce = H(key_version || device_id || message_id || random)
  - random은 CSPRNG 사용
  - message_id는 단조 증가 또는 UUID로 유니크 보장
- ECB 모드 금지(테스트 제외)

키 계층(패턴 C)
- 대량 데이터 암복호화는 DEK 사용
- 상용화 단계에서는 KEK/서명키 등 핵심 키를 HSM에서 생성·보관
- 개발 단계에서는 HSM 인터페이스만 유지하고 실제 장비 연동은 비활성
- DEK 평문 저장 금지
  - wrapped_dek = Wrap(DEK, KEK)
  - 복호는 메모리 내 단기 사용 후 폐기(가능 시 zeroize)

장애 정책(Fail-Closed)
- HSM 불가용 시 신규 암호화/회전/재암호화 중지
- 신규 암호화 요청은 큐 적재 후 재시도 또는 제한 모드 전환

비고
- 무료 구현과 상용 모듈 전환 간 데이터 포맷/정책은 동일 유지
