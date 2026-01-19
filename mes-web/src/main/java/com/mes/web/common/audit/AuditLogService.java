package com.mes.web.common.audit;

/**
 * 목적: 감사 로그 기록을 표준화한다.
 * 기능: 주요 보안 이벤트를 기록하는 인터페이스를 제공한다.
 * 이유: 감사 추적 요구사항을 충족하기 위함이다.
 * 유지보수: 저장소 변경 시 구현체만 교체한다.
 */
public interface AuditLogService {

    /**
     * 목적: 보안 이벤트를 기록한다.
     * 기능: 이벤트 유형과 결과를 로깅 대상으로 전달한다.
     * 이유: 로그인/권한/변경 이력을 추적하기 위함이다.
     * 유지보수: 필드 확장 시 메서드 시그니처를 조정한다.
     */
    void logEvent(String eventType, String result, String userId, String detail);
}
