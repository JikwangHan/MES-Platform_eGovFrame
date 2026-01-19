package com.mes.web.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mes.web.dao.OrderDao;
import com.mes.web.service.OrderService;

/**
 * 목적: 수주 서비스의 기본 구현을 제공한다.
 * 기능: 임시로 빈 목록을 반환한다.
 * 이유: 화면 골격을 먼저 완성하고 데이터 연동을 단계적으로 진행하기 위함이다.
 * 유지보수: 실제 DAO 연동 시 구현을 확장한다.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;

    /**
     * 목적: DAO를 주입받는다.
     * 기능: 서비스에서 DAO를 사용할 수 있게 한다.
     * 이유: 데이터 접근과 비즈니스 로직을 분리하기 위함이다.
     * 유지보수: DAO 교체 시 주입만 변경한다.
     */
    @Autowired
    public OrderServiceImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    /**
     * 목적: 수주 목록을 조회한다.
     * 기능: DAO를 통해 수주 목록을 조회한다.
     * 이유: DB 연동 구조를 미리 확보하기 위함이다.
     * 유지보수: 조건 확정 시 criteria 구조를 보완한다.
     */
    @Override
    public List<Map<String, Object>> findOrders(Map<String, Object> criteria) {
        return orderDao.selectOrders(criteria);
    }
}
