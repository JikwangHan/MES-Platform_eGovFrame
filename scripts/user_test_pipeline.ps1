# 목적: AI Middleware 파이프라인 스모크 테스트를 초보자도 쉽게 실행하도록 돕습니다.
# 기능: 빌드 -> 클래스패스 생성 -> 테스트 실행 순서를 자동으로 처리합니다.
# 이유: 수동 명령 입력 실수를 줄이고, 빠르게 결과를 확인하기 위함입니다.

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
$logFile = Join-Path $logDir ("pipeline_smoke_" + $timestamp + ".log")

# 목적: 스크립트 기준으로 프로젝트 루트를 찾습니다.
# 이유: 경로를 고정해 어디서 실행해도 동일하게 동작하게 합니다.
Write-Host "[STEP 1/3] 소스 컴파일 시작" | Tee-Object -FilePath $logFile -Append
$compileArgs = @(
  "-f", (Join-Path $modulePath "pom.xml"),
  "-DskipTests",
  "compile"
)
& mvn @compileArgs | Tee-Object -FilePath $logFile -Append

Write-Host "[STEP 2/3] 클래스패스 생성" | Tee-Object -FilePath $logFile -Append
$classpathFile = Join-Path $modulePath "target\\classpath.txt"
$classpathArgs = @(
  "-f", (Join-Path $modulePath "pom.xml"),
  "-DincludeScope=runtime",
  "dependency:build-classpath",
  "-Dmdep.outputFile=$classpathFile"
)
& mvn @classpathArgs | Tee-Object -FilePath $logFile -Append

# 목적: 런타임 클래스패스를 구성합니다.
# 이유: 외부 라이브러리까지 포함해 실행해야 합니다.
$deps = Get-Content -Path $classpathFile -Raw
$classes = Join-Path $modulePath "target\\classes"
$fullClasspath = "$classes;$deps"

Write-Host "[STEP 3/3] 파이프라인 스모크 테스트 실행" | Tee-Object -FilePath $logFile -Append
# 목적: 보안 스캔 구현체를 환경에 따라 선택합니다.
# 이유: ClamAV가 설치된 환경에서는 실제 스캔을 수행할 수 있습니다.
$useClamAv = $env:AI_USE_CLAMAV
if ($useClamAv -and $useClamAv.ToLower() -eq "true") {
  Write-Host "MODE: CLAMAV" | Tee-Object -FilePath $logFile -Append
  Write-Host "ClamAV 스캔 모드로 실행합니다." | Tee-Object -FilePath $logFile -Append
  # 목적: clamscan 실행 파일 존재 여부를 확인합니다.
  # 이유: 미설치 상태에서 오류가 나지 않도록 안내합니다.
  if (-not (Get-Command clamscan -ErrorAction SilentlyContinue)) {
    $msg = "ClamAV(clamscan)을 찾을 수 없습니다. AI_USE_CLAMAV 설정을 해제하거나 ClamAV를 설치하세요."
    Write-Host $msg | Tee-Object -FilePath $logFile -Append
    exit 1
  }
  $javaArgs = @(
    "-Dfile.encoding=UTF-8",
    "-Dai.security.scan.impl=clamav",
    "-Dai.security.scan.command=clamscan",
    "-cp", $fullClasspath,
    "com.mes.ai.tools.PipelineSmokeRunner"
  )
  & java @javaArgs | Tee-Object -FilePath $logFile -Append
} else {
  Write-Host "MODE: MOCK" | Tee-Object -FilePath $logFile -Append
  Write-Host "메모리 스캔 모드로 실행합니다. (모의 CLEAN)" | Tee-Object -FilePath $logFile -Append
  $javaArgs = @(
    "-Dfile.encoding=UTF-8",
    "-Dai.security.scan.mockClean=true",
    "-Dai.security.scan.impl=inmemory",
    "-cp", $fullClasspath,
    "com.mes.ai.tools.PipelineSmokeRunner"
  )
  & java @javaArgs | Tee-Object -FilePath $logFile -Append
}

Write-Host ("완료: 결과 로그 파일 -> " + $logFile) | Tee-Object -FilePath $logFile -Append
