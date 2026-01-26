package com.mes.web.common.audit;

/**
 * 목적: 감사 로그에 필요한 요청 컨텍스트 정보를 담는다.
 * 기능: IP, User-Agent, Correlation ID 정보를 보관한다.
 * 이유: 감사 로그에 최소한의 추적 정보를 남기기 위함이다.
 * 유지보수: 추가 필드가 필요하면 속성을 확장한다.
 */
public class AuditContext {

    private final String clientIp;
    private final String userAgent;
    private final String correlationId;

    /**
     * 목적: 감사 컨텍스트 객체를 생성한다.
     * 기능: 요청 추적에 필요한 정보를 저장한다.
     * 이유: 감사 로그 기록 시 일관된 데이터를 사용하기 위함이다.
     * 유지보수: 추적 정보가 추가되면 생성자를 확장한다.
     */
    public AuditContext(String clientIp, String userAgent, String correlationId) {
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.correlationId = correlationId;
    }

    /**
     * 목적: 클라이언트 IP를 반환한다.
     * 기능: 감사 로그 IP 해시 생성에 사용한다.
     * 이유: 접속 경로를 추적하기 위함이다.
     * 유지보수: IP 수집 규칙 변경 시 호출부를 점검한다.
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * 목적: User-Agent 값을 반환한다.
     * 기능: 감사 로그 UA 해시 생성에 사용한다.
     * 이유: 접속 환경 추적에 필요하다.
     * 유지보수: UA 수집 규칙 변경 시 호출부를 점검한다.
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 목적: Correlation ID 값을 반환한다.
     * 기능: 요청 추적 키를 제공한다.
     * 이유: 로그 간 상관관계를 추적하기 위함이다.
     * 유지보수: 헤더 이름 변경 시 필터 로직을 점검한다.
     */
    public String getCorrelationId() {
        return correlationId;
    }
}
