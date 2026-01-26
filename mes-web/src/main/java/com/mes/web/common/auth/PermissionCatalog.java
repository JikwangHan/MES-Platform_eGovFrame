package com.mes.web.common.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 목적: 권한 매트릭스 화면용 카탈로그를 제공한다.
 * 기능: 권한 그룹과 항목 목록을 정리해 UI로 전달한다.
 * 이유: 권한 목록을 한 곳에서 관리해 화면과 정책을 일치시키기 위함이다.
 * 유지보수: 권한 항목 추가 시 이 카탈로그에 반영한다.
 */
public final class PermissionCatalog {

    /**
     * 목적: 권한 그룹 정보를 담는다.
     * 기능: 그룹명과 항목 목록을 보관한다.
     * 이유: 화면에서 카테고리별로 표시하기 위함이다.
     * 유지보수: 그룹 구조 변경 시 생성 로직을 조정한다.
     */
    public static class Group {
        private final String name;
        private final List<Item> items;

        public Group(String name, List<Item> items) {
            this.name = name;
            this.items = items;
        }

        public String getName() {
            return name;
        }

        public List<Item> getItems() {
            return items;
        }
    }

    /**
     * 목적: 권한 항목 정보를 담는다.
     * 기능: 권한 키와 설명을 보관한다.
     * 이유: 화면에서 권한 의미를 쉽게 표시하기 위함이다.
     * 유지보수: 표시 문구 변경 시 이 클래스 값을 수정한다.
     */
    public static class Item {
        private final String key;
        private final String label;

        public Item(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public String getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }
    }

    private PermissionCatalog() {
        // 유틸리티 클래스이므로 인스턴스 생성을 막는다.
    }

    /**
     * 목적: 권한 그룹 목록을 생성한다.
     * 기능: 메뉴/기능별 권한 항목을 그룹으로 반환한다.
     * 이유: 화면에 동일한 순서로 표시하기 위함이다.
     * 유지보수: 신규 권한 추가 시 이 목록을 보완한다.
     */
    public static List<Group> buildGroups() {
        List<Group> groups = new ArrayList<Group>();

        groups.add(new Group("메뉴", buildMenuItems()));
        groups.add(new Group("수주", buildOrderItems()));
        groups.add(new Group("작업", buildWorkItems()));
        groups.add(new Group("재고", buildInventoryItems()));
        groups.add(new Group("품질", buildQualityItems()));
        groups.add(new Group("설비", buildEquipmentItems()));
        groups.add(new Group("관리", buildAdminItems()));

        return groups;
    }

    private static List<Item> buildMenuItems() {
        List<Item> items = new ArrayList<Item>();
        items.add(new Item(PermissionKey.MENU_DASHBOARD, "대시보드 메뉴"));
        items.add(new Item(PermissionKey.MENU_ORDERS, "수주 메뉴"));
        items.add(new Item(PermissionKey.MENU_WORK, "작업 메뉴"));
        items.add(new Item(PermissionKey.MENU_INVENTORY, "재고 메뉴"));
        items.add(new Item(PermissionKey.MENU_QUALITY, "품질 메뉴"));
        items.add(new Item(PermissionKey.MENU_EQUIPMENT, "설비 메뉴"));
        items.add(new Item(PermissionKey.MENU_ADMIN, "관리 메뉴"));
        return Collections.unmodifiableList(items);
    }

    private static List<Item> buildOrderItems() {
        List<Item> items = new ArrayList<Item>();
        items.add(new Item(PermissionKey.ACTION_ORDER_LIST, "수주 조회"));
        items.add(new Item(PermissionKey.ACTION_ORDER_CREATE, "수주 등록"));
        items.add(new Item(PermissionKey.ACTION_ORDER_UPDATE, "수주 수정"));
        items.add(new Item(PermissionKey.ACTION_ORDER_DELETE, "수주 삭제"));
        return Collections.unmodifiableList(items);
    }

    private static List<Item> buildWorkItems() {
        List<Item> items = new ArrayList<Item>();
        items.add(new Item(PermissionKey.ACTION_WORK_LIST, "작업 조회"));
        items.add(new Item(PermissionKey.ACTION_WORK_CREATE, "작업 등록"));
        items.add(new Item(PermissionKey.ACTION_WORK_UPDATE, "작업 수정"));
        items.add(new Item(PermissionKey.ACTION_WORK_STATUS, "작업 상태 변경"));
        items.add(new Item(PermissionKey.ACTION_WORK_DELETE, "작업 삭제"));
        return Collections.unmodifiableList(items);
    }

    private static List<Item> buildInventoryItems() {
        List<Item> items = new ArrayList<Item>();
        items.add(new Item(PermissionKey.ACTION_INVENTORY_LIST, "재고 조회"));
        items.add(new Item(PermissionKey.ACTION_INVENTORY_CREATE, "재고 등록"));
        items.add(new Item(PermissionKey.ACTION_INVENTORY_UPDATE, "재고 수정"));
        items.add(new Item(PermissionKey.ACTION_INVENTORY_DELETE, "재고 삭제"));
        return Collections.unmodifiableList(items);
    }

    private static List<Item> buildQualityItems() {
        List<Item> items = new ArrayList<Item>();
        items.add(new Item(PermissionKey.ACTION_QUALITY_LIST, "불량 조회"));
        items.add(new Item(PermissionKey.ACTION_QUALITY_CREATE, "불량 등록"));
        items.add(new Item(PermissionKey.ACTION_QUALITY_DELETE, "불량 삭제"));
        return Collections.unmodifiableList(items);
    }

    private static List<Item> buildEquipmentItems() {
        List<Item> items = new ArrayList<Item>();
        items.add(new Item(PermissionKey.ACTION_EQUIPMENT_LIST, "설비 조회"));
        items.add(new Item(PermissionKey.ACTION_EQUIPMENT_CREATE, "설비 등록"));
        items.add(new Item(PermissionKey.ACTION_EQUIPMENT_UPDATE, "설비 수정"));
        items.add(new Item(PermissionKey.ACTION_EQUIPMENT_DELETE, "설비 삭제"));
        return Collections.unmodifiableList(items);
    }

    private static List<Item> buildAdminItems() {
        List<Item> items = new ArrayList<Item>();
        items.add(new Item(PermissionKey.ACTION_ADMIN_VIEW, "관리 화면 접근"));
        return Collections.unmodifiableList(items);
    }
}
