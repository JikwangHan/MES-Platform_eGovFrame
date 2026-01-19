package com.mes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 목적: 재고 관련 화면을 제공한다.
 * 기능: 재고현황/입고/출고/소요산출 화면을 반환한다.
 * 이유: 재고 흐름 CRUD 화면을 제공하기 위함이다.
 * 유지보수: 계산 로직 연결 시 서비스 호출을 확장한다.
 */
@Controller
public class InventoryController {

    /**
     * 목적: 재고현황 화면을 반환한다.
     * 기능: /inventory/status 요청을 JSP로 연결한다.
     * 이유: 재고 조회 화면을 제공하기 위함이다.
     * 유지보수: 필터 추가 시 모델 값을 보강한다.
     */
    @GetMapping("/inventory/status")
    public String inventoryStatus() {
        return "inventory/status";
    }

    /**
     * 목적: 입고내역 화면을 반환한다.
     * 기능: /inventory/inbound 요청을 JSP로 연결한다.
     * 이유: 입고 전표 관리 화면을 제공하기 위함이다.
     * 유지보수: 모달 입력 확장 시 UI를 수정한다.
     */
    @GetMapping("/inventory/inbound")
    public String inventoryInbound() {
        return "inventory/inbound";
    }

    /**
     * 목적: 출고내역 화면을 반환한다.
     * 기능: /inventory/outbound 요청을 JSP로 연결한다.
     * 이유: 출고 전표 관리 화면을 제공하기 위함이다.
     * 유지보수: 검증 규칙 추가 시 서비스 로직을 확장한다.
     */
    @GetMapping("/inventory/outbound")
    public String inventoryOutbound() {
        return "inventory/outbound";
    }

    /**
     * 목적: 소요산출 화면을 반환한다.
     * 기능: /inventory/requirements 요청을 JSP로 연결한다.
     * 이유: 소요산출 실행/결과 화면을 제공하기 위함이다.
     * 유지보수: 결과 탭 추가 시 UI를 확장한다.
     */
    @GetMapping("/inventory/requirements")
    public String inventoryRequirements() {
        return "inventory/requirements";
    }
}
