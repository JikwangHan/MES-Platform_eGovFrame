package com.mes.web.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mes.web.dao.mapper.InventoryMapper;

/**
 * 목적: 재고 데이터 접근을 담당한다.
 * 기능: 재고 조회/등록/수정/삭제를 수행한다.
 * 이유: 서비스 계층과 SQL 매퍼를 분리하기 위함이다.
 * 유지보수: 쿼리 변경 시 매퍼 XML과 함께 수정한다.
 */
@Repository
public class InventoryDao {

    private final InventoryMapper inventoryMapper;

    /**
     * 목적: 매퍼를 주입받는다.
     * 기능: DAO 내부에서 매퍼를 사용할 수 있게 한다.
     * 이유: 데이터 접근을 인터페이스로 분리하기 위함이다.
     * 유지보수: 매퍼 교체 시 주입만 변경한다.
     */
    @Autowired
    public InventoryDao(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    /**
     * 목적: 재고 현황을 조회한다.
     * 기능: 매퍼를 호출해 결과 목록을 반환한다.
     * 이유: 서비스 계층에서 데이터를 사용하기 위함이다.
     * 유지보수: 필터 확정 시 criteria 구조를 구체화한다.
     */
    public List<Map<String, Object>> selectInventoryStatus(Map<String, Object> criteria) {
        return inventoryMapper.selectInventoryStatus(criteria);
    }

    /**
     * 목적: 재고를 등록한다.
     * 기능: 재고 정보를 저장한다.
     * 이유: 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 컬럼 확정 시 파라미터를 보완한다.
     */
    public int insertInventoryStatus(Map<String, Object> inventory) {
        return inventoryMapper.insertInventoryStatus(inventory);
    }

    /**
     * 목적: 재고를 수정한다.
     * 기능: 재고 수량과 상태를 갱신한다.
     * 이유: 입출고 흐름을 반영하기 위함이다.
     * 유지보수: 수정 가능 컬럼 변경 시 SQL을 수정한다.
     */
    public int updateInventoryStatus(Map<String, Object> inventory) {
        return inventoryMapper.updateInventoryStatus(inventory);
    }

    /**
     * 목적: 재고를 삭제한다.
     * 기능: 재고 ID 기준으로 삭제한다.
     * 이유: 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 수정한다.
     */
    public int deleteInventoryStatus(long id) {
        return inventoryMapper.deleteInventoryStatus(id);
    }
}
