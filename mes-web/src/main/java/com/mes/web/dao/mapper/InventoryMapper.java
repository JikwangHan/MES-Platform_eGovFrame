package com.mes.web.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 목적: 재고 매퍼 인터페이스를 정의한다.
 * 기능: SQL 매퍼와 연결되는 메서드를 선언한다.
 * 이유: DAO에서 안전하게 SQL을 호출하기 위함이다.
 * 유지보수: SQL 변경 시 이 인터페이스와 XML을 함께 수정한다.
 */
public interface InventoryMapper {

    /**
     * 목적: 재고 현황을 조회한다.
     * 기능: 조건에 맞는 재고 목록을 반환한다.
     * 이유: 재고 화면 그리드 데이터를 제공하기 위함이다.
     * 유지보수: 조건 확정 시 파라미터 구조를 명확히 한다.
     */
    List<Map<String, Object>> selectInventoryStatus(@Param("criteria") Map<String, Object> criteria);
}
