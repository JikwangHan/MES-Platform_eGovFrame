package com.mes.web.common.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * 목적: 권한 정책과 조회 로직을 제공한다.
 * 기능: 역할별 권한 맵을 생성하고 요청 경로에 필요한 권한을 계산한다.
 * 이유: 권한 기준을 서버에서 일관되게 확정하기 위함이다.
 * 유지보수: 역할/권한 정책 변경 시 이 클래스만 수정한다.
 */
@Service
public class PermissionService {

    private static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";
    private static final String ROLE_MANAGER = "MANAGER";
    private static final String ROLE_OPERATOR = "OPERATOR";
    private static final String ROLE_VIEWER = "VIEWER";

    /**
     * 목적: 역할 기준 권한 맵을 생성한다.
     * 기능: 메뉴/버튼/API 권한을 Map으로 반환한다.
     * 이유: 화면 렌더링과 API 접근 제어를 동일 기준으로 처리하기 위함이다.
     * 유지보수: 권한 정책 변경 시 기본 역할별 권한을 조정한다.
     */
    public Map<String, Boolean> buildPermissionMap(String role) {
        Map<String, Boolean> permissions = new HashMap<String, Boolean>();
        initializeAll(permissions);

        if (ROLE_SYSTEM_ADMIN.equalsIgnoreCase(role)) {
            grantAll(permissions);
            return permissions;
        }
        if (ROLE_MANAGER.equalsIgnoreCase(role)) {
            grantManager(permissions);
            return permissions;
        }
        if (ROLE_OPERATOR.equalsIgnoreCase(role)) {
            grantOperator(permissions);
            return permissions;
        }
        grantViewer(permissions);
        return permissions;
    }

