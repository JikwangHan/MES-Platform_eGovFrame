package com.mes.web.common.correlation;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 목적: 모든 요청에 상관관계 ID를 부여한다.
 * 기능: 헤더/요청 속성에 Correlation ID를 설정하고 응답에도 반환한다.
 * 이유: 문제 분석과 감사 로그 추적을 쉽게 하기 위함이다.
 * 유지보수: 규칙 변경 시 헤더 이름만 교체하도록 구성한다.
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String HEADER_NAME = "X-Correlation-Id";
    private static final String ATTRIBUTE_NAME = "CORRELATION_ID";

    /**
     * 목적: 요청에 Correlation ID가 없으면 생성한다.
     * 기능: 요청/응답 헤더와 속성에 동일한 값을 저장한다.
     * 이유: 로그 간 추적 키를 항상 확보하기 위함이다.
     * 유지보수: 생성 규칙을 UUID 외 형식으로 교체 가능하다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = request.getHeader(HEADER_NAME);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        request.setAttribute(ATTRIBUTE_NAME, correlationId);
        response.setHeader(HEADER_NAME, correlationId);
        LOGGER.debug("Correlation ID 설정: {}", correlationId);
        filterChain.doFilter(request, response);
    }
}
