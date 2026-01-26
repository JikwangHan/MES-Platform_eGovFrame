package com.mes.web.common.auth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 목적: 로그인 세션을 확인해 보호된 화면 접근을 제어한다.
 * 기능: 로그인 미인증 사용자는 /login으로 이동시킨다.
 * 이유: 권한 없는 접근을 차단해 보안을 확보하기 위함이다.
 * 유지보수: 예외 경로 추가 시 허용 목록만 수정한다.
 */
public class AuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);
    private static final String SESSION_KEY = "AUTH_USER";

    /**
     * 목적: 요청 경로에 대해 인증 여부를 검사한다.
     * 기능: 인증이 없으면 로그인 페이지로 리다이렉트한다.
     * 이유: 전용 로그인 흐름(/login)을 유지하기 위함이다.
     * 유지보수: 공개 경로 정책이 바뀌면 isAllowedPath를 수정한다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isAllowedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        Object authUser = request.getSession().getAttribute(SESSION_KEY);
        if (authUser == null) {
            LOGGER.debug("인증되지 않은 접근 차단: {}", path);
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 목적: 인증 없이 허용할 경로를 판별한다.
     * 기능: 로그인 화면과 정적 리소스 경로를 통과시킨다.
     * 이유: 로그인 화면 및 공통 리소스가 정상 로드되도록 하기 위함이다.
     * 유지보수: 추가 공개 경로가 생기면 조건을 확장한다.
     */
    private boolean isAllowedPath(String path) {
        return path.endsWith("/login")
                || path.endsWith("/signup")
                || path.startsWith("/signup/")
                || path.endsWith("/first-login")
                || path.contains("/resources/")
                || path.endsWith("/index.jsp")
                || path.equals("/");
    }
}
