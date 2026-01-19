package com.mes.web.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 목적: 작업 매퍼 인터페이스를 정의한다.
 * 기능: 작업 조회/등록/수정/삭제 SQL을 선언한다.
 * 이유: DAO에서 안전하게 SQL을 호출하기 위함이다.
 * 유지보수: SQL 변경 시 이 인터페이스와 XML을 함께 수정한다.
 */
public interface WorkMapper {

    /**
     * 목적: 작업 목록을 조회한다.
     * 기능: 조건에 맞는 작업 목록을 반환한다.
     * 이유: 작업 화면 그리드 데이터를 제공하기 위함이다.
     * 유지보수: 조건 확정 시 파라미터 구조를 명확히 한다.
     */
    List<Map<String, Object>> selectWorkOrders(@Param("criteria") Map<String, Object> criteria);

    /**
     * 목적: 작업을 등록한다.
     * 기능: 작업 정보를 DB에 저장한다.
     * 이유: 작업 지시 흐름을 제공하기 위함이다.
     * 유지보수: 필수 컬럼 추가 시 파라미터를 보완한다.
     */
    int insertWorkOrder(@Param("work") Map<String, Object> work);

    /**
     * 목적: 작업을 수정한다.
     * 기능: 작업 정보를 갱신한다.
     * 이유: 변경 흐름을 제공하기 위함이다.
     * 유지보수: 수정 가능 컬럼 변경 시 SQL을 조정한다.
     */
    int updateWorkOrder(@Param("work") Map<String, Object> work);

    /**
     * 목적: 작업 상태를 수정한다.
     * 기능: 상태 값을 갱신한다.
     * 이유: 작업 흐름 제어를 지원하기 위함이다.
     * 유지보수: 상태 코드 체계 변경 시 SQL을 수정한다.
     */
    int updateWorkStatus(@Param("workNo") String workNo, @Param("status") String status);

    /**
     * 목적: 작업을 삭제한다.
     * 기능: 작업 번호 기준으로 삭제한다.
     * 이유: 삭제 요청을 처리하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 조정한다.
     */
    int deleteWorkOrder(@Param("workNo") String workNo);
}
