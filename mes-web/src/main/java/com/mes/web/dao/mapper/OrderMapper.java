package com.mes.web.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 목적: 수주 매퍼 인터페이스를 정의한다.
 * 기능: 수주 조회/등록/수정/삭제 SQL을 선언한다.
 * 이유: DAO에서 안전하게 SQL을 호출하기 위함이다.
 * 유지보수: 컬럼/조건 변경 시 이 인터페이스와 XML을 함께 수정한다.
 */
public interface OrderMapper {

    /**
     * 목적: 수주 목록을 조회한다.
     * 기능: 조건에 맞는 수주 목록을 반환한다.
     * 이유: 수주 화면 그리드 데이터를 제공하기 위함이다.
     * 유지보수: 조건 확정 시 파라미터 구조를 명확히 한다.
     */
    List<Map<String, Object>> selectOrders(@Param("criteria") Map<String, Object> criteria);

    /**
     * 목적: 수주를 등록한다.
     * 기능: 수주 기본 정보를 DB에 저장한다.
     * 이유: 수주 CRUD 흐름을 완성하기 위함이다.
     * 유지보수: 필수 컬럼 추가 시 파라미터를 보완한다.
     */
    int insertOrder(@Param("order") Map<String, Object> order);

    /**
     * 목적: 수주를 수정한다.
     * 기능: 수주 정보를 갱신한다.
     * 이유: 변경 흐름을 지원하기 위함이다.
     * 유지보수: 수정 허용 컬럼 변경 시 SQL을 조정한다.
     */
    int updateOrder(@Param("order") Map<String, Object> order);

    /**
     * 목적: 수주를 삭제한다.
     * 기능: 수주 번호 기준으로 삭제한다.
     * 이유: 삭제 요청을 처리하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 조정한다.
     */
    int deleteOrder(@Param("orderNo") String orderNo);
}
