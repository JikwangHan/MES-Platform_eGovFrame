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

import com.mes.web.service.InventoryService;

/**
 * 목적: 재고 관련 화면을 제공한다.
 * 기능: 재고현황/입고/출고/소요산출 화면을 반환한다.
 * 이유: 재고 흐름 CRUD 화면을 제공하기 위함이다.
 * 유지보수: 계산 로직 연결 시 서비스 호출을 확장한다.
 */
@Controller
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 목적: 재고 서비스를 주입받는다.
     * 기능: 재고 조회/등록/수정/삭제를 호출할 수 있게 한다.
     * 이유: 화면과 서비스 로직을 연결하기 위함이다.
     * 유지보수: 서비스 교체 시 주입만 변경한다.
     */
    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

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

    /**
     * 목적: 재고 현황을 조회한다.
     * 기능: 조건에 맞는 재고 목록을 반환한다.
     * 이유: 재고 그리드 조회 기능을 제공하기 위함이다.
     * 유지보수: 검색 조건 확정 시 파라미터를 보완한다.
     */
    @PostMapping("/api/inventory/list")
    @ResponseBody
    public Map<String, Object> list(@RequestParam Map<String, Object> criteria) {
        List<Map<String, Object>> inventories = inventoryService.findInventoryStatus(criteria);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", "success");
        result.put("data", inventories);
        return result;
    }

    /**
     * 목적: 재고를 등록한다.
     * 기능: 재고 정보를 저장하고 결과를 반환한다.
     * 이유: 재고 CRUD 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 필드 확정 시 검증 로직을 추가한다.
     */
    @PostMapping("/api/inventory/create")
    @ResponseBody
    public Map<String, Object> create(@RequestParam Map<String, Object> inventory) {
        int count = inventoryService.createInventoryStatus(inventory);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 재고를 수정한다.
     * 기능: 재고 정보를 갱신하고 결과를 반환한다.
     * 이유: 재고 CRUD 수정 기능을 제공하기 위함이다.
     * 유지보수: 수정 가능 필드 확정 시 검증 로직을 추가한다.
     */
    @PostMapping("/api/inventory/update")
    @ResponseBody
    public Map<String, Object> update(@RequestParam Map<String, Object> inventory) {
        int count = inventoryService.updateInventoryStatus(inventory);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 재고를 삭제한다.
     * 기능: 재고 ID 기준으로 삭제한다.
     * 이유: 재고 CRUD 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 로직을 보완한다.
     */
    @PostMapping("/api/inventory/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("id") long id) {
        int count = inventoryService.deleteInventoryStatus(id);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }
}
