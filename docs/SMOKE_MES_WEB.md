MES Web Service 스모크 테스트 기록

목적
- DB 스키마/시드 적용 여부를 간단히 확인한다.
- 핵심 테이블 초기 데이터 존재 여부를 확인한다.

테스트 범위
- mes_default, mes_company_a DB의 기본 테이블 카운트 확인
- 대상 테이블: users, orders, work_orders, inventory_status, defects, equipments

실행 요약
- 결과: 두 테넌트 DB 모두 각 테이블 1건 이상 존재 확인
- 비고: 계정/비밀번호/로컬 경로는 기록하지 않음

결과(요약)
- mes_default: users=1, orders=1, work_orders=1, inventory_status=1, defects=1, equipments=1
- mes_company_a: users=1, orders=1, work_orders=1, inventory_status=1, defects=1, equipments=1

주의
- 본 문서는 스모크 테스트 요약만 기록한다.
- 상세 실행 로그/민감정보는 저장하지 않는다.
