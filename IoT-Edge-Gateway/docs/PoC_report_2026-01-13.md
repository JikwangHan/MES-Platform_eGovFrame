IoT-Edge-Gateway PoC 결과 보고서

1) 개요
- 수행 일자: 2026-01-13
- 수행 담당: Bigaeinohu
- 테스트 대상 장비/시뮬레이터: ModRSsim2 (Modbus TCP, 127.0.0.1:502)
- 테스트 주소 범위: Holding Regs 0~2 (0-based)

2) 실행 결과 요약
- Modbus Read: 성공
- 표준 메시지 생성: 성공
- 서버 수신: 성공 (MQTT mes/telemetry)
- 복구 전송: 성공 (is_recovered 포함 메시지 수신)
- 로그 API(Mock) 연동: 성공 (AP1002)

3) 상세 결과
테스트 항목 | 결과 | 비고
주소 읽기(1) | 성공 | Temperature (addr 0)
주소 읽기(2) | 성공 | Humidity (addr 1)
주소 읽기(3) | 성공 | Shot_Count (addr 2)
메시지 변환 | 성공 | version/device/timestamp/sensors 포함
전송/수신 | 성공 | mosquitto_sub 수신 확인
주기 검증(60s) | 성공 | Temperature/Humidity 수신(2026-01-13 14:19:09)
복구 전송 | 성공 | is_recovered=true 수신 확인
로그 API(Mock) | 성공 | AP1002 응답 확인

4) 오류 테스트
- 타임아웃: 성공 (address=10.255.255.1:502, i/o timeout 확인)
- 잘못된 주소: 성공(Timeout 기반) (address=9999, 시뮬레이터 특성상 timeout으로 확인)
- 인증 실패: 보류 (MQTT 인증 미사용 환경)
- MQTT 단절(Store & Forward): 성공 (DB 저장 및 복구 로그 확인)

5) 빌드/기동/헬스체크
- 빌드: 성공 (go build ./cmd/edge-gateway)
- 기동: 성공 (go run ./cmd/edge-gateway)
- 헬스체크: 성공 (GET http://localhost:18080/health)

6) 문제 및 개선 사항
- ModRSsim2는 범위 밖 주소에서 Illegal Address 대신 timeout으로 응답함.
- 복구 전송 메시지는 구독 타이밍에 따라 누락될 수 있어 DB 저장/복구 로그로 확인.

7) 다음 단계 제안
- MQTT 인증 실패 테스트 추가
- Illegal Address 응답이 가능한 시뮬레이터로 재검증
- 변경 사항 커밋 및 배포 절차 정리
