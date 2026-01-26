package com.mes.web.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;

import com.mes.web.common.auth.PermissionAdminService;
import com.mes.web.common.auth.PermissionCatalog;
import com.mes.web.common.auth.PermissionService;
import com.mes.web.common.auth.RoleAdminService;

/**
 * 목적: 관리자 화면을 제공한다.
 * 기능: 사용자/권한/담당자/거래처/공장/창고 화면을 반환한다.
 * 이유: 관리자 기능을 한 곳에서 제공하기 위함이다.
 * 유지보수: 권한 정책 변경 시 화면 매핑을 수정한다.
 */
@Controller
public class AdminController {

    private final PermissionService permissionService;
    private final PermissionAdminService permissionAdminService;
    private final RoleAdminService roleAdminService;

    /**
     * 목적: 권한 서비스를 주입받는다.
     * 기능: 권한 매트릭스 데이터를 생성할 수 있게 한다.
     * 이유: 권한 화면에서 서버 기준 권한을 표시하기 위함이다.
     * 유지보수: 권한 서비스 변경 시 주입만 수정한다.
     */
    @Autowired
    public AdminController(PermissionService permissionService, PermissionAdminService permissionAdminService,
                           RoleAdminService roleAdminService) {
        this.permissionService = permissionService;
        this.permissionAdminService = permissionAdminService;
        this.roleAdminService = roleAdminService;
    }

    /**
     * 목적: 사용자 화면을 반환한다.
     * 기능: /admin/users 요청을 JSP로 연결한다.
     * 이유: 사용자 관리 화면을 제공하기 위함이다.
     * 유지보수: 초기화/권한 기능 추가 시 컨트롤러를 확장한다.
     */
    @GetMapping("/admin/users")
    public String users() {
        return "admin/users";
    }

    /**
     * 목적: 역할 관리 화면을 반환한다.
     * 기능: /admin/roles 요청을 JSP로 연결한다.
     * 이유: 역할 추가/삭제 화면을 제공하기 위함이다.
     * 유지보수: 역할 필드 확정 시 모델 값을 보완한다.
     */
    @GetMapping("/admin/roles")
    public String roles(Model model) {
        model.addAttribute("roles", roleAdminService.loadRoles());
        return "admin/roles";
    }

    /**
     * 목적: 역할을 등록한다.
     * 기능: 역할 코드/이름/설명을 저장한다.
     * 이유: 역할 추가 기능을 제공하기 위함이다.
     * 유지보수: 검증 정책 변경 시 서비스 로직을 보완한다.
     */
    @PostMapping("/admin/roles/create")
    public String createRole(@RequestParam("roleCode") String roleCode,
                             @RequestParam("roleName") String roleName,
                             @RequestParam(value = "roleDesc", required = false) String roleDesc,
                             RedirectAttributes redirectAttributes) {
        String message = roleAdminService.createRole(roleCode, roleName, roleDesc, getAuthUserId());
        redirectAttributes.addFlashAttribute("saveMessage", message);
        return "redirect:/admin/roles";
    }

    /**
     * 목적: 역할을 삭제한다.
     * 기능: 역할 코드 기준으로 삭제한다.
     * 이유: 역할 정리 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 서비스 로직을 보완한다.
     */
    @PostMapping("/admin/roles/delete")
    public String deleteRole(@RequestParam("roleCode") String roleCode,
                             RedirectAttributes redirectAttributes) {
        String message = roleAdminService.deleteRole(roleCode, getAuthUserId());
        redirectAttributes.addFlashAttribute("saveMessage", message);
        return "redirect:/admin/roles";
    }

