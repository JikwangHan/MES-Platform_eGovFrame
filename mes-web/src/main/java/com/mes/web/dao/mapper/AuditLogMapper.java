package com.mes.web.dao.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 목적: 감사 로그 매퍼 인터페이스를 정의한다.
 * 기능: 감사 로그 저장 SQL을 선언한다.
 * 이유: 감사 이벤트를 DB에 기록하기 위함이다.
 * 유지보수: 컬럼 확장 시 SQL과 함께 수정한다.
 */
public interface AuditLogMapper {

    /**
     * 목적: 감사 로그를 저장한다.
     * 기능: 이벤트 유형/결과/상세를 저장한다.
     * 이유: 감사 이벤트 추적에 필요하다.
     * 유지보수: 컬럼 확장 시 파라미터 구조를 보완한다.
     */
    void insertLog(@Param("params") Map<String, Object> params);
}
