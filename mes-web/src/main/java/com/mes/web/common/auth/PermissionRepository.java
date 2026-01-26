package com.mes.web.common.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mes.web.dao.PermissionDao;

/**
 * 목적: 권한 저장소 접근 로직을 제공한다.
 * 기능: DB에서 역할별 권한 정보를 조회한다.
 * 이유: 권한 정책을 서버에서 확정하기 위함이다.
 * 유지보수: 저장소 변경 시 DAO 로직만 교체한다.
 */
@Service
public class PermissionRepository {

    private final PermissionDao permissionDao;

    /**
     * 목적: 권한 DAO를 주입받는다.
     * 기능: 권한 데이터를 조회할 수 있게 한다.
     * 이유: 권한 정책을 DB에서 읽기 위함이다.
     * 유지보수: DAO 교체 시 주입만 변경한다.
     */
    @Autowired
    public PermissionRepository(PermissionDao permissionDao) {
        this.permissionDao = permissionDao;
    }

    /**
     * 목적: 역할별 권한 맵을 조회한다.
     * 기능: DB에서 권한 목록을 읽어 Map으로 반환한다.
     * 이유: 권한 체크와 메뉴 표시에서 사용하기 위함이다.
     * 유지보수: 필드 구조 변경 시 매핑 로직을 보완한다.
     */
    public Map<String, Boolean> loadRolePermissions(String roleCode) {
        List<Map<String, Object>> rows = permissionDao.selectRolePermissions(roleCode);
        Map<String, Boolean> permissions = new HashMap<String, Boolean>();
        if (rows == null) {
            return permissions;
        }
        for (Map<String, Object> row : rows) {
            Object key = row.get("permKey");
            Object allowed = row.get("allowed");
            if (key == null) {
                continue;
            }
            boolean value = false;
            if (allowed instanceof Number) {
                value = ((Number) allowed).intValue() == 1;
            } else if (allowed != null) {
                value = "1".equals(allowed.toString()) || "true".equalsIgnoreCase(allowed.toString());
            }
            permissions.put(key.toString(), value);
        }
        return permissions;
    }
}
