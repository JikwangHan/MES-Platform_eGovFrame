package com.mes.web.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 목적: 품질 매퍼 인터페이스를 정의한다.
 * 기능: 불량 조회/등록/삭제 SQL을 선언한다.
 * 이유: DAO에서 안전하게 SQL을 호출하기 위함이다.
 * 유지보수: SQL 변경 시 이 인터페이스와 XML을 함께 수정한다.
 */
public interface QualityMapper {

    /**
     * 목적: 불량 내역을 조회한다.
     * 기능: 조건에 맞는 불량 목록을 반환한다.
     * 이유: 품질 화면 그리드 데이터를 제공하기 위함이다.
     * 유지보수: 조건 확정 시 파라미터 구조를 명확히 한다.
     */
    List<Map<String, Object>> selectDefects(@Param("criteria") Map<String, Object> criteria);

    /**
     * 목적: 불량을 등록한다.
     * 기능: 불량 정보를 저장한다.
     * 이유: 불량 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 컬럼 확정 시 파라미터를 보완한다.
     */
    int insertDefect(@Param("defect") Map<String, Object> defect);

    /**
     * 목적: 불량을 삭제한다.
     * 기능: 불량 ID 기준으로 삭제한다.
     * 이유: 삭제 요청을 처리하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 조정한다.
     */
    int deleteDefect(@Param("id") long id);
}
