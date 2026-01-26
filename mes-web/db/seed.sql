-- 목적: 개발/테스트용 초기 데이터를 삽입한다.
-- 기능: 관리자 계정과 기본 기준 데이터를 제공한다.
-- 이유: 초기 화면 검증을 빠르게 하기 위함이다.
-- 유지보수: 운영 전 실제 데이터로 교체한다.

-- 주의: password_hash는 BCrypt 해시로 교체해야 한다.
-- 예: java BCryptPasswordEncoder로 생성한 해시 값을 입력한다.
INSERT INTO users (user_id, user_name, password_hash, role, status)
VALUES ('admin', '관리자', '$2a$10$23kDjTEajlF.pz3AJ6J.9OZVPCpb7e7XOfLagrQ45g33NRj2e9BOa', 'SYSTEM_ADMIN', 'active');

-- 역할 기본 데이터
INSERT INTO roles (role_code, role_name, role_desc, status) VALUES
  ('SYSTEM_ADMIN', '시스템 관리자', '전체 권한 관리자', 'active'),
  ('MANAGER', '관리자', '현장 관리자', 'active'),
  ('OPERATOR', '작업자', '현장 작업자', 'active'),
  ('VIEWER', '조회 전용', '조회 전용 계정', 'active')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_desc = VALUES(role_desc),
  status = VALUES(status);

-- 권한 카탈로그 기본 데이터
INSERT INTO permissions (perm_key, perm_label, perm_group) VALUES
  ('MENU_DASHBOARD', '대시보드 메뉴', '메뉴'),
  ('MENU_ORDERS', '수주 메뉴', '메뉴'),
  ('MENU_WORK', '작업 메뉴', '메뉴'),
  ('MENU_INVENTORY', '재고 메뉴', '메뉴'),
  ('MENU_QUALITY', '품질 메뉴', '메뉴'),
  ('MENU_EQUIPMENT', '설비 메뉴', '메뉴'),
  ('MENU_ADMIN', '관리 메뉴', '메뉴'),
  ('ACTION_ORDER_LIST', '수주 조회', '수주'),
  ('ACTION_ORDER_CREATE', '수주 등록', '수주'),
  ('ACTION_ORDER_UPDATE', '수주 수정', '수주'),
  ('ACTION_ORDER_DELETE', '수주 삭제', '수주'),
  ('ACTION_WORK_LIST', '작업 조회', '작업'),
  ('ACTION_WORK_CREATE', '작업 등록', '작업'),
  ('ACTION_WORK_UPDATE', '작업 수정', '작업'),
  ('ACTION_WORK_STATUS', '작업 상태 변경', '작업'),
  ('ACTION_WORK_DELETE', '작업 삭제', '작업'),
  ('ACTION_INVENTORY_LIST', '재고 조회', '재고'),
  ('ACTION_INVENTORY_CREATE', '재고 등록', '재고'),
  ('ACTION_INVENTORY_UPDATE', '재고 수정', '재고'),
  ('ACTION_INVENTORY_DELETE', '재고 삭제', '재고'),
  ('ACTION_QUALITY_LIST', '불량 조회', '품질'),
  ('ACTION_QUALITY_CREATE', '불량 등록', '품질'),
  ('ACTION_QUALITY_DELETE', '불량 삭제', '품질'),
  ('ACTION_EQUIPMENT_LIST', '설비 조회', '설비'),
  ('ACTION_EQUIPMENT_CREATE', '설비 등록', '설비'),
  ('ACTION_EQUIPMENT_UPDATE', '설비 수정', '설비'),
  ('ACTION_EQUIPMENT_DELETE', '설비 삭제', '설비'),
  ('ACTION_ADMIN_VIEW', '관리 화면 접근', '관리')
ON DUPLICATE KEY UPDATE
  perm_label = VALUES(perm_label),
  perm_group = VALUES(perm_group);

