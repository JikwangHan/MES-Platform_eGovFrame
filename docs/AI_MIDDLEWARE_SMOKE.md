# AI Middleware 스모크 테스트 증빙

## 목적
- HTTP 수신 경로 기동과 요청 처리(health 성격의 호출)를 확인합니다.
- 파이프라인 저장/격리 흐름이 정상 동작하는지 확인합니다.

## 증빙 작성 규칙(필수)
- 로컬 경로, 계정, 토큰 등 민감정보는 문서에 기록하지 않습니다.
- 실행 환경(OS/JDK/아키텍처), 실행 명령, 결과 요약을 반드시 남깁니다.
- PASS/FAIL 근거는 수치로 기록합니다(예: 표준 저장 증가, 격리 증가).

## 실행 절차(복붙용)
1) 컴파일(빌드)
```powershell
mvn -f ai-middleware/pom.xml -DskipTests compile
```

2) 클래스패스 생성
```powershell
mvn -f ai-middleware/pom.xml -DincludeScope=runtime dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
```

3) HTTP Ingress 스모크 테스트(기동 + 호출)
```powershell
$deps = Get-Content -Raw ai-middleware/target/classpath.txt
$cp = "ai-middleware/target/classes;$deps"
java "-Dfile.encoding=UTF-8" "-Dai.security.scan.mockClean=true" -cp $cp com.mes.ai.tools.HttpIngressSmokeRunner
```

포트 충돌이 있으면 아래처럼 포트를 지정합니다.
```powershell
java "-Dfile.encoding=UTF-8" "-Dai.security.scan.mockClean=true" "-Dai.http.port=18080" -cp $cp com.mes.ai.tools.HttpIngressSmokeRunner
java "-Dfile.encoding=UTF-8" "-Dai.security.scan.mockClean=true" "-Dai.http.port=18081" -cp $cp com.mes.ai.tools.HttpIngressSmokeRunner
```

장비 샘플 케이스는 다른 deviceTypeId/messageType 조합으로 정상 저장을 확인합니다.
별칭 매핑 케이스는 표준 키 대신 별칭 키로 들어온 입력을 정규화하는지 확인합니다.
버전 형식 오류 케이스는 protocolVersion 규칙 위반이 격리되는지 확인합니다.
경고 케이스는 스키마 버전을 일부러 불일치시키며, 내부에서 warn 정책을 사용합니다.

## 결과(PASS 근거)
### 성공 케이스
- 응답 코드: 202
- 표준 저장 증가: 1
- Unknown Ingest 증가: 0

### 별칭 매핑 케이스
- 응답 코드: 202
- 표준 저장 증가: 1
- Unknown Ingest 증가: 0

### 장비 샘플 케이스
- 응답 코드: 202
- 표준 저장 증가: 1
- Unknown Ingest 증가: 0

### 장비 샘플 케이스-2
- 응답 코드: 202
- 표준 저장 증가: 1
- Unknown Ingest 증가: 0

### 버전 형식 오류 케이스
- 응답 코드: 202
- 표준 저장 증가: 0
- 격리 증가: 1

### 경고 케이스
- 응답 코드: 202
- 표준 저장 증가: 1
- Unknown Ingest 증가: 1

### 실패 케이스
- 응답 코드: 202
- 표준 저장 증가: 0
- 격리 증가: 1

## 증빙 기록 템플릿(복사해서 사용)
### 실행 환경
- OS:
- JDK:
- 아키텍처:
- 실행 일시:

### 실행 명령
```powershell
# 여기에 실제 실행 명령을 기록
```

### 결과 요약
- 성공 케이스:
- 별칭 매핑 케이스:
- 장비 샘플 케이스:
- 장비 샘플 케이스-2:
- 버전 형식 오류 케이스:
- 경고 케이스:
- 실패 케이스:

### PASS/FAIL 근거
- 표준 저장 증가:
- 격리 증가:
- Unknown Ingest 증가:

## 실행 기록(2026-01-17)
### 실행 환경
- OS: Microsoft Windows 11 Pro 10.0.26200 (64비트)
- JDK: OpenJDK Temurin 17.0.11+9
- 아키텍처: 64비트
- 실행 일시: 2026-01-17 16:18 (KST)

### 실행 명령
```powershell
$deps = Get-Content -Raw ai-middleware/target/classpath.txt
$cp = "ai-middleware/target/classes;$deps"
java "-Dfile.encoding=UTF-8" "-Dai.security.scan.mockClean=true" "-Dai.crypto.enabled=true" "-Dai.crypto.key.base64=<임시 키>" "-Dai.crypto.key.id=dev-key" "-Dai.crypto.key.version=v1" "-Dai.http.port=18080" -cp $cp com.mes.ai.tools.HttpIngressSmokeRunner
```

### 결과 요약
- 성공 케이스: 응답 202, 표준 저장 증가 1, 격리 증가 0, Unknown Ingest 증가 0
- 별칭 매핑 케이스: 응답 202, 표준 저장 증가 1, 격리 증가 0, Unknown Ingest 증가 0
- 장비 샘플 케이스: 응답 202, 표준 저장 증가 1, 격리 증가 0, Unknown Ingest 증가 0
- 장비 샘플 케이스-2: 응답 202, 표준 저장 증가 1, 격리 증가 0, Unknown Ingest 증가 0
- 버전 형식 오류 케이스: 응답 202, 표준 저장 증가 0, 격리 증가 1, Unknown Ingest 증가 0
- 경고 케이스: 응답 202, 표준 저장 증가 1, 격리 증가 0, Unknown Ingest 증가 1
- 실패 케이스: 응답 202, 표준 저장 증가 0, 격리 증가 1, Unknown Ingest 증가 0

### PASS/FAIL 근거
- 표준 저장 증가: 성공/별칭/장비/장비-2/경고 케이스 각 1
- 격리 증가: 버전 형식 오류 1, 실패 1
- Unknown Ingest 증가: 경고 1