    /**
     * 목적: 사용자 권한 화면을 반환한다.
     * 기능: /admin/permissions 요청을 JSP로 연결한다.
     * 이유: 권한 매트릭스 화면을 제공하기 위함이다.
     * 유지보수: 권한 구조 변경 시 UI를 수정한다.
     */
    @GetMapping("/admin/permissions")
    public String permissions(Model model) {
        model.addAttribute("roles", loadRoles());
        model.addAttribute("permissionGroups", PermissionCatalog.buildGroups());
        Map<String, Map<String, Boolean>> rolePermissions = new LinkedHashMap<String, Map<String, Boolean>>();
        for (String role : loadRoles()) {
            rolePermissions.put(role, permissionService.buildPermissionMap(role));
        }
        model.addAttribute("rolePermissions", rolePermissions);
        return "admin/permissions";
    }

    /**
     * 목적: 권한 매트릭스 저장을 처리한다.
     * 기능: 선택된 권한을 역할 기준으로 저장한다.
     * 이유: 서버 기준 권한 정책을 관리자 화면에서 수정하기 위함이다.
     * 유지보수: 저장 규칙 변경 시 서비스 로직을 수정한다.
     */
    @PostMapping("/admin/permissions/save")
    public String savePermissions(@RequestParam("roleCode") String roleCode,
                                  @RequestParam(value = "permKeys", required = false) java.util.List<String> permKeys,
                                  RedirectAttributes redirectAttributes) {
        String message = permissionAdminService.saveRolePermissions(roleCode, permKeys, getAuthUserId());
        redirectAttributes.addFlashAttribute("saveMessage", message);
        return "redirect:/admin/permissions";
    }

    /**
     * 목적: 역할 목록을 조회한다.
     * 기능: 역할 목록을 문자열 리스트로 반환한다.
     * 이유: 권한 매트릭스 화면에 역할을 표시하기 위함이다.
     * 유지보수: 역할 데이터 구조 변경 시 로직을 보완한다.
     */
    private java.util.List<String> loadRoles() {
        java.util.List<java.util.Map<String, Object>> roles = roleAdminService.loadRoles();
        java.util.List<String> roleCodes = new java.util.ArrayList<String>();
        if (roles == null) {
            return roleCodes;
        }
        for (java.util.Map<String, Object> role : roles) {
            Object code = role.get("roleCode");
            if (code != null) {
                roleCodes.add(code.toString());
            }
        }
        return roleCodes;
    }

    /**
     * 목적: 로그인 사용자 ID를 조회한다.
     * 기능: 세션에서 사용자 ID를 추출한다.
     * 이유: 권한 변경 감사 로그에 사용자 정보를 기록하기 위함이다.
     * 유지보수: 인증 세션 구조 변경 시 로직을 수정한다.
     */
    private String getAuthUserId() {
        Object authUser = org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
                .getAttribute("AUTH_USER", org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION);
        if (authUser instanceof com.mes.web.common.auth.AuthUser) {
            return ((com.mes.web.common.auth.AuthUser) authUser).getUserId();
        }
        return null;
    }

    /**
     * 목적: 업무담당자 화면을 반환한다.
     * 기능: /admin/responsibles 요청을 JSP로 연결한다.
     * 이유: 담당자 관리 화면을 제공하기 위함이다.
     * 유지보수: 담당자 매핑 규칙 변경 시 UI를 수정한다.
     */
    @GetMapping("/admin/responsibles")
    public String responsibles() {
        return "admin/responsibles";
    }

    /**
     * 목적: 거래처 화면을 반환한다.
     * 기능: /admin/partners 요청을 JSP로 연결한다.
     * 이유: 거래처 관리 화면을 제공하기 위함이다.
     * 유지보수: 데이터 연동 시 서비스 호출을 추가한다.
     */
    @GetMapping("/admin/partners")
    public String partners() {
        return "admin/partners";
    }

    /**
     * 목적: 생산공장/창고 화면을 반환한다.
     * 기능: /admin/factories-warehouses 요청을 JSP로 연결한다.
     * 이유: 공장/창고 관리 화면을 제공하기 위함이다.
     * 유지보수: 트리 구조 변경 시 UI를 수정한다.
     */
    @GetMapping("/admin/factories-warehouses")
    public String factoriesWarehouses() {
        return "admin/factories_warehouses";
    }
}
