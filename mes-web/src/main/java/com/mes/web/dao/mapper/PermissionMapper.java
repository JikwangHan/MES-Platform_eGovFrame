package com.mes.web.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * 목적: 권한 매퍼 인터페이스를 정의한다.
 * 기능: 역할별 권한 조회/삭제/저장을 선언한다.
 * 이유: 권한 관리 기능을 DB와 연결하기 위함이다.
 * 유지보수: 권한 컬럼 확장 시 SQL과 함께 수정한다.
 */
public interface PermissionMapper {

    /**
     * 목적: 역할별 권한을 조회한다.
     * 기능: role_code 기준 권한 목록을 반환한다.
     * 이유: 권한 검증과 화면 표시를 위해 필요하다.
     * 유지보수: 조회 조건 확장 시 파라미터를 보완한다.
     */
    List<Map<String, Object>> selectRolePermissions(@Param("roleCode") String roleCode);

    /**
     * 목적: 역할 권한을 삭제한다.
     * 기능: 지정된 역할의 권한을 모두 제거한다.
     * 이유: 저장 시 기존 권한을 교체하기 위함이다.
     * 유지보수: 삭제 조건 변경 시 SQL을 수정한다.
     */
    void deleteRolePermissions(@Param("roleCode") String roleCode);

    /**
     * 목적: 역할 권한을 저장한다.
     * 기능: 역할별 권한 목록을 일괄 삽입한다.
     * 이유: 권한 정책을 DB에 반영하기 위함이다.
     * 유지보수: 저장 구조 변경 시 파라미터를 보완한다.
     */
    void insertRolePermissions(@Param("rows") List<Map<String, Object>> rows);
}
