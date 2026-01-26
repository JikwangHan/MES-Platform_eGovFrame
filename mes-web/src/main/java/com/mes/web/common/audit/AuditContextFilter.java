package com.mes.web.common.audit;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 목적: 감사 로그에 필요한 요청 정보를 수집한다.
 * 기능: IP/UA/Correlation ID를 스레드 로컬에 저장한다.
 * 이유: 감사 로그 기록 시 요청 정보를 쉽게 활용하기 위함이다.
 * 유지보수: 프록시 환경 변경 시 IP 추출 로직을 보완한다.
 */
public class AuditContextFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ATTRIBUTE = "CORRELATION_ID";

    /**
     * 목적: 요청 정보를 감사 컨텍스트로 등록한다.
     * 기능: 요청 처리 전/후로 컨텍스트를 설정하고 정리한다.
     * 이유: 모든 감사 로그가 동일한 컨텍스트를 사용하게 하기 위함이다.
     * 유지보수: 비동기 처리 도입 시 컨텍스트 전파를 추가한다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        AuditContextHolder.set(new AuditContext(extractClientIp(request), request.getHeader("User-Agent"),
                (String) request.getAttribute(CORRELATION_ATTRIBUTE)));
        try {
            filterChain.doFilter(request, response);
        } finally {
            AuditContextHolder.clear();
        }
    }

    /**
     * 목적: 클라이언트 IP를 추출한다.
     * 기능: X-Forwarded-For를 우선 확인하고 없으면 RemoteAddr를 사용한다.
     * 이유: 프록시 환경에서도 실제 클라이언트 IP를 추적하기 위함이다.
     * 유지보수: 헤더 정책 변경 시 추출 로직을 수정한다.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
