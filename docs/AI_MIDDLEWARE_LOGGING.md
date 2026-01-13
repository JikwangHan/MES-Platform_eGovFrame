# AI Middleware 운영 로그 표준

이 문서는 파이프라인 처리 결과를 운영 로그로 남길 때의 표준 형식을 정의합니다.
원인 분석/통계 집계를 쉽게 하기 위해 코드/상세를 분리해 기록합니다.

## 1) 로그 이벤트명
- PIPELINE_DECISION

## 2) 로그 필드(키=값)
- event: 고정값 PIPELINE_DECISION
- decision: STANDARD | QUARANTINE | UNKNOWN
- rawId: 원본 데이터 ID(없으면 -)
- ingressType: 수신 경로(mqtt/http 등)
- contentType: 콘텐츠 타입 힌트
- reasonCode: 실패/경고/차단 코드
- reasonDetail: 상세 사유(없으면 -)
- scanStatus: CLEAN/INFECTED/ERROR 등 스캔 상태(없으면 -)

## 3) reasonCode 규칙
- 기본 검증 실패는 ValidationResult의 reason 앞부분(콜론 앞)이 code가 됩니다.
- 예시:
  - VALIDATION_MISSING_FIELD
  - VALIDATION_INVALID_TYPE
  - VALIDATION_SCHEMA_MISMATCH
  - INGRESS_PAYLOAD_EMPTY
  - SECURITY_SCAN_BLOCKED
  - UNKNOWN_INGEST
  - VALIDATION_PASS
  - SCHEMA_MISSING_WARN

## 4) 로그 예시
```
event=PIPELINE_DECISION decision=STANDARD rawId=raw-001 ingressType=http contentType=application/json reasonCode=VALIDATION_PASS reasonDetail=- scanStatus=CLEAN
event=PIPELINE_DECISION decision=QUARANTINE rawId=raw-002 ingressType=http contentType=application/json reasonCode=VALIDATION_INVALID_TYPE reasonDetail=protocolVersion_형식_오류 scanStatus=CLEAN
event=PIPELINE_DECISION decision=UNKNOWN rawId=raw-003 ingressType=http contentType=application/json reasonCode=SCHEMA_MISSING_WARN reasonDetail=schemaVersion=2.0,messageType=TELEMETRY,deviceTypeId=MES scanStatus=CLEAN
```

## 5) 운영 주의사항
- 로그에는 원본 payload/개인정보를 남기지 않습니다.
- reasonDetail은 공백을 언더스코어로 치환하여 파싱 안정성을 유지합니다.
- 로그 형식 변경 시 이 문서와 `PipelineOrchestrator` 로그 출력을 동시에 수정합니다.
