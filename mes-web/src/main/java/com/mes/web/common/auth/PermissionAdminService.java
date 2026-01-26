package com.mes.web.common.auth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mes.web.dao.PermissionDao;
import com.mes.web.dao.RoleDao;
import com.mes.web.common.audit.AuditLogService;

/**
 * 목적: 권한 매트릭스 관리 기능을 제공한다.
 * 기능: 역할별 권한 저장과 검증을 수행한다.
 * 이유: 관리자 화면에서 권한을 서버 기준으로 저장하기 위함이다.
 * 유지보수: 권한 정책 변경 시 검증 로직을 보완한다.
 */
@Service
public class PermissionAdminService {

    private static final Set<String> PROTECTED_ROLES = new HashSet<String>();
    private static final String REQUIRED_ADMIN_PERMISSION = PermissionKey.MENU_ADMIN;

    static {
        PROTECTED_ROLES.add("SYSTEM_ADMIN");
    }

    private final PermissionDao permissionDao;
    private final RoleDao roleDao;
    private final PermissionRepository permissionRepository;
    private final AuditLogService auditLogService;

    /**
     * 목적: 권한 DAO를 주입받는다.
     * 기능: 역할 권한 저장을 DAO로 위임한다.
     * 이유: DB 저장 로직을 서비스와 분리하기 위함이다.
     * 유지보수: DAO 교체 시 주입만 변경한다.
     */
    @Autowired
    public PermissionAdminService(PermissionDao permissionDao, RoleDao roleDao,
                                  PermissionRepository permissionRepository, AuditLogService auditLogService) {
        this.permissionDao = permissionDao;
        this.roleDao = roleDao;
        this.permissionRepository = permissionRepository;
        this.auditLogService = auditLogService;
    }

    /**
     * 목적: 역할 권한을 저장한다.
     * 기능: 유효성 검증 후 권한을 삭제/삽입한다.
     * 이유: 권한 정책을 DB에 확정하기 위함이다.
     * 유지보수: 역할/권한 정책 변경 시 검증을 보완한다.
     */
    public String saveRolePermissions(String roleCode, List<String> selectedKeys, String userId) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            return "역할 코드가 비어 있습니다.";
        }
        String normalizedRole = roleCode.trim().toUpperCase();
        List<Map<String, Object>> roles = roleDao.selectRoles();
        if (roles == null) {
            return "역할 목록을 불러오지 못했습니다.";
        }
        boolean roleExists = false;
        for (Map<String, Object> role : roles) {
            Object code = role.get("roleCode");
            if (code != null && normalizedRole.equals(code.toString())) {
                roleExists = true;
                break;
            }
        }
        if (!roleExists) {
            return "등록되지 않은 역할입니다.";
        }

        Set<String> allowedKeys = PermissionCatalog.buildAllKeys();
        Map<String, Boolean> permissions = new HashMap<String, Boolean>();
        for (String key : allowedKeys) {
            permissions.put(key, false);
        }
        if (selectedKeys != null) {
            for (String key : selectedKeys) {
                if (key != null && allowedKeys.contains(key)) {
                    permissions.put(key, true);
                }
            }
        }

        if (PROTECTED_ROLES.contains(normalizedRole) && !Boolean.TRUE.equals(permissions.get(REQUIRED_ADMIN_PERMISSION))) {
            auditLogService.logEvent("permission_update", "fail", userId,
                    "role=" + normalizedRole + ",reason=protected_role_missing_admin_permission");
            return "SYSTEM_ADMIN 역할은 관리 메뉴 권한을 유지해야 합니다.";
        }

        Map<String, Boolean> beforePermissions = permissionRepository.loadRolePermissions(normalizedRole);
        permissionDao.deleteRolePermissions(normalizedRole);
        permissionDao.insertRolePermissions(normalizedRole, permissions);
        String diffSummary = buildDiffSummary(beforePermissions, permissions);
        auditLogService.logEvent("permission_update", "success", userId,
                "role=" + normalizedRole + ",diff=" + diffSummary);
        return "권한 저장이 완료되었습니다.";
    }

    /**
     * 목적: 역할 목록을 조회한다.
     * 기능: DB의 역할 목록을 반환한다.
     * 이유: 권한 매트릭스/역할 관리 화면에서 사용하기 위함이다.
     * 유지보수: 역할 테이블 구조 변경 시 반환 맵을 보완한다.
     */
    public List<Map<String, Object>> loadRoles() {
        return roleDao.selectRoles();
    }

    /**
     * 목적: 권한 변경 내역을 요약한다.
     * 기능: 변경된 권한 키 목록과 개수를 요약 문자열로 반환한다.
     * 이유: 감사 로그에 변경 내역을 남기기 위함이다.
     * 유지보수: 요약 포맷 변경 시 이 메서드를 수정한다.
     */
    private String buildDiffSummary(Map<String, Boolean> before, Map<String, Boolean> after) {
        if (after == null) {
            return "none";
        }
        List<String> changed = new java.util.ArrayList<String>();
        for (Map.Entry<String, Boolean> entry : after.entrySet()) {
            Boolean beforeValue = before == null ? null : before.get(entry.getKey());
            Boolean afterValue = entry.getValue();
            if (beforeValue == null || !beforeValue.equals(afterValue)) {
                changed.add(entry.getKey());
            }
        }
        int count = changed.size();
        if (count == 0) {
            return "none";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(count).append(" changed:");
        for (int i = 0; i < Math.min(count, 10); i++) {
            builder.append(changed.get(i));
            if (i < Math.min(count, 10) - 1) {
                builder.append("|");
            }
        }
        if (count > 10) {
            builder.append("|...");
        }
        return builder.toString();
    }
}