-- 역할별 권한 기본값
INSERT INTO role_permissions (role_code, perm_key, allowed) VALUES
  ('SYSTEM_ADMIN', 'MENU_DASHBOARD', 1),
  ('SYSTEM_ADMIN', 'MENU_ORDERS', 1),
  ('SYSTEM_ADMIN', 'MENU_WORK', 1),
  ('SYSTEM_ADMIN', 'MENU_INVENTORY', 1),
  ('SYSTEM_ADMIN', 'MENU_QUALITY', 1),
  ('SYSTEM_ADMIN', 'MENU_EQUIPMENT', 1),
  ('SYSTEM_ADMIN', 'MENU_ADMIN', 1),
  ('SYSTEM_ADMIN', 'ACTION_ORDER_LIST', 1),
  ('SYSTEM_ADMIN', 'ACTION_ORDER_CREATE', 1),
  ('SYSTEM_ADMIN', 'ACTION_ORDER_UPDATE', 1),
  ('SYSTEM_ADMIN', 'ACTION_ORDER_DELETE', 1),
  ('SYSTEM_ADMIN', 'ACTION_WORK_LIST', 1),
  ('SYSTEM_ADMIN', 'ACTION_WORK_CREATE', 1),
  ('SYSTEM_ADMIN', 'ACTION_WORK_UPDATE', 1),
  ('SYSTEM_ADMIN', 'ACTION_WORK_STATUS', 1),
  ('SYSTEM_ADMIN', 'ACTION_WORK_DELETE', 1),
  ('SYSTEM_ADMIN', 'ACTION_INVENTORY_LIST', 1),
  ('SYSTEM_ADMIN', 'ACTION_INVENTORY_CREATE', 1),
  ('SYSTEM_ADMIN', 'ACTION_INVENTORY_UPDATE', 1),
  ('SYSTEM_ADMIN', 'ACTION_INVENTORY_DELETE', 1),
  ('SYSTEM_ADMIN', 'ACTION_QUALITY_LIST', 1),
  ('SYSTEM_ADMIN', 'ACTION_QUALITY_CREATE', 1),
  ('SYSTEM_ADMIN', 'ACTION_QUALITY_DELETE', 1),
  ('SYSTEM_ADMIN', 'ACTION_EQUIPMENT_LIST', 1),
  ('SYSTEM_ADMIN', 'ACTION_EQUIPMENT_CREATE', 1),
  ('SYSTEM_ADMIN', 'ACTION_EQUIPMENT_UPDATE', 1),
  ('SYSTEM_ADMIN', 'ACTION_EQUIPMENT_DELETE', 1),
  ('SYSTEM_ADMIN', 'ACTION_ADMIN_VIEW', 1),
  ('MANAGER', 'MENU_DASHBOARD', 1),
  ('MANAGER', 'MENU_ORDERS', 1),
  ('MANAGER', 'MENU_WORK', 1),
  ('MANAGER', 'MENU_INVENTORY', 1),
  ('MANAGER', 'MENU_QUALITY', 1),
  ('MANAGER', 'MENU_EQUIPMENT', 1),
  ('MANAGER', 'MENU_ADMIN', 0),
  ('MANAGER', 'ACTION_ORDER_LIST', 1),
  ('MANAGER', 'ACTION_ORDER_CREATE', 1),
  ('MANAGER', 'ACTION_ORDER_UPDATE', 1),
  ('MANAGER', 'ACTION_ORDER_DELETE', 1),
  ('MANAGER', 'ACTION_WORK_LIST', 1),
  ('MANAGER', 'ACTION_WORK_CREATE', 1),
  ('MANAGER', 'ACTION_WORK_UPDATE', 1),
  ('MANAGER', 'ACTION_WORK_STATUS', 1),
  ('MANAGER', 'ACTION_WORK_DELETE', 1),
  ('MANAGER', 'ACTION_INVENTORY_LIST', 1),
  ('MANAGER', 'ACTION_INVENTORY_CREATE', 1),
  ('MANAGER', 'ACTION_INVENTORY_UPDATE', 1),
  ('MANAGER', 'ACTION_INVENTORY_DELETE', 1),
  ('MANAGER', 'ACTION_QUALITY_LIST', 1),
  ('MANAGER', 'ACTION_QUALITY_CREATE', 1),
  ('MANAGER', 'ACTION_QUALITY_DELETE', 1),
  ('MANAGER', 'ACTION_EQUIPMENT_LIST', 1),
  ('MANAGER', 'ACTION_EQUIPMENT_CREATE', 1),
  ('MANAGER', 'ACTION_EQUIPMENT_UPDATE', 1),
  ('MANAGER', 'ACTION_EQUIPMENT_DELETE', 1),
  ('MANAGER', 'ACTION_ADMIN_VIEW', 0),
  ('OPERATOR', 'MENU_DASHBOARD', 1),
  ('OPERATOR', 'MENU_ORDERS', 1),
  ('OPERATOR', 'MENU_WORK', 1),
  ('OPERATOR', 'MENU_INVENTORY', 1),
  ('OPERATOR', 'MENU_QUALITY', 1),
  ('OPERATOR', 'MENU_EQUIPMENT', 1),
  ('OPERATOR', 'MENU_ADMIN', 0),
  ('OPERATOR', 'ACTION_ORDER_LIST', 1),
  ('OPERATOR', 'ACTION_ORDER_CREATE', 0),
  ('OPERATOR', 'ACTION_ORDER_UPDATE', 0),
  ('OPERATOR', 'ACTION_ORDER_DELETE', 0),
  ('OPERATOR', 'ACTION_WORK_LIST', 1),
  ('OPERATOR', 'ACTION_WORK_CREATE', 0),
  ('OPERATOR', 'ACTION_WORK_UPDATE', 0),
  ('OPERATOR', 'ACTION_WORK_STATUS', 1),
  ('OPERATOR', 'ACTION_WORK_DELETE', 0),
  ('OPERATOR', 'ACTION_INVENTORY_LIST', 1),
  ('OPERATOR', 'ACTION_INVENTORY_CREATE', 0),
  ('OPERATOR', 'ACTION_INVENTORY_UPDATE', 0),
  ('OPERATOR', 'ACTION_INVENTORY_DELETE', 0),
  ('OPERATOR', 'ACTION_QUALITY_LIST', 1),
  ('OPERATOR', 'ACTION_QUALITY_CREATE', 0),
  ('OPERATOR', 'ACTION_QUALITY_DELETE', 0),
  ('OPERATOR', 'ACTION_EQUIPMENT_LIST', 1),
  ('OPERATOR', 'ACTION_EQUIPMENT_CREATE', 0),
  ('OPERATOR', 'ACTION_EQUIPMENT_UPDATE', 0),
  ('OPERATOR', 'ACTION_EQUIPMENT_DELETE', 0),
  ('OPERATOR', 'ACTION_ADMIN_VIEW', 0),
  ('VIEWER', 'MENU_DASHBOARD', 1),
  ('VIEWER', 'MENU_ORDERS', 1),
  ('VIEWER', 'MENU_WORK', 1),
  ('VIEWER', 'MENU_INVENTORY', 1),
  ('VIEWER', 'MENU_QUALITY', 1),
  ('VIEWER', 'MENU_EQUIPMENT', 1),
  ('VIEWER', 'MENU_ADMIN', 0),
  ('VIEWER', 'ACTION_ORDER_LIST', 1),
  ('VIEWER', 'ACTION_ORDER_CREATE', 0),
  ('VIEWER', 'ACTION_ORDER_UPDATE', 0),
  ('VIEWER', 'ACTION_ORDER_DELETE', 0),
  ('VIEWER', 'ACTION_WORK_LIST', 1),
  ('VIEWER', 'ACTION_WORK_CREATE', 0),
  ('VIEWER', 'ACTION_WORK_UPDATE', 0),
  ('VIEWER', 'ACTION_WORK_STATUS', 1),
  ('VIEWER', 'ACTION_WORK_DELETE', 0),
  ('VIEWER', 'ACTION_INVENTORY_LIST', 1),
  ('VIEWER', 'ACTION_INVENTORY_CREATE', 0),
  ('VIEWER', 'ACTION_INVENTORY_UPDATE', 0),
  ('VIEWER', 'ACTION_INVENTORY_DELETE', 0),
  ('VIEWER', 'ACTION_QUALITY_LIST', 1),
  ('VIEWER', 'ACTION_QUALITY_CREATE', 0),
  ('VIEWER', 'ACTION_QUALITY_DELETE', 0),
  ('VIEWER', 'ACTION_EQUIPMENT_LIST', 1),
  ('VIEWER', 'ACTION_EQUIPMENT_CREATE', 0),
  ('VIEWER', 'ACTION_EQUIPMENT_UPDATE', 0),
  ('VIEWER', 'ACTION_EQUIPMENT_DELETE', 0),
  ('VIEWER', 'ACTION_ADMIN_VIEW', 0)
