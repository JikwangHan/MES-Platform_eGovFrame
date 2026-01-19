package com.mes.web.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mes.web.dao.mapper.AuditLogMapper;

/**
 * 목적: 감사 로그 데이터 접근을 담당한다.
 * 기능: 감사 로그 저장을 수행한다.
 * 이유: 감사 이벤트를 DB에 일관되게 기록하기 위함이다.
 * 유지보수: 저장 컬럼 확장 시 SQL을 함께 수정한다.
 */
@Repository
public class AuditLogDao {

    private final AuditLogMapper auditLogMapper;

    /**
     * 목적: 매퍼를 주입받는다.
     * 기능: DAO 내부에서 매퍼를 사용할 수 있게 한다.
     * 이유: 데이터 접근을 인터페이스로 분리하기 위함이다.
     * 유지보수: 매퍼 교체 시 주입만 변경한다.
     */
    @Autowired
    public AuditLogDao(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 목적: 감사 로그를 저장한다.
     * 기능: 이벤트 유형/결과/상세를 저장한다.
     * 이유: 보안 이벤트 추적을 위해 필요하다.
     * 유지보수: 필드 확장 시 파라미터를 보완한다.
     */
    public void insertLog(String eventType, String result, String userId, String detail) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("eventType", eventType);
        params.put("result", result);
        params.put("userId", userId);
        params.put("detail", detail);
        auditLogMapper.insertLog(params);
    }
}
