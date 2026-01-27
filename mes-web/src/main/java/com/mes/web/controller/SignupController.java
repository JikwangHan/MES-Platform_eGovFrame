package com.mes.web.controller;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mes.web.common.audit.AuditLogService;
import com.mes.web.common.auth.AuthService;
import com.mes.web.common.auth.AuthUser;
import com.mes.web.common.auth.PermissionService;
import com.mes.web.common.mail.EmailService;
import com.mes.web.common.tenant.TenantContextHolder;
import com.mes.web.dao.UserDao;
import com.mes.web.service.UserRegistrationService;

/**
 * 목적: 회원가입 화면과 처리 흐름을 제공한다.
 * 기능: /signup 화면 표시 및 회원가입 처리 결과를 반환한다.
 * 이유: 로그인 이전 단계에서 사용자 등록을 지원하기 위함이다.
 * 유지보수: 입력 항목/정책 변경 시 이 컨트롤러를 수정한다.
 */
@Controller
public class SignupController {

    private final UserRegistrationService userRegistrationService;
    private final AuditLogService auditLogService;
    private final UserDao userDao;
    private final AuthService authService;
    private final PermissionService permissionService;
    private final EmailService emailService;

    /**
     * 목적: 필요한 서비스를 주입받는다.
     * 기능: 회원가입 서비스와 감사 로그 서비스를 연결한다.
     * 이유: 가입 처리와 보안 기록을 분리하기 위함이다.
     * 유지보수: 서비스 구현체 변경 시 주입만 교체한다.
     */
    @Autowired
    public SignupController(UserRegistrationService userRegistrationService,
                            AuditLogService auditLogService,
                            UserDao userDao,
                            AuthService authService,
                            PermissionService permissionService,
                            EmailService emailService) {
        this.userRegistrationService = userRegistrationService;
        this.auditLogService = auditLogService;
        this.userDao = userDao;
        this.authService = authService;
        this.permissionService = permissionService;
        this.emailService = emailService;
    }

