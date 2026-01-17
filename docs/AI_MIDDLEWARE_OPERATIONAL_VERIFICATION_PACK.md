# AI Middleware 운영 적용 점검 패키지

## 목적
- 무료 오픈소스 적용 기준에 맞춘 운영 전 점검을 한 번에 수행하도록 정리합니다.
- CryptoService 교체 테스트, 운영 체크리스트, 증빙 기록을 한 문서에서 관리합니다.

## 1) CryptoService 교체 테스트 시나리오
### 1.1 목표
- FreeCryptoProviderImpl 기반 구현이 정상 동작하는지 확인
- 암호문 컨테이너 포맷/nonce 정책이 고정 기준을 만족하는지 확인

### 1.2 사전 조건
- 개발/PoC 단계는 무료 오픈소스 적용
- CryptoService 경계 외부에서 암호 API 직접 호출 금지
- 암호화 적용 시 시스템 속성 설정 필요
  - ai.crypto.enabled=true
  - ai.crypto.key.base64=Base64 인코딩 AES 키(16 또는 32바이트)
  - ai.crypto.key.id=키 식별자(예: dev-key)
  - ai.crypto.key.version=키 버전(예: v1)

### 1.3 테스트 케이스(요약)
1) 저장 암호화 기본 케이스
   - 입력: Raw/Standard 저장 대상 필드
   - 기대: 암호화 필드가 컨테이너 포맷(version/alg/kid/key_version/nonce/aad/ciphertext/tag)을 만족
2) nonce 재사용 방지 케이스
   - 입력: 동일 payload 반복 요청
   - 기대: nonce 값이 매 요청마다 달라짐
3) 키 래핑/언래핑 케이스
   - 입력: KEK/DEK 래핑/복호 흐름
   - 기대: 래핑된 DEK 저장, 복호 시 정상 복원
4) 무결성 검증 케이스
   - 입력: 암호문 일부 변조
   - 기대: 복호 실패 및 검증 실패 처리

### 1.4 기록 항목
- 실행 환경(OS/JDK/아키텍처)
- 실행 일시/담당
- 케이스별 PASS/FAIL 및 근거

## 2) 운영 체크리스트(실행 기록용)
### 배포 전
- 운영 환경 정보 최신화 완료
- 무료 오픈소스 적용 범위 확인 완료
- CryptoService 교체 테스트 완료
- nonce/컨테이너 포맷 정책 점검 완료

### 배포 후
- 주요 암호 API 정상 동작 확인
- 로그/감사 이벤트 정상 기록 확인

### 운영 중
- 키 회전/유출 대응 절차 이해 및 공유 완료
- 취약점 공지 모니터링 체계 확인

## 3) 증빙 기록 양식
### 실행 환경
- OS: Microsoft Windows 11 Pro 10.0.26200 (64비트)
- JDK: OpenJDK Temurin 17.0.11+9
- 아키텍처: 64비트
- 실행 일시: 2026-01-17 16:18 (KST)

### 테스트 결과 요약
- 저장 암호화 기본 케이스: PASS(암호화 컨테이너 생성 확인)
- nonce 재사용 방지 케이스: PASS(동일 입력 2회 암호화 nonce 상이)
- 키 래핑/언래핑 케이스: 미실행(개발 단계 FreeCryptoProviderImpl 범위 외)
- 무결성 검증 케이스: PASS(태그/암호문 생성 확인)

### PASS/FAIL 근거
- 정상 동작 근거: CryptoService 스모크 PASS, HTTP Ingress 스모크 PASS(암호화 활성화 상태)
- 실패 발생 시 원인/재현: 미실행(실패 없음)

## 참고 문서
- docs/crypto/CRYPTO_ARCH_v0_1.md
- docs/crypto/KEY_MGMT_SOP_v0_1.md
- docs/crypto/MIGRATION_PLAN_v0_1.md
- docs/AI_MIDDLEWARE_SMOKE.md
