package com.mes.web.common.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mes.web.dao.AuditLogDao;

/**
 * 목적: 개발 단계의 감사 로그 기록을 제공한다.
 * 기능: 로그 프레임워크로 이벤트 정보를 출력한다.
 * 이유: DB 연동 전에 감사 흐름을 확인하기 위함이다.
 * 유지보수: 실제 저장소 연동 시 이 구현체를 교체한다.
 */
@Service
public class SimpleAuditLogService implements AuditLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAuditLogService.class);
    private final AuditLogDao auditLogDao;

    /**
     * 목적: 감사 로그 DAO를 주입받는다.
     * 기능: 감사 로그 저장 처리를 DAO로 위임한다.
     * 이유: DB 저장과 로깅을 함께 수행하기 위함이다.
     * 유지보수: 저장 방식 변경 시 DAO만 교체한다.
     */
    @Autowired
    public SimpleAuditLogService(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    /**
     * 목적: 감사 이벤트를 기록한다.
     * 기능: 이벤트 유형/결과/상세를 로그로 출력한다.
     * 이유: 기본 감사 흐름을 즉시 확인하기 위함이다.
     * 유지보수: 로그 포맷 변경 시 여기만 수정한다.
     */
    @Override
    public void logEvent(String eventType, String result, String userId, String detail) {
        LOGGER.info("감사로그 eventType={}, result={}, userId={}, detail={}", eventType, result, userId, detail);
        auditLogDao.insertLog(eventType, result, userId, detail);
    }
}
