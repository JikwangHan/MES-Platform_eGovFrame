package com.mes.web.common.tenant;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 목적: 요청에서 테넌트 ID를 추출해 컨텍스트에 저장한다.
 * 기능: 헤더 또는 세션 값을 읽어 테넌트를 설정한다.
 * 이유: 기업별 DB 분리를 보장하기 위함이다.
 * 유지보수: 테넌트 식별 규칙이 바뀌면 여기서 수정한다.
 */
public class TenantResolverFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantResolverFilter.class);
    private static final String HEADER_NAME = "X-Tenant-Id";
    private static final String SESSION_KEY = "TENANT_ID";
    private static final String DEFAULT_TENANT = "default";

    /**
     * 목적: 요청별 테넌트 ID를 결정한다.
     * 기능: 헤더 우선, 없으면 세션, 없으면 기본값을 사용한다.
     * 이유: 다중 접속 환경에서도 명확한 DB 라우팅을 하기 위함이다.
     * 유지보수: 정책 변경 시 우선순위를 조정한다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantId = request.getHeader(HEADER_NAME);
        if (tenantId == null || tenantId.trim().isEmpty()) {
            Object sessionValue = request.getSession().getAttribute(SESSION_KEY);
            tenantId = sessionValue == null ? DEFAULT_TENANT : sessionValue.toString();
        }
        TenantContextHolder.setTenantId(tenantId);
        LOGGER.debug("테넌트 ID 설정: {}", tenantId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
