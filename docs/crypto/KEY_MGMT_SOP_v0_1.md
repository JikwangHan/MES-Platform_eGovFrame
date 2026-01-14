KEY_MGMT_SOP_v0_1

목적
- 키 회전/승인/폐기/재암호화/분기 점검 절차를 표준화합니다.

확정 정책(초안)
- 회전 주기: 1년(운영 환경에 맞게 조정)
- 승인 방식: SYSTEM_ADMIN + 2인 승인(2-man rule)
- 유출 대응: 즉시 폐기 → 신규 발급 → 재암호화
- 점검 주기: 분기 1회

워크플로
1) 회전(ROTATE)
- ROTATE 요청 생성
- 2인 승인 완료
- 신규 키버전 Active, 기존 키버전 Retired(복호만 허용)

2) 유출 대응(REVOKE)
- 즉시 Revoked 처리
- 신규 키 발급
- 범위별 재암호화 배치 실행

3) 분기 점검
- key usage/실패율/nonce 위반 리포트 생성
- 관리자 보고 및 보관

최소 DB 스키마
- key_version: key_id, key_version, status(Active/Retired/Revoked), created_at, rotate_due_at, alg, mode, notes
- key_request: request_id, type(ROTATE/REVOKE/REKEY), requester, reason, scope, created_at, status
- key_approval: request_id, approver, decision, decided_at
- crypto_audit_log: event_type, key_id, key_version, success/fail, fail_reason, actor, timestamp, trace_id

운영 원칙
- 키 평문 저장 금지
- 키/민감정보 로그 금지
- 개발/PoC 단계는 무료 구현, 상용화 단계에서 검증필 모듈로 교체
 - 개발 단계는 HSM 인터페이스만 유지, 실제 장비 연동 비활성

HSM 적용 범위(패턴 C)
- 공통: KEK/CMK, 서명키, 키 래핑/언래핑, 난수(키 생성), 회전/폐기 이력 보관
- AI Middleware: 토큰/세션키, API 연계 서명키, 서비스 간 메시지 서명 루트 키
- MES Web Service: DB DEK 래핑 보관, 감사로그 서명키
- IoT-Edge-Gateway: 기본값 HSM 미적용(패턴 A), 필요 시 디바이스 키만 H/W 관리

HSM 장애 정책(확정)
- 원칙: Fail-Closed
- 권고: 이중화 + 자동 Failover + 헬스체크 + 운영자 알림
- 장애 시:
  1) 신규 키 발급/회전/재암호화 중지
  2) 신규 암호화 요청은 큐 적재 후 재시도(예: 5분 간격, 최대 1시간)
  3) 기존 데이터 복호화는 단기 캐시 DEK 범위에서만 제한 허용