    /**
     * 목적: 요청 경로에 필요한 권한 키를 결정한다.
     * 기능: 메뉴 화면과 API 경로를 권한 키로 매핑한다.
     * 이유: 필터에서 권한 판단을 단순화하기 위함이다.
     * 유지보수: 경로 변경 시 매핑 규칙을 보완한다.
     */
    public String resolvePermissionKey(String path, String method) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("/api/orders/")) {
            return resolveCrudPermission(path, PermissionKey.ACTION_ORDER_LIST, PermissionKey.ACTION_ORDER_CREATE,
                    PermissionKey.ACTION_ORDER_UPDATE, PermissionKey.ACTION_ORDER_DELETE);
        }
        if (path.startsWith("/api/work/")) {
            if (path.endsWith("/status")) {
                return PermissionKey.ACTION_WORK_STATUS;
            }
            return resolveCrudPermission(path, PermissionKey.ACTION_WORK_LIST, PermissionKey.ACTION_WORK_CREATE,
                    PermissionKey.ACTION_WORK_UPDATE, PermissionKey.ACTION_WORK_DELETE);
        }
        if (path.startsWith("/api/inventory/")) {
            return resolveCrudPermission(path, PermissionKey.ACTION_INVENTORY_LIST, PermissionKey.ACTION_INVENTORY_CREATE,
                    PermissionKey.ACTION_INVENTORY_UPDATE, PermissionKey.ACTION_INVENTORY_DELETE);
        }
        if (path.startsWith("/api/quality/defects/")) {
            if (path.endsWith("/delete")) {
                return PermissionKey.ACTION_QUALITY_DELETE;
            }
            if (path.endsWith("/create")) {
                return PermissionKey.ACTION_QUALITY_CREATE;
            }
            return PermissionKey.ACTION_QUALITY_LIST;
        }
        if (path.startsWith("/api/equipment/")) {
            return resolveCrudPermission(path, PermissionKey.ACTION_EQUIPMENT_LIST, PermissionKey.ACTION_EQUIPMENT_CREATE,
                    PermissionKey.ACTION_EQUIPMENT_UPDATE, PermissionKey.ACTION_EQUIPMENT_DELETE);
        }

        if (path.startsWith("/dashboard")) {
            return PermissionKey.MENU_DASHBOARD;
        }
        if (path.startsWith("/orders")) {
            return PermissionKey.MENU_ORDERS;
        }
        if (path.startsWith("/work")) {
            return PermissionKey.MENU_WORK;
        }
        if (path.startsWith("/inventory")) {
            return PermissionKey.MENU_INVENTORY;
        }
        if (path.startsWith("/quality")) {
            return PermissionKey.MENU_QUALITY;
        }
        if (path.startsWith("/equipment")) {
            return PermissionKey.MENU_EQUIPMENT;
        }
        if (path.startsWith("/admin")) {
            return PermissionKey.ACTION_ADMIN_VIEW;
        }
        return null;
    }

    /**
     * 목적: CRUD 경로에 대한 권한 키를 판별한다.
     * 기능: list/create/update/delete 경로를 각각의 키로 매핑한다.
     * 이유: API 권한 규칙을 일관되게 적용하기 위함이다.
     * 유지보수: API 경로 규칙 변경 시 이 메서드를 수정한다.
     */
    private String resolveCrudPermission(String path, String listKey, String createKey, String updateKey, String deleteKey) {
        if (path.endsWith("/list")) {
            return listKey;
        }
        if (path.endsWith("/create")) {
            return createKey;
        }
        if (path.endsWith("/update")) {
            return updateKey;
        }
        if (path.endsWith("/delete")) {
            return deleteKey;
        }
        return null;
    }

    /**
     * 목적: 모든 권한 키를 초기화한다.
     * 기능: 기본값을 false로 설정한다.
     * 이유: 누락 권한을 방지하고 명시적 허용만 가능하게 하기 위함이다.
     * 유지보수: 권한 키 추가 시 초기화 목록에 반영한다.
     */
    private void initializeAll(Map<String, Boolean> permissions) {
        permissions.put(PermissionKey.MENU_DASHBOARD, false);
        permissions.put(PermissionKey.MENU_ORDERS, false);
        permissions.put(PermissionKey.MENU_WORK, false);
        permissions.put(PermissionKey.MENU_INVENTORY, false);
        permissions.put(PermissionKey.MENU_QUALITY, false);
        permissions.put(PermissionKey.MENU_EQUIPMENT, false);
        permissions.put(PermissionKey.MENU_ADMIN, false);

        permissions.put(PermissionKey.ACTION_ORDER_LIST, false);
        permissions.put(PermissionKey.ACTION_ORDER_CREATE, false);
        permissions.put(PermissionKey.ACTION_ORDER_UPDATE, false);
        permissions.put(PermissionKey.ACTION_ORDER_DELETE, false);

        permissions.put(PermissionKey.ACTION_WORK_LIST, false);
        permissions.put(PermissionKey.ACTION_WORK_CREATE, false);
        permissions.put(PermissionKey.ACTION_WORK_UPDATE, false);
        permissions.put(PermissionKey.ACTION_WORK_STATUS, false);
        permissions.put(PermissionKey.ACTION_WORK_DELETE, false);

        permissions.put(PermissionKey.ACTION_INVENTORY_LIST, false);
        permissions.put(PermissionKey.ACTION_INVENTORY_CREATE, false);
        permissions.put(PermissionKey.ACTION_INVENTORY_UPDATE, false);
        permissions.put(PermissionKey.ACTION_INVENTORY_DELETE, false);

        permissions.put(PermissionKey.ACTION_QUALITY_LIST, false);
        permissions.put(PermissionKey.ACTION_QUALITY_CREATE, false);
        permissions.put(PermissionKey.ACTION_QUALITY_DELETE, false);

        permissions.put(PermissionKey.ACTION_EQUIPMENT_LIST, false);
        permissions.put(PermissionKey.ACTION_EQUIPMENT_CREATE, false);
        permissions.put(PermissionKey.ACTION_EQUIPMENT_UPDATE, false);
        permissions.put(PermissionKey.ACTION_EQUIPMENT_DELETE, false);

        permissions.put(PermissionKey.ACTION_ADMIN_VIEW, false);
    }

    /**
     * 목적: 시스템 관리자 권한을 부여한다.
     * 기능: 모든 권한을 true로 설정한다.
     * 이유: 최상위 관리자는 모든 메뉴와 API를 사용할 수 있어야 하기 위함이다.
     * 유지보수: 신규 권한 추가 시 initializeAll로 초기화되므로 별도 수정이 불필요하다.
     */
    private void grantAll(Map<String, Boolean> permissions) {
        for (String key : permissions.keySet()) {
            permissions.put(key, true);
        }
    }

    /**
     * 목적: 관리자 권한을 부여한다.
     * 기능: 운영 관리에 필요한 CRUD 권한을 허용한다.
     * 이유: 현장 관리자 역할을 기준으로 기본 권한을 제공하기 위함이다.
     * 유지보수: 역할 정책 변경 시 허용 범위를 조정한다.
     */
    private void grantManager(Map<String, Boolean> permissions) {
        grantMenuAll(permissions, false);
        grantOrders(permissions, true);
        grantWork(permissions, true);
        grantInventory(permissions, true);
        grantQuality(permissions, true);
        grantEquipment(permissions, true);
        permissions.put(PermissionKey.ACTION_ADMIN_VIEW, false);
    }

    /**
     * 목적: 작업자 권한을 부여한다.
     * 기능: 현장 작업에 필요한 조회/상태 변경 권한을 허용한다.
     * 이유: 현장 작업자 역할을 기준으로 필요한 기능만 제공하기 위함이다.
     * 유지보수: 작업 권한 범위 변경 시 이 메서드를 조정한다.
     */
    private void grantOperator(Map<String, Boolean> permissions) {
        grantMenuAll(permissions, false);
        grantOrders(permissions, false);
        grantWork(permissions, true);
        permissions.put(PermissionKey.ACTION_WORK_CREATE, false);
        permissions.put(PermissionKey.ACTION_WORK_UPDATE, false);
        permissions.put(PermissionKey.ACTION_WORK_DELETE, false);
        grantInventory(permissions, false);
        grantQuality(permissions, false);
        grantEquipment(permissions, false);
        permissions.put(PermissionKey.ACTION_ADMIN_VIEW, false);
    }

    /**
     * 목적: 조회 전용 권한을 부여한다.
     * 기능: 모든 화면의 조회 권한만 허용한다.
     * 이유: 감사/보고용 계정에 최소 권한만 부여하기 위함이다.
     * 유지보수: 조회 범위 변경 시 이 메서드를 조정한다.
     */
    private void grantViewer(Map<String, Boolean> permissions) {
        grantMenuAll(permissions, false);
        grantOrders(permissions, false);
        grantWork(permissions, false);
        grantInventory(permissions, false);
        grantQuality(permissions, false);
        grantEquipment(permissions, false);
        permissions.put(PermissionKey.ACTION_ADMIN_VIEW, false);
    }

    /**
     * 목적: 공통 메뉴 권한을 설정한다.
     * 기능: 기본 메뉴 표시 여부를 설정한다.
     * 이유: 메뉴 가시성을 역할별로 일관되게 적용하기 위함이다.
     * 유지보수: 메뉴 추가 시 이 메서드를 보완한다.
     */
    private void grantMenuAll(Map<String, Boolean> permissions, boolean includeAdmin) {
        permissions.put(PermissionKey.MENU_DASHBOARD, true);
        permissions.put(PermissionKey.MENU_ORDERS, true);
        permissions.put(PermissionKey.MENU_WORK, true);
        permissions.put(PermissionKey.MENU_INVENTORY, true);
        permissions.put(PermissionKey.MENU_QUALITY, true);
        permissions.put(PermissionKey.MENU_EQUIPMENT, true);
        permissions.put(PermissionKey.MENU_ADMIN, includeAdmin);
    }

    private void grantOrders(Map<String, Boolean> permissions, boolean allowEdit) {
        permissions.put(PermissionKey.ACTION_ORDER_LIST, true);
        permissions.put(PermissionKey.ACTION_ORDER_CREATE, allowEdit);
        permissions.put(PermissionKey.ACTION_ORDER_UPDATE, allowEdit);
        permissions.put(PermissionKey.ACTION_ORDER_DELETE, allowEdit);
    }

    private void grantWork(Map<String, Boolean> permissions, boolean allowEdit) {
        permissions.put(PermissionKey.ACTION_WORK_LIST, true);
        permissions.put(PermissionKey.ACTION_WORK_CREATE, allowEdit);
        permissions.put(PermissionKey.ACTION_WORK_UPDATE, allowEdit);
        permissions.put(PermissionKey.ACTION_WORK_DELETE, allowEdit);
        permissions.put(PermissionKey.ACTION_WORK_STATUS, true);
    }

    private void grantInventory(Map<String, Boolean> permissions, boolean allowEdit) {
        permissions.put(PermissionKey.ACTION_INVENTORY_LIST, true);
        permissions.put(PermissionKey.ACTION_INVENTORY_CREATE, allowEdit);
        permissions.put(PermissionKey.ACTION_INVENTORY_UPDATE, allowEdit);
        permissions.put(PermissionKey.ACTION_INVENTORY_DELETE, allowEdit);
    }

    private void grantQuality(Map<String, Boolean> permissions, boolean allowEdit) {
        permissions.put(PermissionKey.ACTION_QUALITY_LIST, true);
        permissions.put(PermissionKey.ACTION_QUALITY_CREATE, allowEdit);
        permissions.put(PermissionKey.ACTION_QUALITY_DELETE, allowEdit);
    }

    private void grantEquipment(Map<String, Boolean> permissions, boolean allowEdit) {
        permissions.put(PermissionKey.ACTION_EQUIPMENT_LIST, true);
        permissions.put(PermissionKey.ACTION_EQUIPMENT_CREATE, allowEdit);
        permissions.put(PermissionKey.ACTION_EQUIPMENT_UPDATE, allowEdit);
        permissions.put(PermissionKey.ACTION_EQUIPMENT_DELETE, allowEdit);
    }
}
