package com.mes.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mes.web.service.OrderService;

/**
 * 목적: 수주 관련 화면을 제공한다.
 * 기능: 수주현황/수주내역 페이지로 이동한다.
 * 이유: 라우트 매핑 기준을 준수하기 위함이다.
 * 유지보수: 화면 분리 시 매핑을 추가한다.
 */
@Controller
public class OrdersController {

    private final OrderService orderService;

    /**
     * 목적: 수주 서비스를 주입받는다.
     * 기능: 수주 조회/등록/수정/삭제를 호출할 수 있게 한다.
     * 이유: 화면과 서비스 로직을 연결하기 위함이다.
     * 유지보수: 서비스 교체 시 주입만 변경한다.
     */
    @Autowired
    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 목적: 수주현황 화면을 반환한다.
     * 기능: /orders/summary 요청을 JSP로 연결한다.
     * 이유: 수주 현황 조회 화면을 제공하기 위함이다.
     * 유지보수: 검색 조건 추가 시 모델 값을 보강한다.
     */
    @GetMapping("/orders/summary")
    public String ordersSummary() {
        return "orders/summary";
    }

    /**
     * 목적: 수주내역 화면을 반환한다.
     * 기능: /orders 요청을 JSP로 연결한다.
     * 이유: 수주 내역 CRUD 화면을 제공하기 위함이다.
     * 유지보수: 모달 구조 변경 시 JSP 구성을 수정한다.
     */
    @GetMapping("/orders")
    public String orders() {
        return "orders/index";
    }

    /**
     * 목적: 수주 목록을 조회한다.
     * 기능: 조건에 맞는 수주 목록을 반환한다.
     * 이유: 화면 그리드 조회 기능을 제공하기 위함이다.
     * 유지보수: 검색 조건 확정 시 파라미터를 보완한다.
     */
    @PostMapping("/api/orders/list")
    @ResponseBody
    public Map<String, Object> list(@RequestParam Map<String, Object> criteria) {
        List<Map<String, Object>> orders = orderService.findOrders(criteria);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", "success");
        result.put("data", orders);
        return result;
    }

    /**
     * 목적: 수주를 등록한다.
     * 기능: 수주 정보를 저장하고 결과를 반환한다.
     * 이유: 수주 CRUD 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 필드 확정 시 검증 로직을 추가한다.
     */
    @PostMapping("/api/orders/create")
    @ResponseBody
    public Map<String, Object> create(@RequestParam Map<String, Object> order) {
        int count = orderService.createOrder(order);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 수주를 수정한다.
     * 기능: 수주 정보를 갱신하고 결과를 반환한다.
     * 이유: 수주 CRUD 수정 기능을 제공하기 위함이다.
     * 유지보수: 수정 가능 필드 확정 시 검증 로직을 추가한다.
     */
    @PostMapping("/api/orders/update")
    @ResponseBody
    public Map<String, Object> update(@RequestParam Map<String, Object> order) {
        int count = orderService.updateOrder(order);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 수주를 삭제한다.
     * 기능: 수주 번호 기준으로 삭제한다.
     * 이유: 수주 CRUD 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 로직을 보완한다.
     */
    @PostMapping("/api/orders/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("orderNo") String orderNo) {
        int count = orderService.deleteOrder(orderNo);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }
}
