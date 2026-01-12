# AI Middleware 스모크 테스트 증빙

## 목적
- HTTP 수신 경로 기동과 요청 처리(health 성격의 호출)를 확인합니다.
- 파이프라인 저장/격리 흐름이 정상 동작하는지 확인합니다.

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

경고 케이스는 스키마 버전을 일부러 불일치시키며, 내부에서 warn 정책을 사용합니다.

## 결과(PASS 근거)
### 성공 케이스
- 응답 코드: 202
- 표준 저장 증가: 1
- Unknown Ingest 증가: 0

### 경고 케이스
- 응답 코드: 202
- 표준 저장 증가: 1
- Unknown Ingest 증가: 1

### 실패 케이스
- 응답 코드: 202
- 표준 저장 증가: 0
- 격리 증가: 1
