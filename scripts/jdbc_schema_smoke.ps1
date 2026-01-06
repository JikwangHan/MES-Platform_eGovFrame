# 목적: JDBC 스키마 키 조회/조회 테스트를 초보자도 쉽게 실행하도록 돕습니다.
# 기능: 빌드 -> 클래스패스 생성 -> 키 조회 -> 스키마 조회 테스트 순으로 진행합니다.
# 이유: DB 연결/스키마 조회가 정상인지 한 번에 확인하기 위함입니다.

param(
  [string]$JdbcUrl,
  [string]$JdbcUser,
  [string]$JdbcPassword,
  [string]$SchemaVersion,
  [string]$MessageType,
  [string]$DeviceTypeId
)

$ErrorActionPreference = "Stop"

# 목적: 콘솔/로그 한글 출력을 안정적으로 유지합니다.
# 이유: 테스트 결과가 깨지지 않도록 UTF-8로 출력 인코딩을 고정합니다.
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# 목적: 스크립트 기준으로 프로젝트 루트를 찾습니다.
# 이유: 경로를 고정해 어디서 실행해도 동일하게 동작하게 합니다.
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$modulePath = Join-Path $repoRoot "ai-middleware"

# 목적: 결과 로그 파일 경로를 생성합니다.
# 이유: 실행 결과를 저장해 사용자 테스트 기록에 활용합니다.
$logDir = Join-Path $repoRoot "logs"
if (-not (Test-Path $logDir)) {
  New-Item -ItemType Directory -Path $logDir | Out-Null
  Write-Host ("로그 폴더 생성: " + $logDir)
}
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $logDir ("jdbc_schema_smoke_" + $timestamp + ".log")

# 목적: JDBC 접속 정보를 환경 변수에서 읽습니다.
# 이유: 비밀번호를 코드에 하드코딩하지 않도록 하기 위함입니다.
$jdbcUrl = $JdbcUrl
if (-not $jdbcUrl) { $jdbcUrl = $env:AI_SCHEMA_JDBC_URL }
if (-not $jdbcUrl) { $jdbcUrl = "jdbc:mariadb://localhost:3306/mes-dev-test" }

$jdbcUser = $JdbcUser
if (-not $jdbcUser) { $jdbcUser = $env:AI_SCHEMA_JDBC_USER }
if (-not $jdbcUser) { $jdbcUser = "mes_user" }

$jdbcPassword = $JdbcPassword
if (-not $jdbcPassword) { $jdbcPassword = $env:AI_SCHEMA_JDBC_PASSWORD }
if (-not $jdbcPassword) {
  # 목적: 비밀번호를 안전하게 입력받습니다.
  # 이유: 화면 노출을 최소화하고, 입력 실수를 줄이기 위함입니다.
  $secure = Read-Host "DB 비밀번호를 입력하세요" -AsSecureString
  $jdbcPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
  )
}

# 목적: 스키마 키 조회 조건을 환경 변수에서 읽습니다.
# 이유: 운영/개발 환경에 따라 키 조합을 변경할 수 있게 합니다.
$schemaVersion = $SchemaVersion
if (-not $schemaVersion) { $schemaVersion = $env:AI_SCHEMA_KEY_SCHEMA_VERSION }
if (-not $schemaVersion) { $schemaVersion = "1.0" }
$messageType = $MessageType
if (-not $messageType) { $messageType = $env:AI_SCHEMA_KEY_MESSAGE_TYPE }
if (-not $messageType) { $messageType = "TELEMETRY" }
$deviceTypeId = $DeviceTypeId
if (-not $deviceTypeId) { $deviceTypeId = $env:AI_SCHEMA_KEY_DEVICE_TYPE_ID }
if (-not $deviceTypeId) { $deviceTypeId = "MES" }

Write-Host "[STEP 1/4] 소스 컴파일 확인" | Tee-Object -FilePath $logFile -Append
if (-not (Test-Path (Join-Path $modulePath "target\\classes"))) {
  $compileArgs = @(
    "-f", (Join-Path $modulePath "pom.xml"),
    "-DskipTests",
    "compile"
  )
  & mvn @compileArgs | Tee-Object -FilePath $logFile -Append
} else {
  Write-Host "컴파일 결과가 이미 존재합니다. (재컴파일 생략)" | Tee-Object -FilePath $logFile -Append
}

Write-Host "[STEP 2/4] 클래스패스 생성" | Tee-Object -FilePath $logFile -Append
$classpathFile = Join-Path $modulePath "target\\classpath.txt"
if (-not (Test-Path $classpathFile)) {
  $classpathArgs = @(
    "-f", (Join-Path $modulePath "pom.xml"),
    "-DincludeScope=runtime",
    "dependency:build-classpath",
    "-Dmdep.outputFile=$classpathFile"
  )
  & mvn @classpathArgs | Tee-Object -FilePath $logFile -Append
}

# 목적: 런타임 클래스패스를 구성합니다.
# 이유: 외부 라이브러리까지 포함해 실행해야 합니다.
$deps = Get-Content -Path $classpathFile -Raw
$classes = Join-Path $modulePath "target\\classes"
$fullClasspath = "$classes;$deps"

Write-Host "[STEP 3/4] 스키마 키 조회" | Tee-Object -FilePath $logFile -Append
$keyArgs = @(
  "-Dfile.encoding=UTF-8",
  "-Dai.schema.jdbc.url=$jdbcUrl",
  "-Dai.schema.jdbc.user=$jdbcUser",
  "-Dai.schema.jdbc.password=$jdbcPassword",
  "-cp", $fullClasspath,
  "com.mes.ai.tools.JdbcSchemaRegistryKeyLister"
)
& java @keyArgs | Tee-Object -FilePath $logFile -Append

Write-Host "[STEP 4/4] 스키마 단건 조회" | Tee-Object -FilePath $logFile -Append
$smokeArgs = @(
  "-Dfile.encoding=UTF-8",
  "-Dai.schema.jdbc.url=$jdbcUrl",
  "-Dai.schema.jdbc.user=$jdbcUser",
  "-Dai.schema.jdbc.password=$jdbcPassword",
  "-Dai.schema.key.schemaVersion=$schemaVersion",
  "-Dai.schema.key.messageType=$messageType",
  "-Dai.schema.key.deviceTypeId=$deviceTypeId",
  "-cp", $fullClasspath,
  "com.mes.ai.tools.JdbcSchemaRegistrySmokeRunner"
)
& java @smokeArgs | Tee-Object -FilePath $logFile -Append

Write-Host ("완료: 결과 로그 파일 -> " + $logFile) | Tee-Object -FilePath $logFile -Append
