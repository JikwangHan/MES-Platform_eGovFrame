package com.mes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 목적: 수주 관련 화면을 제공한다.
 * 기능: 수주현황/수주내역 페이지로 이동한다.
 * 이유: 라우트 매핑 기준을 준수하기 위함이다.
 * 유지보수: 화면 분리 시 매핑을 추가한다.
 */
@Controller
public class OrdersController {

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
}
