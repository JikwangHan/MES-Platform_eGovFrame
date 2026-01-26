package com.mes.web.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mes.web.dao.mapper.RoleMapper;

/**
 * 목적: 역할 데이터 접근을 담당한다.
 * 기능: 역할 목록 조회/등록/삭제 및 사용자 수 확인을 수행한다.
 * 이유: 역할 관리 기능을 DB와 연결하기 위함이다.
 * 유지보수: 테이블 구조 변경 시 DAO/매퍼를 함께 수정한다.
 */
@Repository
public class RoleDao {

    private final RoleMapper roleMapper;

    /**
     * 목적: 역할 매퍼를 주입받는다.
     * 기능: DAO 내부에서 매퍼를 사용할 수 있게 한다.
     * 이유: 데이터 접근을 인터페이스로 분리하기 위함이다.
     * 유지보수: 매퍼 교체 시 주입만 변경한다.
     */
    @Autowired
    public RoleDao(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    /**
     * 목적: 역할 목록을 조회한다.
     * 기능: 역할 정보를 리스트로 반환한다.
     * 이유: 관리자 화면에서 역할 목록을 보여주기 위함이다.
     * 유지보수: 필드 확장 시 반환 컬럼을 보완한다.
     */
    public List<Map<String, Object>> selectRoles() {
        return roleMapper.selectRoles();
    }

    /**
     * 목적: 역할을 등록한다.
     * 기능: 역할 코드/이름/설명을 저장한다.
     * 이유: 새 역할 추가 기능을 제공하기 위함이다.
     * 유지보수: 필드 확장 시 저장 파라미터를 보완한다.
     */
    public int insertRole(String roleCode, String roleName, String roleDesc) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("roleCode", roleCode);
        params.put("roleName", roleName);
        params.put("roleDesc", roleDesc);
        return roleMapper.insertRole(params);
    }

    /**
     * 목적: 역할을 삭제한다.
     * 기능: 역할 코드 기준으로 삭제한다.
     * 이유: 사용하지 않는 역할을 제거하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 수정한다.
     */
    public int deleteRole(String roleCode) {
        return roleMapper.deleteRole(roleCode);
    }

    /**
     * 목적: 역할에 연결된 사용자 수를 확인한다.
     * 기능: 역할 코드 기준 사용자 수를 반환한다.
     * 이유: 사용 중인 역할을 삭제하지 않도록 하기 위함이다.
     * 유지보수: 사용자 테이블 구조 변경 시 SQL을 보완한다.
     */
    public int countUsersByRole(String roleCode) {
        Integer count = roleMapper.countUsersByRole(roleCode);
        return count == null ? 0 : count.intValue();
    }
}
