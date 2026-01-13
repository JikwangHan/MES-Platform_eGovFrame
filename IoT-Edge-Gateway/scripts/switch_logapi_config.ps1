Param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("mock", "prod")]
    [string]$Mode
)

# 목적: log_api 설정을 mock/prod 템플릿으로 안전하게 교체합니다.
# 이유: 실서버 전환 시 수동 실수를 줄이고 반복 작업을 단순화합니다.

$repo = Split-Path -Parent $PSScriptRoot
$configPath = Join-Path $repo "config\config.json"
$backupPath = Join-Path $repo "config\config.json.bak"
$templatePath = Join-Path $repo ("config\config.logapi.{0}.json" -f $Mode)

if (-not (Test-Path $configPath)) {
    Write-Error "config.json이 존재하지 않습니다: $configPath"
    exit 1
}

if (-not (Test-Path $templatePath)) {
    Write-Error "템플릿 파일이 존재하지 않습니다: $templatePath"
    exit 1
}

# 1) 기존 설정 백업
Copy-Item -Path $configPath -Destination $backupPath -Force

# 2) config.json 로드
$configObj = Get-Content -Raw -Path $configPath | ConvertFrom-Json

# 3) 템플릿 log_api 로드
$templateObj = Get-Content -Raw -Path $templatePath | ConvertFrom-Json
if ($null -eq $templateObj.log_api) {
    Write-Error "템플릿에 log_api 블록이 없습니다."
    exit 1
}

# 4) log_api 블록 교체
$configObj | Add-Member -Force -NotePropertyName "log_api" -NotePropertyValue $templateObj.log_api

# 5) 저장
$configObj | ConvertTo-Json -Depth 6 | Set-Content -Path $configPath -Encoding UTF8

Write-Output "log_api 설정을 '$Mode' 템플릿으로 교체했습니다."
Write-Output "백업 파일: $backupPath"
