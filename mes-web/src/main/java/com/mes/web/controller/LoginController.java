package com.mes.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mes.web.common.audit.AuditLogService;
import com.mes.web.common.auth.AuthService;
import com.mes.web.common.auth.AuthUser;

/**
 * 목적: 로그인 및 로그아웃 흐름을 제공한다.
 * 기능: /login 화면 표시, 로그인 처리, 로그아웃을 담당한다.
 * 이유: 전용 로그인 페이지 정책을 준수하기 위함이다.
 * 유지보수: 인증 정책 변경 시 이 컨트롤러를 수정한다.
 */
@Controller
public class LoginController {

    private static final String SESSION_AUTH_KEY = "AUTH_USER";

    private final AuthService authService;
    private final AuditLogService auditLogService;

    /**
     * 목적: 로그인 처리에 필요한 서비스를 주입한다.
     * 기능: 인증 서비스와 감사 로그 서비스를 설정한다.
     * 이유: 인증과 감사 로그를 분리해 유지보수를 쉽게 하기 위함이다.
     * 유지보수: 서비스 구현체 변경 시 주입만 교체한다.
     */
    @Autowired
    public LoginController(AuthService authService, AuditLogService auditLogService) {
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    /**
     * 목적: 로그인 화면을 표시한다.
     * 기능: /login 요청에 로그인 JSP를 반환한다.
     * 이유: 로그인 화면을 전용 페이지로 유지하기 위함이다.
     * 유지보수: 화면 경로 변경 시 반환 값만 수정한다.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "common/login";
    }

    /**
     * 목적: 로그인 처리를 수행한다.
     * 기능: 인증 성공 시 세션에 사용자 정보를 저장한다.
     * 이유: 이후 요청에서 인증 상태를 유지하기 위함이다.
     * 유지보수: 실제 사용자 조회로 교체 시 서비스 로직을 변경한다.
     */
    @PostMapping("/login")
    public String login(@RequestParam("userId") String userId,
                        @RequestParam("password") String password,
                        @RequestParam(value = "tenantId", required = false) String tenantId,
                        HttpServletRequest request,
                        Model model) {
        AuthUser authUser = authService.authenticate(userId, password);
        if (authUser == null) {
            auditLogService.logEvent("login_fail", "fail", userId, "userId=" + userId);
            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "common/login";
        }
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_AUTH_KEY, authUser);
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            session.setAttribute("TENANT_ID", tenantId);
        }
        auditLogService.logEvent("login_success", "success", authUser.getUserId(), "userId=" + authUser.getUserId());
        return "redirect:/dashboard/production";
    }

    /**
     * 목적: 로그아웃 처리를 수행한다.
     * 기능: 세션을 무효화하고 로그인 화면으로 이동한다.
     * 이유: 세션 기반 인증을 종료하기 위함이다.
     * 유지보수: 로그아웃 정책 변경 시 이 메서드를 수정한다.
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        Object authUser = request.getSession().getAttribute(SESSION_AUTH_KEY);
        String userId = null;
        if (authUser instanceof AuthUser) {
            userId = ((AuthUser) authUser).getUserId();
        }
        request.getSession().invalidate();
        auditLogService.logEvent("logout", "success", userId, "manual logout");
        return "redirect:/login";
    }
}
