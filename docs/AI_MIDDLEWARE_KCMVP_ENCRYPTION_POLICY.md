# AI Middleware KCMVP 암호화·TLS 운영 정책(v0.2)

## 목적
- 저장 데이터 암호화와 전송 구간 TLS 운영 기준을 확정합니다.
- 감사/예비검토 대응 시 일관된 정책 근거를 제공합니다.

## 문서 정합성 기준
- 적용 범위/패턴은 `docs/AI_MIDDLEWARE_KCMVP_SCOPE.md` 기준을 따릅니다.
- 키 생성/보관/회전/폐기는 `docs/AI_MIDDLEWARE_KCMVP_KEY_POLICY.md` 기준을 따릅니다.
- 모듈 후보 확정/운영 환경 검증은 `docs/AI_MIDDLEWARE_KCMVP_MODULE_CHECKLIST.md` 기준을 따릅니다.

## 저장 데이터 암호화 범위(필드 기준)
아래는 기본 적용 범위이며, 운영 환경에 따라 조정할 수 있습니다.

### 1) Raw 저장
- payloadBase64 (원문)
- payloadHash (무결성 확인값)
- sourceIdHash (송신 식별자 해시)
- contentType

### 2) Standard 저장
- payload (정규화 데이터)
- deviceId
- protocolVersion
- schemaVersion
- timestamp

### 3) Quarantine 저장
- rawEnvelope.payloadBase64
- reason (사유)
- scanSignature (보안 스캔 시그니처)

### 4) Unknown 저장
- payloadBase64
- quarantineReason
- scanSignature

## 암호화 방식(정책)
- 검증필 암호모듈의 대칭키 암호화 사용
- 키 래핑(KEK/DEK) 구조는 KCMVP 정책 문서 준수
- 암호화 대상 필드는 별도 표준 목록으로 관리
- AEAD(GCM/CCM) 포맷 고정 및 nonce 재사용 금지

## TLS 운영 정책
### 1) TLS 적용 구간
- Ingress HTTP(TLS 필수)
- 내부 서비스 간 연계(필요 구간)

### 2) 인증서 정책
- 공공/기관 납품 시 기관 정책에 맞는 인증서 사용
- 인증서 만료 30일 전 교체
- 교체 이력은 감사 로그로 보관

### 3) 프로토콜/암호군
- 검증필 모듈에서 허용된 TLS 버전만 사용
- 취약한 암호군은 차단

## 운영 체크리스트
- 암호화 필드 범위가 문서와 일치하는지 확인
- 인증서 만료/교체 주기 준수 여부 확인
- 검증필 모듈 해시 일치 여부 확인
- 운영 환경 호환성 결과 반영 여부 확인

## 다음 단계(진행 순서)
1) 암호화 대상 필드 최종 확정(업무팀 협의)
2) TLS 인증서 운영 절차 문서화(운영팀 협의)
3) 테스트 시나리오(암복호/인증서 교체) 정의
