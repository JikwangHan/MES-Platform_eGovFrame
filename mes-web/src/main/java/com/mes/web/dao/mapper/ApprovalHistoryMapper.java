package com.mes.web.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 목적: 승인 이력 매퍼 인터페이스를 정의한다.
 * 기능: 승인 이력 저장/조회 SQL을 선언한다.
 * 이유: 승인 이력 DB 접근을 분리하기 위함이다.
 * 유지보수: 쿼리 변경 시 SQL과 함께 수정한다.
 */
public interface ApprovalHistoryMapper {

    /**
     * 목적: 승인 이력을 저장한다.
     * 기능: 승인/반려 기록을 DB에 저장한다.
     * 이유: 이력 조회 화면에 사용하기 위함이다.
     * 유지보수: 컬럼 추가 시 SQL을 수정한다.
     */
    void insertHistory(@Param("params") Map<String, Object> params);

    /**
     * 목적: 승인 이력 목록을 조회한다.
     * 기능: 검색 조건/페이징을 적용해 목록을 반환한다.
     * 이유: 관리자 이력 조회에 사용하기 위함이다.
     * 유지보수: 조건 추가 시 SQL을 수정한다.
     */
    List<Map<String, Object>> findHistory(@Param("params") Map<String, Object> params);

    /**
     * 목적: 승인 이력 개수를 조회한다.
     * 기능: 검색 조건에 해당하는 총 건수를 반환한다.
     * 이유: 페이징 계산에 사용하기 위함이다.
     * 유지보수: 조건 추가 시 SQL을 수정한다.
     */
    int countHistory(@Param("params") Map<String, Object> params);
}
