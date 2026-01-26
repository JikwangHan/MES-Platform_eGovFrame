package com.mes.web.common.auth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mes.web.dao.PermissionDao;

/**
 * 목적: 권한 매트릭스 관리 기능을 제공한다.
 * 기능: 역할별 권한 저장과 검증을 수행한다.
 * 이유: 관리자 화면에서 권한을 서버 기준으로 저장하기 위함이다.
 * 유지보수: 권한 정책 변경 시 검증 로직을 보완한다.
 */
@Service
public class PermissionAdminService {

    private static final Set<String> ALLOWED_ROLES = new HashSet<String>();

    static {
        ALLOWED_ROLES.add("SYSTEM_ADMIN");
        ALLOWED_ROLES.add("MANAGER");
        ALLOWED_ROLES.add("OPERATOR");
        ALLOWED_ROLES.add("VIEWER");
    }

    private final PermissionDao permissionDao;

    /**
     * 목적: 권한 DAO를 주입받는다.
     * 기능: 역할 권한 저장을 DAO로 위임한다.
     * 이유: DB 저장 로직을 서비스와 분리하기 위함이다.
     * 유지보수: DAO 교체 시 주입만 변경한다.
     */
    @Autowired
    public PermissionAdminService(PermissionDao permissionDao) {
        this.permissionDao = permissionDao;
    }

    /**
     * 목적: 역할 권한을 저장한다.
     * 기능: 유효성 검증 후 권한을 삭제/삽입한다.
     * 이유: 권한 정책을 DB에 확정하기 위함이다.
     * 유지보수: 역할/권한 정책 변경 시 검증을 보완한다.
     */
    public String saveRolePermissions(String roleCode, List<String> selectedKeys) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            return "역할 코드가 비어 있습니다.";
        }
        String normalizedRole = roleCode.trim().toUpperCase();
        if (!ALLOWED_ROLES.contains(normalizedRole)) {
            return "허용되지 않은 역할입니다.";
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

        permissionDao.deleteRolePermissions(normalizedRole);
        permissionDao.insertRolePermissions(normalizedRole, permissions);
        return "권한 저장이 완료되었습니다.";
    }
}
