MES Platform (eGovFrame)

이 저장소는 MES(Manufacturing Execution System, 제조 실행 시스템) 플랫폼을 개발하기 위한 초기 저장소입니다.
프로젝트에서 꼭 지켜야 할 기준과 참고 자료를 정리했으며, 초보자도 이해할 수 있도록 설명을 추가했습니다.

중요 안내
- `00. ref` 폴더는 로컬 전용 참고자료입니다. GitHub에 올리지 않습니다.
- 프로젝트 기준과 규칙은 `docs/BASELINE.md`에 정리되어 있습니다.
- 아직 확정해야 하는 결정 사항은 `docs/OPEN_QUESTIONS.md`에 있습니다.

시작 위치
- `docs/BASELINE.md`: 프로젝트 범위, 보안, UI/UX, AI 미들웨어, 개발 절차 등 핵심 기준
- `docs/OPEN_QUESTIONS.md`: 구현 전에 반드시 확정해야 할 질문 목록

테스트 스크립트(초보자용)
- `scripts/user_test_pipeline.ps1`: 파이프라인 스모크 테스트(메모리 스캔/정규화 흐름 확인)
- `scripts/jdbc_schema_smoke.ps1`: DB 스키마 키 조회/스키마 조회 테스트
  - 실행 전 환경 변수 설정(예시)
    - `AI_SCHEMA_JDBC_URL`: `jdbc:mariadb://localhost:3306/mes-dev-test`
    - `AI_SCHEMA_JDBC_USER`: `mes_user`
    - `AI_SCHEMA_JDBC_PASSWORD`: 비밀번호는 직접 입력(스크립트가 요청함)
    - `AI_SCHEMA_KEY_SCHEMA_VERSION`: `1.0`
    - `AI_SCHEMA_KEY_MESSAGE_TYPE`: `TELEMETRY`
    - `AI_SCHEMA_KEY_DEVICE_TYPE_ID`: `MES`
- `scripts/http_ingress_smoke.ps1`: HTTP Ingress 스모크 테스트(포트/경로/키를 쉽게 변경)
  - 실행 전 환경 변수 또는 파라미터로 설정
    - `AI_HTTP_PORT`: `8080` (기본 8080)
    - `AI_HTTP_PATH`: `/ingest`
    - `AI_SCHEMA_KEY_SCHEMA_VERSION`: `1.0`
    - `AI_SCHEMA_KEY_MESSAGE_TYPE`: `TELEMETRY`
    - `AI_SCHEMA_KEY_DEVICE_TYPE_ID`: `MES`
    - `AI_SECURITY_SCAN_MOCK_CLEAN`: `true`
- `com.mes.ai.tools.HttpIngressSmokeRunner`: HTTP Ingress 통합 스모크 테스트
  - 실행 시 시스템 속성으로 포트/키를 변경 가능
    - `-Dai.http.port=8080` (기본 8080)
    - `-Dai.http.path=/ingest`
    - `-Dai.schema.key.schemaVersion=1.0`
    - `-Dai.schema.key.messageType=TELEMETRY`
    - `-Dai.schema.key.deviceTypeId=MES`

원격 저장소
- https://github.com/JikwangHan/MES-Platform_eGovFrame.git
