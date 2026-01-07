IoT-Edge-Gateway (MES Platform)
===============================

이 디렉터리는 **MES-Platform용 IoT 인터페이스(Edge Gateway)** 전용 작업 공간입니다.

> 중요: `ai-middleware`, `docs` 등 MES-Platform의 다른 디렉터리는 수정하지 않고,
> 모든 Edge Gateway 관련 코드는 반드시 `IoT-Edge-Gateway/` 내부에만 위치합니다.

## 목표

- 제조 장비(Modbus 기반)를 대상으로 한 데이터 수집 PoC
- Envelope 규격(Envelope + Telemetry/Event/Command/Ack) 적용
- MQTT(1883) 또는 REST(18080)를 통한 MES 연동 시험
- 오류 3종(타임아웃 / 주소 오류 / 인증 실패) 처리 및 격리(quarantine)

## 포트 정책 (초안)

- Edge Gateway HTTP: **18080**
- Edge Gateway MQTT: **1883**
- AI Middleware HTTP: **18081**
- AI Middleware MQTT: **1884**
- MES Web Service: **8080**

## 디렉터리 구조 (PoC + Go 본개발 초안)

- `src/`  
  - PoC용 Python 스크립트 (Modbus → Envelope → MQTT/REST)
- `cmd/edge-gateway/`  
  - Go 메인 엔트리포인트 (단일 바이너리 목표, PoC 루프 포함)
- `internal/`  
  - Go 내부 패키지(추후 Modbus, MQTT, 설정, 로거 등 핵심 로직 모듈화 예정)
- `config/`  
  - 설정 파일 (`config.json` – PoC용, 추후 확장 가능)
- `logs/`  
  - 실행 로그 및 정상 처리 내역
- `quarantine/`  
  - 실패/오류 데이터 격리 저장소 (원본 보존)
- `scripts/`  
  - 실행/테스트용 보조 스크립트 (예: PowerShell, 배치 등)

## 규칙 (필수)

- **protocolVersion / schemaVersion** 필드 반드시 포함
- **원본 데이터는 수정하지 않고 별도 보관**
- **실패 데이터는 `quarantine/` 디렉터리에 격리 저장**
- 공통 메모 및 변경 사항은 `local_docs/EdgeGateway_Shared_Notes.md`에 기록

## 사용 예시

### Python PoC (기존)
- 가상환경 생성 후 `pip install -r requirements.txt`
- `python src/main_poc.py`

### Go PoC/본개발 스캐폴드
1) `cd IoT-Edge-Gateway`
2) (최초 1회) `go mod tidy`로 의존성 정리
3) (Go 1.21+) `go run ./cmd/edge-gateway`
   - MQTT 브로커: 기본 `localhost:1883` (코드 상에서 설정)
   - Modbus 서버: 기본 `localhost:502` (코드 상에서 설정)
   - 오류 발생 시 `quarantine/`에 실패 사유 로그 파일 생성


