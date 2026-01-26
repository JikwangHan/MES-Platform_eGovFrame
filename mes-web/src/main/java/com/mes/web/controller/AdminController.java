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

    /**
     * 목적: 권한 서비스를 주입받는다.
     * 기능: 권한 매트릭스 데이터를 생성할 수 있게 한다.
     * 이유: 권한 화면에서 서버 기준 권한을 표시하기 위함이다.
     * 유지보수: 권한 서비스 변경 시 주입만 수정한다.
     */
    @Autowired
    public AdminController(PermissionService permissionService, PermissionAdminService permissionAdminService) {
        this.permissionService = permissionService;
        this.permissionAdminService = permissionAdminService;
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
     * 목적: 사용자 권한 화면을 반환한다.
     * 기능: /admin/permissions 요청을 JSP로 연결한다.
     * 이유: 권한 매트릭스 화면을 제공하기 위함이다.
     * 유지보수: 권한 구조 변경 시 UI를 수정한다.
     */
    @GetMapping("/admin/permissions")
    public String permissions(Model model) {
        model.addAttribute("permissionGroups", PermissionCatalog.buildGroups());
        Map<String, Map<String, Boolean>> rolePermissions = new LinkedHashMap<String, Map<String, Boolean>>();
        rolePermissions.put("SYSTEM_ADMIN", permissionService.buildPermissionMap("SYSTEM_ADMIN"));
        rolePermissions.put("MANAGER", permissionService.buildPermissionMap("MANAGER"));
        rolePermissions.put("OPERATOR", permissionService.buildPermissionMap("OPERATOR"));
        rolePermissions.put("VIEWER", permissionService.buildPermissionMap("VIEWER"));
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
        String message = permissionAdminService.saveRolePermissions(roleCode, permKeys);
        redirectAttributes.addFlashAttribute("saveMessage", message);
        return "redirect:/admin/permissions";
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
