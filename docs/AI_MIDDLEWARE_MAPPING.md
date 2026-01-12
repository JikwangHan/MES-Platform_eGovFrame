# AI Middleware 키 매핑 표(정규화 기준)

이 문서는 장비별/포맷별로 들어오는 키를 표준 키로 통일하기 위한 기준입니다.
정규화 단계에서 사용하는 별칭 목록과 동일하게 유지해야 합니다.

## 1) 표준 키 목록
- deviceId
- deviceTypeId
- eventId
- timestamp
- messageType
- protocolVersion
- schemaVersion

## 2) 키 매핑 규칙(별칭 → 표준 키)
아래 별칭은 들어오는 payload에서 발견되면 표준 키로 복사됩니다.
표준 키가 이미 있으면 덮어쓰지 않습니다.

### deviceId
- device_id
- device-id
- device

### deviceTypeId
- device_type_id
- device_type
- device-type

### eventId
- event_id
- event-id
- event

### timestamp
- eventTime
- event_time
- time
- ts

### messageType
- message_type
- type
- msgType
- msg_type

### protocolVersion
- protocol_version
- protocol

### schemaVersion
- schema_version
- schema

## 3) 운영 원칙
- 표준 키가 있으면 별칭 값은 무시합니다.
- 별칭 값이 비어 있으면 복사하지 않습니다.
- 표준 키 체계가 바뀌면 이 문서와 정규화 유틸을 동시에 수정합니다.
