package com.mes.web.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 목적: 역할 매퍼 인터페이스를 정의한다.
 * 기능: 역할 목록 조회/등록/삭제와 사용자 수 조회를 선언한다.
 * 이유: 역할 관리 기능을 DB와 연결하기 위함이다.
 * 유지보수: 역할 테이블 구조 변경 시 SQL과 함께 수정한다.
 */
public interface RoleMapper {

    /**
     * 목적: 역할 목록을 조회한다.
     * 기능: 역할 정보를 리스트로 반환한다.
     * 이유: 관리자 화면에서 역할 목록을 표시하기 위함이다.
     * 유지보수: 필드 확장 시 반환 컬럼을 보완한다.
     */
    List<Map<String, Object>> selectRoles();

    /**
     * 목적: 역할을 등록한다.
     * 기능: 역할 코드/이름/설명을 저장한다.
     * 이유: 역할 추가 기능을 제공하기 위함이다.
     * 유지보수: 저장 컬럼 확장 시 파라미터를 보완한다.
     */
    int insertRole(@Param("params") Map<String, Object> params);

    /**
     * 목적: 역할을 삭제한다.
     * 기능: 역할 코드 기준으로 삭제한다.
     * 이유: 역할 정리 기능을 제공하기 위함이다.
     * 유지보수: 삭제 조건 변경 시 SQL을 수정한다.
     */
    int deleteRole(@Param("roleCode") String roleCode);

    /**
     * 목적: 역할에 연결된 사용자 수를 조회한다.
     * 기능: 역할 코드 기준 사용자 수를 반환한다.
     * 이유: 사용 중인 역할 삭제를 차단하기 위함이다.
     * 유지보수: 사용자 테이블 구조 변경 시 SQL을 보완한다.
     */
    Integer countUsersByRole(@Param("roleCode") String roleCode);
}
