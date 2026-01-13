# AI Middleware Viewer 안내

## 목적
- 파이프라인 처리 결과(표준/격리/Unknown)를 브라우저에서 확인합니다.
- 운영 로그 표준(decision/reasonCode)을 UI에서 빠르게 점검합니다.

## 실행 방법(예시)
Viewer 실행은 `ViewerServiceRunner` 기준으로 동작합니다.
```powershell
java -cp "ai-middleware/target/classes;$((Get-Content -Raw ai-middleware/target/classpath.txt))" com.mes.ai.viewer.ViewerServiceRunner
```

## API 목록
- `/api/summary`: 원본/표준/격리/Unknown 카운트
- `/api/raw`: 원본 데이터 목록
- `/api/standard`: 표준 데이터 목록
- `/api/quarantine`: 격리 데이터 목록
- `/api/unknown`: Unknown Ingest 목록
- `/api/decisions`: decision/reasonCode 요약 목록

## /api/decisions 필드
- decision: STANDARD | QUARANTINE | UNKNOWN
- reasonCode: 사유 코드(ValidationResult/Unknown 사유 기반)
- reasonDetail: 상세 사유(없으면 "-")
- rawId/recordId: 원본 또는 Unknown 레코드 식별자
- ingressType/contentType: 수신 경로/콘텐츠 타입
