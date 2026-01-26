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

추가 시나리오(서버 기동 후)
- 수주: /api/orders/list, /api/orders/create, /api/orders/update, /api/orders/delete
- 작업: /api/work/list, /api/work/create, /api/work/update, /api/work/status, /api/work/delete
- 재고: /api/inventory/list, /api/inventory/create, /api/inventory/update, /api/inventory/delete
- 품질: /api/quality/defects/list, /api/quality/defects/create, /api/quality/defects/delete
- 설비: /api/equipment/list, /api/equipment/create, /api/equipment/update, /api/equipment/delete
- 역할: /admin/roles 접근, /admin/roles/create, /admin/roles/delete
- 권한: SYSTEM_ADMIN에서 관리 메뉴 권한 해제 시 차단 메시지 확인

실행 결과
- API 스모크(조회)는 서버 기동 후 수행 완료
- 결과 요약: 수주/작업/재고/품질/설비 조회 응답 result=success 확인

CRUD 스모크(서버 기동 후)
- 결과 요약: 수주/작업/재고/품질/설비 create/update/delete 응답 result=success 확인
- 비고: 테스트용 데이터는 생성 후 즉시 정리함

CRUD 스모크(companyA 테넌트)
- 결과 요약: companyA 테넌트에서 수주/작업/재고/품질/설비 create/update/delete 응답 result=success 확인
- 비고: 테스트용 데이터는 생성 후 즉시 정리함

권한 스모크(서버 기동 후)
- 결과 요약: 관리자 계정은 관리자 화면 접근 가능(200), 조회 전용 계정은 관리자 화면 접근 차단(403) 확인
- 결과 요약: 조회 전용 계정은 목록 조회 가능(200), 생성 요청 차단(403) 확인

감사 로그 스모크(서버 기동 후)
- 결과 요약: 감사 로그에 ip_hash/user_agent_hash/detail_hash 값 저장 확인

권한 매트릭스 화면 스모크(서버 기동 후)
- 결과 요약: 관리자 권한에서 /admin/permissions 접근 200 확인, 조회 전용 계정 접근 403 확인

조회 조건 검증 스모크(서버 기동 후)
- 결과 요약: 잘못된 날짜(YYYY-99-99) 입력 시 result=fail 응답 확인

권한 저장 스모크(서버 기동 후)
- 결과 요약: 관리자 권한으로 VIEWER 권한 저장 요청 302 확인
