package com.mes.web.common.auth;

/**
 * 목적: 권한 키 상수를 정의한다.
 * 기능: 메뉴/버튼/API 권한 식별자를 일관되게 제공한다.
 * 이유: 하드코딩 문자열을 줄여 유지보수를 쉽게 하기 위함이다.
 * 유지보수: 권한 항목 추가 시 이 클래스에 상수를 추가한다.
 */
public final class PermissionKey {

    public static final String MENU_DASHBOARD = "MENU_DASHBOARD";
    public static final String MENU_ORDERS = "MENU_ORDERS";
    public static final String MENU_WORK = "MENU_WORK";
    public static final String MENU_INVENTORY = "MENU_INVENTORY";
    public static final String MENU_QUALITY = "MENU_QUALITY";
    public static final String MENU_EQUIPMENT = "MENU_EQUIPMENT";
    public static final String MENU_ADMIN = "MENU_ADMIN";

    public static final String ACTION_ORDER_LIST = "ACTION_ORDER_LIST";
    public static final String ACTION_ORDER_CREATE = "ACTION_ORDER_CREATE";
    public static final String ACTION_ORDER_UPDATE = "ACTION_ORDER_UPDATE";
    public static final String ACTION_ORDER_DELETE = "ACTION_ORDER_DELETE";

    public static final String ACTION_WORK_LIST = "ACTION_WORK_LIST";
    public static final String ACTION_WORK_CREATE = "ACTION_WORK_CREATE";
    public static final String ACTION_WORK_UPDATE = "ACTION_WORK_UPDATE";
    public static final String ACTION_WORK_STATUS = "ACTION_WORK_STATUS";
    public static final String ACTION_WORK_DELETE = "ACTION_WORK_DELETE";

    public static final String ACTION_INVENTORY_LIST = "ACTION_INVENTORY_LIST";
    public static final String ACTION_INVENTORY_CREATE = "ACTION_INVENTORY_CREATE";
    public static final String ACTION_INVENTORY_UPDATE = "ACTION_INVENTORY_UPDATE";
    public static final String ACTION_INVENTORY_DELETE = "ACTION_INVENTORY_DELETE";

    public static final String ACTION_QUALITY_LIST = "ACTION_QUALITY_LIST";
    public static final String ACTION_QUALITY_CREATE = "ACTION_QUALITY_CREATE";
    public static final String ACTION_QUALITY_DELETE = "ACTION_QUALITY_DELETE";

    public static final String ACTION_EQUIPMENT_LIST = "ACTION_EQUIPMENT_LIST";
    public static final String ACTION_EQUIPMENT_CREATE = "ACTION_EQUIPMENT_CREATE";
    public static final String ACTION_EQUIPMENT_UPDATE = "ACTION_EQUIPMENT_UPDATE";
    public static final String ACTION_EQUIPMENT_DELETE = "ACTION_EQUIPMENT_DELETE";

    public static final String ACTION_ADMIN_VIEW = "ACTION_ADMIN_VIEW";

    private PermissionKey() {
        // 유틸리티 클래스이므로 인스턴스 생성을 막는다.
    }
}
