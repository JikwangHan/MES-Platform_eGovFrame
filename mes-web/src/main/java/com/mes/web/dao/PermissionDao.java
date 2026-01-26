package com.mes.web.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mes.web.dao.mapper.PermissionMapper;

/**
 * 목적: 권한 데이터 접근을 담당한다.
 * 기능: 역할별 권한 조회/저장 처리를 수행한다.
 * 이유: 권한 관리 기능을 DB 기반으로 운영하기 위함이다.
 * 유지보수: 권한 테이블 구조 변경 시 DAO/매퍼를 함께 수정한다.
 */
@Repository
public class PermissionDao {

    private final PermissionMapper permissionMapper;

    /**
     * 목적: 권한 매퍼를 주입받는다.
     * 기능: DAO 내부에서 매퍼를 사용할 수 있게 한다.
     * 이유: 데이터 접근을 인터페이스로 분리하기 위함이다.
     * 유지보수: 매퍼 교체 시 주입만 변경한다.
     */
    @Autowired
    public PermissionDao(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /**
     * 목적: 역할별 권한 목록을 조회한다.
     * 기능: role_code 기준으로 권한 허용 목록을 반환한다.
     * 이유: 권한 검증과 화면 표시를 위해 필요하다.
     * 유지보수: 조회 조건 확장 시 파라미터를 보완한다.
     */
    public List<Map<String, Object>> selectRolePermissions(String roleCode) {
        return permissionMapper.selectRolePermissions(roleCode);
    }

    /**
     * 목적: 역할 권한을 초기화한다.
     * 기능: 지정된 역할의 권한을 모두 삭제한다.
     * 이유: 저장 시 기존 권한을 덮어쓰기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 수정한다.
     */
    public void deleteRolePermissions(String roleCode) {
        permissionMapper.deleteRolePermissions(roleCode);
    }

    /**
     * 목적: 역할 권한을 저장한다.
     * 기능: 역할별 권한 목록을 일괄 삽입한다.
     * 이유: 권한 정책을 DB에 반영하기 위함이다.
     * 유지보수: 저장 필드 확장 시 맵 구조를 보완한다.
     */
    public void insertRolePermissions(String roleCode, Map<String, Boolean> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        List<Map<String, Object>> rows = new java.util.ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("roleCode", roleCode);
            row.put("permKey", entry.getKey());
            row.put("allowed", entry.getValue() != null && entry.getValue() ? 1 : 0);
            rows.add(row);
        }
        permissionMapper.insertRolePermissions(rows);
    }
}
