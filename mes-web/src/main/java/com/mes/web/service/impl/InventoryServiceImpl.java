package com.mes.web.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mes.web.dao.InventoryDao;
import com.mes.web.service.InventoryService;

/**
 * 목적: 재고 서비스의 기본 구현을 제공한다.
 * 기능: 재고 CRUD 로직을 DAO로 위임한다.
 * 이유: 서비스 계층에서 흐름을 통제하기 위함이다.
 * 유지보수: 추가 검증 로직이 필요하면 여기에서 보완한다.
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryDao inventoryDao;

    /**
     * 목적: DAO를 주입받는다.
     * 기능: 서비스에서 DAO를 사용할 수 있게 한다.
     * 이유: 데이터 접근과 비즈니스 로직을 분리하기 위함이다.
     * 유지보수: DAO 교체 시 주입만 변경한다.
     */
    @Autowired
    public InventoryServiceImpl(InventoryDao inventoryDao) {
        this.inventoryDao = inventoryDao;
    }

    /**
     * 목적: 재고 현황을 조회한다.
     * 기능: DAO를 통해 재고 현황을 조회한다.
     * 이유: DB 연동 구조를 유지하기 위함이다.
     * 유지보수: 조건 확정 시 criteria 구조를 보완한다.
     */
    @Override
    public List<Map<String, Object>> findInventoryStatus(Map<String, Object> criteria) {
        return inventoryDao.selectInventoryStatus(criteria);
    }

    /**
     * 목적: 재고를 등록한다.
     * 기능: DAO를 통해 재고 정보를 저장한다.
     * 이유: 등록 기능을 제공하기 위함이다.
     * 유지보수: 검증 규칙이 추가되면 여기서 처리한다.
     */
    @Override
    public int createInventoryStatus(Map<String, Object> inventory) {
        return inventoryDao.insertInventoryStatus(inventory);
    }

    /**
     * 목적: 재고를 수정한다.
     * 기능: DAO를 통해 재고 수량과 상태를 갱신한다.
     * 이유: 입출고 흐름을 반영하기 위함이다.
     * 유지보수: 수정 조건 확정 시 로직을 보완한다.
     */
    @Override
    public int updateInventoryStatus(Map<String, Object> inventory) {
        return inventoryDao.updateInventoryStatus(inventory);
    }

    /**
     * 목적: 재고를 삭제한다.
     * 기능: DAO를 통해 재고를 삭제한다.
     * 이유: 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 로직을 보완한다.
     */
    @Override
    public int deleteInventoryStatus(long id) {
        return inventoryDao.deleteInventoryStatus(id);
    }
}
