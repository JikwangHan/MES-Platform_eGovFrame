# AI Middleware 저장/격리 JDBC 정책

이 문서는 JDBC 저장 로직의 컬럼 구성과 운영 모드를 정의합니다.
운영 환경의 테이블 구조가 다를 경우 이 문서를 기준으로 조정합니다.

## 1) 저장 모드(System Property)
- 키: ai.jdbc.insertMode
- 값:
  - basic (기본값)
  - extended (확장 컬럼 사용)

## 2) raw_data 저장 (기본 모드)
테이블 컬럼(기본):
- received_at
- ingress_type
- payload
- payload_hash
- source_id_hash
- content_type
- created_at

## 3) parsed_data 저장
### basic 모드 컬럼
- raw_id
- standard_payload
- schema_version
- protocol_version
- created_at

### extended 모드 컬럼
- raw_id
- standard_payload
- schema_version
- protocol_version
- device_id
- message_type
- event_id
- event_time
- created_at

## 4) quarantine_data 저장
### basic 모드 컬럼
- raw_id
- reason_code
- reason_detail
- created_at

### extended 모드 컬럼
- raw_id
- reason_code
- reason_detail
- scan_status
- scan_engine
- scan_signature
- scan_duration_ms
- created_at

## 5) 운영 가이드
- 기본 모드는 최소 컬럼만 요구하므로 초기 구축에 적합합니다.
- 확장 모드는 조회/추적 편의가 높지만 컬럼 준비가 필요합니다.
- 모드를 바꿀 때는 DB 테이블 컬럼을 먼저 맞춘 뒤 적용합니다.