    /**
     * 목적: 회원가입 화면을 표시한다.
     * 기능: /signup 요청에 회원가입 JSP를 반환한다.
     * 이유: 로그인과 분리된 가입 화면을 제공하기 위함이다.
     * 유지보수: 화면 경로 변경 시 반환 값만 수정한다.
     */
    @GetMapping("/signup")
    public String signupPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new HashMap<String, Object>());
        }
        return "page/auth/signup";
    }

    /**
     * 목적: 회원가입 처리를 수행한다.
     * 기능: 입력값 검증 및 DB 저장 후 로그인 화면으로 이동한다.
     * 이유: 회원가입 완료 후 로그인으로 안내하기 위함이다.
     * 유지보수: 검증/저장 정책 변경 시 로직을 수정한다.
     */
    @PostMapping("/signup")
    public String signup(@RequestParam("userId") String userId,
                         @RequestParam("userName") String userName,
                         @RequestParam("password") String password,
                         @RequestParam("passwordConfirm") String passwordConfirm,
                         @RequestParam(value = "role", required = false) String role,
                         @RequestParam(value = "tenantId", required = false) String tenantId,
                         @RequestParam(value = "phone", required = false) String phone,
                         @RequestParam(value = "email", required = false) String email,
                         @RequestParam(value = "agreeTerms", required = false) String agreeTerms,
                         @RequestParam(value = "agreePrivacy", required = false) String agreePrivacy,
                         @RequestParam(value = "autoLogin", required = false) String autoLogin,
                         HttpServletRequest request,
                         Model model) {
        Map<String, Object> form = new HashMap<String, Object>();
        form.put("userId", userId);
        form.put("userName", userName);
        form.put("role", role);
        form.put("tenantId", tenantId);
        form.put("phone", phone);
        form.put("email", email);
        model.addAttribute("form", form);

        if (agreeTerms == null || agreePrivacy == null) {
            model.addAttribute("errorMessage", "필수 약관에 동의해 주세요.");
            auditLogService.logEvent("signup_fail", "fail", userId, "required_terms_missing");
            return "page/auth/signup";
        }

        if (!password.equals(passwordConfirm)) {
            model.addAttribute("errorMessage", "비밀번호와 확인값이 일치하지 않습니다.");
            auditLogService.logEvent("signup_fail", "fail", userId, "password_mismatch");
            return "page/auth/signup";
        }

        String resolvedTenant = null;
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            resolvedTenant = tenantId.trim();
            request.getSession().setAttribute("TENANT_ID", resolvedTenant);
            TenantContextHolder.setTenantId(resolvedTenant);
        }

        try {
            boolean requireApproval = Boolean.parseBoolean(System.getProperty("mes.signup.requireApproval", "false"));
            boolean requireEmailVerify = email != null && !email.trim().isEmpty();
            String status = requireEmailVerify ? "pending_email" : (requireApproval ? "pending_approval" : "active");
            userRegistrationService.registerUser(userId, userName, password, role, phone, email, status);
            auditLogService.logEvent("signup_success", "success", userId, "role=" + role);

            if (requireEmailVerify) {
                String code = generateVerificationCode();
                request.getSession().setAttribute("SIGNUP_VERIFY_CODE", code);
                request.getSession().setAttribute("SIGNUP_VERIFY_USER", userId);
                request.getSession().setAttribute("SIGNUP_VERIFY_EMAIL", email);
                boolean sent = false;
                if (emailService.isEnabled()) {
                    try {
                        emailService.sendSignupVerification(email, userId, code);
                        sent = true;
                    } catch (IllegalStateException ex) {
                        request.getSession().setAttribute("SIGNUP_VERIFY_ERROR", "이메일 발송에 실패했습니다. 관리자에게 문의해 주세요.");
                    }
                } else {
                    request.getSession().setAttribute("SIGNUP_VERIFY_ERROR", "이메일 설정이 없어 인증 메일을 발송하지 못했습니다.");
                }
                request.getSession().setAttribute("SIGNUP_VERIFY_SENT", sent);
                return "redirect:/signup/verify";
            }

            if (requireApproval) {
                request.getSession().setAttribute("SIGNUP_SUCCESS", "회원가입이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다.");
                return "redirect:/login";
            }

            if ("Y".equalsIgnoreCase(autoLogin)) {
                AuthUser authUser = authService.authenticate(userId, password);
                if (authUser != null) {
                    request.getSession().setAttribute("AUTH_USER", authUser);
                    request.getSession().setAttribute("PERMISSIONS", permissionService.buildPermissionMap(authUser.getRole()));
                    request.getSession().setAttribute("AUTH_ROLE", authUser.getRole());
                    auditLogService.logEvent("signup_autologin", "success", userId, "auto_login");
                    return "redirect:/dashboard/production";
                }
                auditLogService.logEvent("signup_autologin", "fail", userId, "auto_login_failed");
            }
            request.getSession().setAttribute("SIGNUP_SUCCESS", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            auditLogService.logEvent("signup_fail", "fail", userId, ex.getMessage());
            return "page/auth/signup";
        } finally {
            if (resolvedTenant != null) {
                TenantContextHolder.clear();
            }
        }
    }

    /**
     * 목적: 이메일 인증 화면을 표시한다.
     * 기능: 인증 코드 입력 화면을 제공한다.
     * 이유: 이메일 기반 인증 흐름을 제공하기 위함이다.
     * 유지보수: 안내 문구/흐름 변경 시 수정한다.
     */
    @GetMapping("/signup/verify")
    public String signupVerifyPage(HttpServletRequest request, Model model) {
        Object userId = request.getSession().getAttribute("SIGNUP_VERIFY_USER");
        Object email = request.getSession().getAttribute("SIGNUP_VERIFY_EMAIL");
        Object code = request.getSession().getAttribute("SIGNUP_VERIFY_CODE");
        Object sent = request.getSession().getAttribute("SIGNUP_VERIFY_SENT");
        Object error = request.getSession().getAttribute("SIGNUP_VERIFY_ERROR");
        model.addAttribute("verifyUserId", userId);
        model.addAttribute("verifyEmail", email);
        model.addAttribute("devCode", code);
        model.addAttribute("verifySent", sent);
        model.addAttribute("verifyError", error);
        return "page/auth/signup-verify";
    }

    /**
     * 목적: 이메일 인증 처리를 수행한다.
     * 기능: 인증 코드가 일치하면 사용자 상태를 활성화한다.
     * 이유: 이메일 인증 완료 후 로그인 가능 상태로 전환하기 위함이다.
     * 유지보수: 인증 정책 변경 시 로직을 수정한다.
     */
    @PostMapping("/signup/verify")
    public String signupVerify(@RequestParam("code") String code,
                               HttpServletRequest request,
                               Model model) {
        String storedCode = String.valueOf(request.getSession().getAttribute("SIGNUP_VERIFY_CODE"));
        String userId = String.valueOf(request.getSession().getAttribute("SIGNUP_VERIFY_USER"));
        if (userId == null || storedCode == null) {
            model.addAttribute("errorMessage", "인증 정보가 만료되었습니다. 다시 회원가입해 주세요.");
            return "page/auth/signup-verify";
        }
        if (!storedCode.equals(code)) {
            model.addAttribute("errorMessage", "인증 코드가 일치하지 않습니다.");
            auditLogService.logEvent("signup_verify_fail", "fail", userId, "code_mismatch");
            return "page/auth/signup-verify";
        }
        boolean requireApproval = Boolean.parseBoolean(System.getProperty("mes.signup.requireApproval", "false"));
        String nextStatus = requireApproval ? "pending_approval" : "active";
        userDao.updateUserStatus(userId, nextStatus);
        auditLogService.logEvent("signup_verify_success", "success", userId, "email_verified");
        request.getSession().removeAttribute("SIGNUP_VERIFY_CODE");
        request.getSession().removeAttribute("SIGNUP_VERIFY_USER");
        request.getSession().removeAttribute("SIGNUP_VERIFY_EMAIL");
        if (requireApproval) {
            request.getSession().setAttribute("SIGNUP_SUCCESS", "이메일 인증이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다.");
        } else {
            request.getSession().setAttribute("SIGNUP_SUCCESS", "이메일 인증이 완료되었습니다. 로그인해 주세요.");
        }
        return "redirect:/login";
    }

    /**
     * 목적: 이메일 인증 코드를 생성한다.
     * 기능: 6자리 숫자 코드를 생성한다.
     * 이유: 사용자 입력을 단순화하기 위함이다.
     * 유지보수: 길이/형식 변경 시 이 메서드를 수정한다.
     */
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
