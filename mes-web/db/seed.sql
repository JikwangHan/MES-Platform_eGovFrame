-- 목적: 개발/테스트용 초기 데이터를 삽입한다.
-- 기능: 관리자 계정과 기본 기준 데이터를 제공한다.
-- 이유: 초기 화면 검증을 빠르게 하기 위함이다.
-- 유지보수: 운영 전 실제 데이터로 교체한다.

-- 주의: password_hash는 BCrypt 해시로 교체해야 한다.
-- 예: java BCryptPasswordEncoder로 생성한 해시 값을 입력한다.
INSERT INTO users (user_id, user_name, password_hash, role, status)
VALUES ('admin', '관리자', '$2a$10$23kDjTEajlF.pz3AJ6J.9OZVPCpb7e7XOfLagrQ45g33NRj2e9BOa', 'SYSTEM_ADMIN', 'active');

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
