package com.mes.web.common.auth;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mes.web.common.audit.AuditLogService;
import com.mes.web.dao.PermissionDao;
import com.mes.web.dao.RoleDao;

/**
 * 목적: 역할 관리 기능을 제공한다.
 * 기능: 역할 추가/삭제 및 유효성 검증을 수행한다.
 * 이유: 관리자 화면에서 역할을 안전하게 관리하기 위함이다.
 * 유지보수: 역할 정책 변경 시 검증 로직을 보완한다.
 */
@Service
public class RoleAdminService {

    private final RoleDao roleDao;
    private final PermissionDao permissionDao;
    private final AuditLogService auditLogService;

    /**
     * 목적: 역할/권한 DAO와 감사 로그 서비스를 주입받는다.
     * 기능: 역할 저장/삭제 및 감사 로그 기록을 수행한다.
     * 이유: 관리 작업을 서버 기준으로 처리하기 위함이다.
     * 유지보수: 저장소 변경 시 주입만 수정한다.
     */
    @Autowired
    public RoleAdminService(RoleDao roleDao, PermissionDao permissionDao, AuditLogService auditLogService) {
        this.roleDao = roleDao;
        this.permissionDao = permissionDao;
        this.auditLogService = auditLogService;
    }

    /**
     * 목적: 역할 목록을 조회한다.
     * 기능: DB의 역할 목록을 반환한다.
     * 이유: 화면에 역할 정보를 표시하기 위함이다.
     * 유지보수: 역할 테이블 구조 변경 시 반환 맵을 보완한다.
     */
    public List<Map<String, Object>> loadRoles() {
        return roleDao.selectRoles();
    }

    /**
     * 목적: 역할을 등록한다.
     * 기능: 역할 코드/이름/설명을 저장한다.
     * 이유: 역할 추가 기능을 제공하기 위함이다.
     * 유지보수: 역할 정책 변경 시 검증 로직을 보완한다.
     */
    public String createRole(String roleCode, String roleName, String roleDesc, String userId) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            return "역할 코드는 필수입니다.";
        }
        if (roleName == null || roleName.trim().isEmpty()) {
            return "역할명은 필수입니다.";
        }
        String normalizedCode = roleCode.trim().toUpperCase();
        if (!normalizedCode.matches("^[A-Z0-9_]{3,30}$")) {
            return "역할 코드는 영문 대문자/숫자/언더바 조합으로 3~30자여야 합니다.";
        }
        for (Map<String, Object> role : roleDao.selectRoles()) {
            Object code = role.get("roleCode");
            if (code != null && normalizedCode.equals(code.toString())) {
                return "이미 존재하는 역할 코드입니다.";
            }
        }
        roleDao.insertRole(normalizedCode, roleName.trim(), roleDesc);
        auditLogService.logEvent("role_create", "success", userId, "role=" + normalizedCode);
        return "역할이 추가되었습니다.";
    }

    /**
     * 목적: 역할을 삭제한다.
     * 기능: 역할 사용 여부를 확인하고 삭제한다.
     * 이유: 사용 중인 역할 삭제를 방지하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 검증 로직을 보완한다.
     */
    public String deleteRole(String roleCode, String userId) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            return "역할 코드는 필수입니다.";
        }
        String normalizedCode = roleCode.trim().toUpperCase();
        if ("SYSTEM_ADMIN".equals(normalizedCode)) {
            return "SYSTEM_ADMIN 역할은 삭제할 수 없습니다.";
        }
        int users = roleDao.countUsersByRole(normalizedCode);
        if (users > 0) {
            return "사용 중인 역할은 삭제할 수 없습니다.";
        }
        permissionDao.deleteRolePermissions(normalizedCode);
        int count = roleDao.deleteRole(normalizedCode);
        auditLogService.logEvent("role_delete", count > 0 ? "success" : "fail", userId, "role=" + normalizedCode);
        return count > 0 ? "역할이 삭제되었습니다." : "삭제할 역할을 찾지 못했습니다.";
    }
}
