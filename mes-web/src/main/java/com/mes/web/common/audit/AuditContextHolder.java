package com.mes.web.common.audit;

/**
 * 목적: 감사 컨텍스트를 스레드 로컬로 보관한다.
 * 기능: 요청 처리 중 어디서든 감사 정보를 조회할 수 있게 한다.
 * 이유: 감사 로그 기록 시 요청 정보를 전달하기 위함이다.
 * 유지보수: 비동기 처리 도입 시 전달 방식(예: MDC)을 보완한다.
 */
public final class AuditContextHolder {

    private static final ThreadLocal<AuditContext> CONTEXT = new ThreadLocal<AuditContext>();

    private AuditContextHolder() {
        // 유틸리티 클래스이므로 인스턴스 생성을 막는다.
    }

    /**
     * 목적: 감사 컨텍스트를 저장한다.
     * 기능: 현재 스레드에 컨텍스트를 등록한다.
     * 이유: 이후 서비스/DAO에서 동일 컨텍스트를 사용하기 위함이다.
     * 유지보수: 전달 방식 변경 시 이 메서드를 수정한다.
     */
    public static void set(AuditContext context) {
        CONTEXT.set(context);
    }

    /**
     * 목적: 감사 컨텍스트를 조회한다.
     * 기능: 현재 스레드의 컨텍스트를 반환한다.
     * 이유: 감사 로그 기록에 사용하기 위함이다.
     * 유지보수: 컨텍스트 구조 변경 시 호출부를 점검한다.
     */
    public static AuditContext get() {
        return CONTEXT.get();
    }

    /**
     * 목적: 감사 컨텍스트를 제거한다.
     * 기능: 요청 처리 종료 시 스레드 로컬을 정리한다.
     * 이유: 메모리 누수를 방지하기 위함이다.
     * 유지보수: 필터 구조 변경 시 호출 타이밍을 점검한다.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
