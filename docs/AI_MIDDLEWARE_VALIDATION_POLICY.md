# AI Middleware 검증 정책

이 문서는 검증 실패/경고 분기 기준을 고정하기 위한 정책 문서입니다.
운영 환경에서 정책이 바뀌면 이 문서를 먼저 갱신합니다.
운영 로그 표준은 docs/AI_MIDDLEWARE_LOGGING.md를 참고합니다.

## 1) 기본 원칙
- 원본 데이터는 항상 저장합니다.
- 검증 실패 데이터는 Quarantine으로 격리합니다.
- 경고성 통과는 표준 저장을 유지하되, 추적용 Unknown 기록을 남깁니다.

## 2) 스키마 미등록 정책
스키마가 없을 때의 처리 기준은 시스템 속성으로 제어합니다.

### 속성 키
- ai.schema.missingPolicy

### 정책 값
- fail (기본값)
  - 처리: 검증 실패로 간주 → Quarantine 저장
  - 사유: VALIDATION_SCHEMA_MISMATCH:스키마 없음(...)
- pass
  - 처리: 스키마 없이 통과 → 표준 저장
  - 사유: 없음
- warn
  - 처리: 통과 + 경고 기록
  - 표준 저장: 수행
  - Unknown 기록: SCHEMA_MISSING_WARN:... 사유로 저장

## 3) 경고성 통과 처리 기준
- 조건: ValidationResult pass=true 이면서 reason이 "SCHEMA_MISSING_WARN:"로 시작
- 처리: 표준 저장 + Unknown Ingest 기록

## 4) 운영 시 권장 흐름
1) 개발/초기: warn (미등록 스키마 상황을 추적)
2) 안정화: fail (미등록 스키마 차단)
3) 긴급 대응: pass (임시 허용)

## 5) 기본 검증 실패 사유 코드(요약)
아래 코드는 BasicValidator 기준으로 대표적인 실패 사유를 요약한 것입니다.
운영 로그/격리 분석 시 이 목록을 기준으로 원인을 분류합니다.

- INGRESS_PAYLOAD_EMPTY: 원본 데이터 없음
- NORMALIZE_PARSE_ERROR: 정규화 결과가 비어 있음
- VALIDATION_MISSING_FIELD:messageType
- VALIDATION_MISSING_FIELD:deviceId
- VALIDATION_MISSING_FIELD:timestamp
- VALIDATION_MISSING_FIELD:eventId
- VALIDATION_MISSING_FIELD:protocolVersion
- VALIDATION_MISSING_FIELD:schemaVersion
- VALIDATION_INVALID_TYPE:eventId 길이 부족
- VALIDATION_INVALID_TYPE:protocolVersion 형식 오류
- VALIDATION_INVALID_TYPE:schemaVersion 형식 오류
- VALIDATION_INVALID_TYPE:timestamp 형식 오류
- CLASSIFICATION_MISSING_DEVICE_TYPE:분류 결과 없음
- CLASSIFICATION_MISSING_DEVICE_TYPE:deviceTypeId 없음
- CLASSIFICATION_LOW_CONFIDENCE:기준값 미만
