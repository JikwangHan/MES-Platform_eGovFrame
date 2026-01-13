IoT-Edge-Gateway Log API 운영 전환 리허설 가이드

1) 목적
- 실서버가 없는 환경에서 Mock 기반으로 운영 전환 절차를 연습하고, 실제 서버 전환 시 실수를 줄입니다.

2) 준비 사항
- Mock 로그 API 실행 가능 상태
- 설정 템플릿 파일 존재
  - config/config.logapi.mock.json
  - config/config.logapi.prod.example.json

3) 리허설 절차(추천 순서)
- 1단계: 현재 설정 백업
  - config/config.json을 별도 위치에 복사합니다.
- 2단계: Mock 템플릿 적용
  - config/config.logapi.mock.json의 log_api 블록을 config.json에 반영합니다.
- 3단계: Mock 서버 실행
  - scripts/mock_log_api.go를 실행합니다.
- 4단계: 게이트웨이 실행
  - MQTT 정상 전송 후 로그 API 전송 성공(AP1002) 확인.
- 5단계: 운영 템플릿 적용 리허설
  - config/config.logapi.prod.example.json의 log_api 블록을 config.json에 반영합니다.
  - 실서버가 없으므로 전송 결과는 SKIP으로 기록합니다.
- 6단계: 원상 복구
  - 1단계 백업본으로 config.json을 복원합니다.

4) 기록 기준
- Mock 전송 결과: PASS 또는 FAIL
- 운영 전환 결과: SKIP (실서버 부재)

5) 다음 단계
- 실서버 확보 후 5단계 재실행
- 운영 인증키/URL 확정 및 문서 갱신
