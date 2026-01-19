package com.mes.web.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 목적: 재고 매퍼 인터페이스를 정의한다.
 * 기능: 재고 조회/등록/수정/삭제 SQL을 선언한다.
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

    /**
     * 목적: 재고를 등록한다.
     * 기능: 재고 정보를 저장한다.
     * 이유: 재고 초기 등록을 지원하기 위함이다.
     * 유지보수: 필수 컬럼 추가 시 파라미터를 보완한다.
     */
    int insertInventoryStatus(@Param("inventory") Map<String, Object> inventory);

    /**
     * 목적: 재고를 수정한다.
     * 기능: 재고 수량/상태를 갱신한다.
     * 이유: 입출고 반영을 위해 필요하다.
     * 유지보수: 변경 컬럼 확정 시 SQL을 조정한다.
     */
    int updateInventoryStatus(@Param("inventory") Map<String, Object> inventory);

    /**
     * 목적: 재고를 삭제한다.
     * 기능: 재고 ID 기준으로 삭제한다.
     * 이유: 삭제 요청을 처리하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 조정한다.
     */
    int deleteInventoryStatus(@Param("id") long id);
}
