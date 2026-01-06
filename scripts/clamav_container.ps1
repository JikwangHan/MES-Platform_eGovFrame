# 목적: 로컬 개발 환경에서 ClamAV 컨테이너를 쉽게 관리합니다.
# 기능: 시작/중지/상태 확인/업데이트를 간단 명령으로 제공합니다.
# 이유: 실시간 감시 없이 필요한 순간에만 스캔 엔진을 사용하기 위함입니다.

param(
  [ValidateSet("start", "stop", "status", "update")]
  [string]$Action = "status"
)

$ErrorActionPreference = "Stop"

$containerName = "clamav"
$imageName = "clamav/clamav:latest"
$dbVolume = "clamav-db"

function Start-ClamAvContainer {
  Write-Host "ClamAV 컨테이너를 시작합니다."
  & docker run -d --name $containerName -p 3310:3310 -v "$dbVolume:/var/lib/clamav" $imageName
}

function Stop-ClamAvContainer {
  Write-Host "ClamAV 컨테이너를 중지/삭제합니다."
  & docker stop $containerName
  & docker rm $containerName
}

function Show-ClamAvStatus {
  Write-Host "ClamAV 컨테이너 상태를 확인합니다."
  & docker ps -a --filter "name=$containerName"
}

function Update-ClamAvSignatures {
  Write-Host "ClamAV 시그니처 업데이트를 수행합니다."
  & docker exec -it $containerName freshclam
}

switch ($Action) {
  "start" { Start-ClamAvContainer }
  "stop" { Stop-ClamAvContainer }
  "status" { Show-ClamAvStatus }
  "update" { Update-ClamAvSignatures }
}
