# 승인 이력 테이블 구성 가이드

## 목적
- 승인/반려 이력을 별도 테이블로 저장하여 감사/조회 기능을 제공한다.
- 사유 텍스트는 암호화 저장한다.

## 활성화 방법
- 시스템 속성: mes.approval.history.enabled=true
- 환경 변수: MES_APPROVAL_HISTORY_ENABLED=true

## 테이블 생성 SQL (MariaDB)
```sql
CREATE TABLE IF NOT EXISTS user_approval_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id VARCHAR(50) NOT NULL,
  action_type VARCHAR(20) NOT NULL,
  reason_code VARCHAR(50) NULL,
  reason_text_enc TEXT NULL,
  actor_user_id VARCHAR(50) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_approval_history_user (user_id),
  KEY idx_approval_history_action (action_type),
  KEY idx_approval_history_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 운영 메모
- reason_text_enc 컬럼은 암호화된 문자열을 저장한다.
- 복호화 실패 시 화면에는 “복호화 실패”로 표시된다.
- 테이블 생성 전에 DB 권한과 스키마(테넌트) 분리를 확인한다.
