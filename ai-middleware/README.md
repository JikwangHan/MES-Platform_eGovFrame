AI Middleware (eGovFrame 기반)

목적
- 제조장비/게이트웨이에서 들어오는 데이터를 수신하고, 표준화/검증/저장하는 역할을 담당합니다.
- 원본 데이터는 절대 수정하지 않고 그대로 보관하며, 검증된 데이터만 표준 DB에 저장합니다.

핵심 파이프라인(순서)
1) Ingress: 데이터 수신
2) Normalize: 포맷/키 정리
3) Classify: 장비/포맷 후보 추정
4) Validate: 값/범위/필수 필드 검증
5) Store: 표준 DB 저장
6) Quarantine: 실패 데이터 격리

구현 원칙
- 모든 코드 주석은 한국어로 작성합니다.
- 핵심 로직에는 목적/이유를 반드시 설명합니다.
- 멀티테넌트(기업별 DB 분리)를 전제로 합니다.

폴더 구조
- src/main/java/com/mes/ai/model: 메시지/데이터 모델
- src/main/java/com/mes/ai/pipeline: 파이프라인 단계 인터페이스
- src/main/java/com/mes/ai/service: 구현 서비스

참고 문서
- local_docs/S3_ERD_detail.md
- local_docs/S3_security_design.md
- local_docs/S3_logging_audit_design.md
- local_docs/S3_backup_restore_design.md
- local_docs/EdgeGateway_PoC_plan.md

CryptoService 설정(개발/PoC)
1) 암호화 활성화 키
   - ai.crypto.enabled=true
2) AES 키(Base64, 16 또는 32바이트)
   - ai.crypto.key.base64=<임시 키>
3) 키 식별자/버전
   - ai.crypto.key.id=dev-key
   - ai.crypto.key.version=v1
주의: 실제 키는 문서/로그/코드에 기록하지 않습니다.

CryptoService 스모크(암호화 활성)
```powershell
$deps = Get-Content -Raw ai-middleware/target/classpath.txt
$cp = "ai-middleware/target/classes;$deps"
java "-Dfile.encoding=UTF-8" "-Dai.crypto.enabled=true" "-Dai.crypto.key.base64=<임시 키>" "-Dai.crypto.key.id=dev-key" "-Dai.crypto.key.version=v1" -cp $cp com.mes.ai.tools.CryptoServiceSmokeRunner
```

HTTP Ingress 스모크(암호화 활성)
```powershell
$deps = Get-Content -Raw ai-middleware/target/classpath.txt
$cp = "ai-middleware/target/classes;$deps"
java "-Dfile.encoding=UTF-8" "-Dai.security.scan.mockClean=true" "-Dai.crypto.enabled=true" "-Dai.crypto.key.base64=<임시 키>" "-Dai.crypto.key.id=dev-key" "-Dai.crypto.key.version=v1" "-Dai.http.port=18080" -cp $cp com.mes.ai.tools.HttpIngressSmokeRunner
```
