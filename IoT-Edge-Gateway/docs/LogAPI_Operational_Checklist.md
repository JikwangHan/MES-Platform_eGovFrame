IoT-Edge-Gateway Log API 운영 전환 체크리스트

1) 목적
- 실서버가 없는 환경에서도 Mock 검증을 완료하고, 실서버 확보 시 즉시 전환할 수 있도록 절차를 표준화합니다.

2) 전환 전 준비
- 실서버 Base URL 확보 (예: https://real.example.com)
- 인증키(crtfc_key) 확보
- use_se 값 확정 (DO6001~DO6999)
- 네트워크 방화벽/포트 허용 여부 확인

3) 설정 전환(필수)
- 파일: IoT-Edge-Gateway/config/config.json
- 변경 대상:
  - log_api.enabled = true
  - log_api.base_url = 실서버 URL
  - log_api.mode = "prod"
  - log_api.crtfc_key = 실제 키
  - log_api.use_se = 실제 use_se
  - log_api.source_ip = 고정 IP 또는 빈 값(자동)

4) 전환 후 검증(최소 1회)
- 게이트웨이 실행
- 로그 API 응답 코드 확인
  - 기대값: AP1002
- 오류 응답 시 즉시 중단하고 원인 분석

5) 실패 시 점검 순서
- URL 오타 여부
- 인증키 오류 여부
- 방화벽/포트 차단 여부
- 서버 가용성

6) 기록/증빙
- 테스트 일시, 담당자, 결과(PASS/FAIL/SKIP)를 PoC 보고서에 기록
