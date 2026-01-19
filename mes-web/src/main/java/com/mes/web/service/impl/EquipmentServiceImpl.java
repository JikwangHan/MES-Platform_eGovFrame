package com.mes.web.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mes.web.dao.EquipmentDao;
import com.mes.web.service.EquipmentService;

/**
 * 목적: 설비 서비스의 기본 구현을 제공한다.
 * 기능: 임시로 빈 목록을 반환한다.
 * 이유: 화면 골격을 먼저 확보하기 위함이다.
 * 유지보수: 모니터링/제어 연동 시 확장한다.
 */
@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentDao equipmentDao;

    /**
     * 목적: DAO를 주입받는다.
     * 기능: 서비스에서 DAO를 사용할 수 있게 한다.
     * 이유: 데이터 접근과 비즈니스 로직을 분리하기 위함이다.
     * 유지보수: DAO 교체 시 주입만 변경한다.
     */
    @Autowired
    public EquipmentServiceImpl(EquipmentDao equipmentDao) {
        this.equipmentDao = equipmentDao;
    }

    /**
     * 목적: 설비 현황을 조회한다.
     * 기능: DAO를 통해 설비 현황을 조회한다.
     * 이유: DB 연동 구조를 미리 확보하기 위함이다.
     * 유지보수: 조건 확정 시 criteria 구조를 보완한다.
     */
    @Override
    public List<Map<String, Object>> findEquipmentStatus(Map<String, Object> criteria) {
        return equipmentDao.selectEquipmentStatus(criteria);
    }
}