ON DUPLICATE KEY UPDATE
  allowed = VALUES(allowed);

INSERT INTO partners (partner_code, partner_name, status)
VALUES ('P-0001', '샘플 거래처', 'active');

INSERT INTO items (item_code, item_name, category, item_type, unit, spec, unit_price, partner_id)
VALUES ('ITEM-0001', '샘플 품목', '기본', '완제품', 'EA', 'Spec', 1000.00, 1);

INSERT INTO factories (factory_name, region)
VALUES ('1공장', '서울');

INSERT INTO warehouses (factory_id, warehouse_name, location)
VALUES (1, '1창고', 'A-1');

INSERT INTO orders (order_no, order_date, due_date, item_id, order_qty, status, partner_id, owner_id)
VALUES ('ORDER-0001', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 1, 10, 'planned', 1, 1);

INSERT INTO work_orders (work_no, order_id, plan_start_date, plan_end_date, plan_qty, status, owner_id)
VALUES ('WORK-0001', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 10, 'planned', 1);

INSERT INTO inventory_status (item_id, warehouse_id, stock_qty, stock_type)
VALUES (1, 1, 100, 'normal');

INSERT INTO defect_types (parent_category, name, remark)
VALUES ('기본', '스크래치', '샘플 불량 유형');

INSERT INTO defects (defect_date, item_id, defect_type_id, defect_qty)
VALUES (CURDATE(), 1, 1, 2);

INSERT INTO equipments (equipment_code, equipment_name, equipment_type, status)
VALUES ('EQ-0001', '샘플 설비', 'PRESS', 'idle');

INSERT INTO equipment_status (equipment_id, status)
VALUES (1, 'idle');
