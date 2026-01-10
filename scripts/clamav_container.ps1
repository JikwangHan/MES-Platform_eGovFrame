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
  & docker run -d --name $containerName -p 3310:3310 -v "${dbVolume}:/var/lib/clamav" $imageName
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
  # 목적: 콘솔 TTY 없는 환경에서도 업데이트가 실행되도록 합니다.
  # 이유: 자동화/로그 기반 실행에서는 -it 옵션이 실패할 수 있습니다.
  # 목적: 컨테이너 내부에서 freshclam 데몬이 이미 동작 중인지 확인합니다.
  # 이유: 동작 중이면 로그 잠금으로 업데이트가 실패할 수 있습니다.
  $freshclamRunning = & docker exec -i $containerName sh -lc "pgrep -x freshclam >/dev/null && echo RUNNING || echo STOPPED"
  if ($freshclamRunning -match "RUNNING") {
    Write-Host "freshclam 데몬이 이미 실행 중입니다. (자동 업데이트 중)"
    Write-Host "필요 시 컨테이너를 재시작한 뒤 다시 업데이트를 시도하세요."
    return
  }
  & docker exec -i $containerName freshclam --foreground --stdout
}

switch ($Action) {
  "start" { Start-ClamAvContainer }
  "stop" { Stop-ClamAvContainer }
  "status" { Show-ClamAvStatus }
  "update" { Update-ClamAvSignatures }
}
