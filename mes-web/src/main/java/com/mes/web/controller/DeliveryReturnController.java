package com.mes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 목적: 납품/반품 화면을 제공한다.
 * 기능: 납품내역/반품내역 페이지로 이동한다.
 * 이유: 라우트 기준을 통일하기 위함이다.
 * 유지보수: 연동 기능 추가 시 모델 값을 확장한다.
 */
@Controller
public class DeliveryReturnController {

    /**
     * 목적: 납품내역 화면을 반환한다.
     * 기능: /deliveries 요청을 JSP로 연결한다.
     * 이유: 납품 내역 관리 화면을 제공하기 위함이다.
     * 유지보수: 데이터 연동 시 서비스 호출을 추가한다.
     */
    @GetMapping("/deliveries")
    public String deliveries() {
        return "deliveries/index";
    }

    /**
     * 목적: 반품내역 화면을 반환한다.
     * 기능: /returns 요청을 JSP로 연결한다.
     * 이유: 반품 내역 관리 화면을 제공하기 위함이다.
     * 유지보수: 검증 규칙 변경 시 서비스 로직을 보강한다.
     */
    @GetMapping("/returns")
    public String returns() {
        return "returns/index";
    }
}
