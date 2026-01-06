# 목적: HTTP Ingress 스모크 테스트를 초보자도 쉽게 실행하도록 돕습니다.
# 기능: 포트/경로/키를 환경 변수 또는 파라미터로 받아 테스트를 실행합니다.
# 이유: 개발/운영 환경에서 포트 변경을 쉽게 적용하기 위함입니다.

param(
  [int]$Port,
  [string]$Path,
  [string]$SchemaVersion,
  [string]$MessageType,
  [string]$DeviceTypeId,
  [string]$MockClean
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
$logFile = Join-Path $logDir ("http_ingress_smoke_" + $timestamp + ".log")

# 목적: 포트/경로/키 값을 우선순위에 따라 설정합니다.
# 이유: 파라미터/환경 변수/기본값 중에서 가장 적절한 값을 사용하기 위함입니다.
$portValue = $Port
if (-not $portValue) { $portValue = $env:AI_HTTP_PORT }
if (-not $portValue) { $portValue = 8080 }

$pathValue = $Path
if (-not $pathValue) { $pathValue = $env:AI_HTTP_PATH }
if (-not $pathValue) { $pathValue = "/ingest" }

$schemaVersionValue = $SchemaVersion
if (-not $schemaVersionValue) { $schemaVersionValue = $env:AI_SCHEMA_KEY_SCHEMA_VERSION }
if (-not $schemaVersionValue) { $schemaVersionValue = "1.0" }

$messageTypeValue = $MessageType
if (-not $messageTypeValue) { $messageTypeValue = $env:AI_SCHEMA_KEY_MESSAGE_TYPE }
if (-not $messageTypeValue) { $messageTypeValue = "TELEMETRY" }

$deviceTypeIdValue = $DeviceTypeId
if (-not $deviceTypeIdValue) { $deviceTypeIdValue = $env:AI_SCHEMA_KEY_DEVICE_TYPE_ID }
if (-not $deviceTypeIdValue) { $deviceTypeIdValue = "MES" }

$mockCleanValue = $MockClean
if (-not $mockCleanValue) { $mockCleanValue = $env:AI_SECURITY_SCAN_MOCK_CLEAN }
if (-not $mockCleanValue) { $mockCleanValue = "true" }

Write-Host "[STEP 1/2] 클래스패스 확인" | Tee-Object -FilePath $logFile -Append
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

$deps = Get-Content -Path $classpathFile -Raw
$classes = Join-Path $modulePath "target\\classes"
$fullClasspath = "$classes;$deps"

Write-Host "[STEP 2/2] HTTP Ingress 스모크 테스트 실행" | Tee-Object -FilePath $logFile -Append
$javaArgs = @(
  "-Dfile.encoding=UTF-8",
  "-Dai.security.scan.mockClean=$mockCleanValue",
  "-Dai.http.port=$portValue",
  "-Dai.http.path=$pathValue",
  "-Dai.schema.key.schemaVersion=$schemaVersionValue",
  "-Dai.schema.key.messageType=$messageTypeValue",
  "-Dai.schema.key.deviceTypeId=$deviceTypeIdValue",
  "-cp", $fullClasspath,
  "com.mes.ai.tools.HttpIngressSmokeRunner"
)
& java @javaArgs | Tee-Object -FilePath $logFile -Append

Write-Host ("완료: 결과 로그 파일 -> " + $logFile) | Tee-Object -FilePath $logFile -Append
