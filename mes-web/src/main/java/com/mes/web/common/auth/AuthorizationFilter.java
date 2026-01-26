package com.mes.web.common.auth;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 목적: 권한 기반 접근 제어를 수행한다.
 * 기능: 요청 경로에 필요한 권한을 확인하고 허용/차단을 처리한다.
 * 이유: 권한별 메뉴/버튼 제어를 서버 기준으로 확정하기 위함이다.
 * 유지보수: 권한 정책 변경 시 PermissionService만 수정한다.
 */
public class AuthorizationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);
    private static final String SESSION_AUTH_KEY = "AUTH_USER";
    private static final String SESSION_PERMISSION_KEY = "PERMISSIONS";
    private static final String SESSION_ROLE_KEY = "AUTH_ROLE";

    private PermissionService permissionService;

    /**
     * 목적: 요청마다 권한을 확인한다.
     * 기능: 권한 없으면 403 또는 JSON 오류를 반환한다.
     * 이유: 서버에서 권한을 확정하고 UI/버튼 조작을 차단하기 위함이다.
     * 유지보수: 응답 포맷 변경 시 이 메서드를 수정한다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = normalizePath(request);
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Object authUser = request.getSession().getAttribute(SESSION_AUTH_KEY);
        if (!(authUser instanceof AuthUser)) {
            filterChain.doFilter(request, response);
            return;
        }

        AuthUser user = (AuthUser) authUser;
        PermissionService service = getPermissionService(request);
        Map<String, Boolean> permissions = service.buildPermissionMap(user.getRole());
        request.getSession().setAttribute(SESSION_PERMISSION_KEY, permissions);
        request.getSession().setAttribute(SESSION_ROLE_KEY, user.getRole());

        String requiredKey = service.resolvePermissionKey(path, request.getMethod());
        if (requiredKey == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (Boolean.TRUE.equals(permissions.get(requiredKey))) {
            filterChain.doFilter(request, response);
            return;
        }

        LOGGER.warn("권한 차단: userId={}, role={}, path={}", user.getUserId(), user.getRole(), path);
        if (path.startsWith("/api/")) {
            writeJsonForbidden(response);
            return;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "권한이 없습니다.");
    }

    /**
     * 목적: 권한 서비스 빈을 조회한다.
     * 기능: WebApplicationContext에서 PermissionService를 가져온다.
     * 이유: 필터가 스프링 빈이 아니므로 직접 조회해야 하기 위함이다.
     * 유지보수: 빈 이름/패키지 변경 시 조회 로직을 수정한다.
     */
    private PermissionService getPermissionService(HttpServletRequest request) {
        if (permissionService == null) {
            WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(
                    request.getServletContext());
            permissionService = context.getBean(PermissionService.class);
        }
        return permissionService;
    }

    /**
     * 목적: 요청 경로를 정규화한다.
     * 기능: 컨텍스트 경로를 제거한 순수 경로를 반환한다.
     * 이유: 권한 매핑을 단순화하기 위함이다.
     * 유지보수: URL 정책 변경 시 정규화 규칙을 보완한다.
     */
    private String normalizePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        }
        return path;
    }

    /**
     * 목적: 공개 경로 여부를 확인한다.
     * 기능: 로그인/정적 리소스 경로를 허용한다.
     * 이유: 로그인 화면과 정적 리소스는 인증 없이 접근해야 하기 위함이다.
     * 유지보수: 공개 경로 추가 시 조건을 확장한다.
     */
    private boolean isPublicPath(String path) {
        return path == null
                || path.equals("/")
                || path.endsWith("/login")
                || path.endsWith("/logout")
                || path.startsWith("/resources/")
                || path.endsWith("/index.jsp")
                || path.startsWith("/account/");
    }

    /**
     * 목적: JSON 형태의 권한 오류 응답을 반환한다.
     * 기능: 응답 코드와 메시지를 JSON으로 내려준다.
     * 이유: API 호출이 화면 리다이렉트로 깨지지 않도록 하기 위함이다.
     * 유지보수: 응답 포맷 변경 시 JSON 구조를 수정한다.
     */
    private void writeJsonForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"result\":\"forbidden\",\"message\":\"권한이 없습니다.\"}");
    }
}
