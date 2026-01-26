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

import com.mes.web.common.audit.AuditLogService;
import com.mes.web.common.validation.CriteriaUtils;
import com.mes.web.common.validation.ValidationUtils;
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
    private final AuditLogService auditLogService;

    /**
     * 목적: 재고 서비스를 주입받는다.
     * 기능: 재고 조회/등록/수정/삭제를 호출할 수 있게 한다.
     * 이유: 화면과 서비스 로직을 연결하기 위함이다.
     * 유지보수: 서비스 교체 시 주입만 변경한다.
     */
    @Autowired
    public InventoryController(InventoryService inventoryService, AuditLogService auditLogService) {
        this.inventoryService = inventoryService;
        this.auditLogService = auditLogService;
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
        CriteriaUtils.applyPaging(criteria, 50, 200);
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
        String validationError = validateCreate(inventory);
        if (validationError != null) {
            return buildFail(validationError);
        }
        normalizeNumbers(inventory);
        int count = inventoryService.createInventoryStatus(inventory);
        auditLogService.logEvent("inventory_create", count > 0 ? "success" : "fail", getUserId(inventory),
                "itemId=" + inventory.get("itemId"));
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
        String validationError = validateUpdate(inventory);
        if (validationError != null) {
            return buildFail(validationError);
        }
        normalizeNumbers(inventory);
        int count = inventoryService.updateInventoryStatus(inventory);
        auditLogService.logEvent("inventory_update", count > 0 ? "success" : "fail", getUserId(inventory),
                "id=" + inventory.get("id"));
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
        if (id <= 0) {
            return buildFail("재고 ID는 필수입니다.");
        }
        int count = inventoryService.deleteInventoryStatus(id);
        auditLogService.logEvent("inventory_delete", count > 0 ? "success" : "fail", null, "id=" + id);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 재고 등록 필수 값을 검증한다.
     * 기능: 필수 값 누락 시 오류 메시지를 반환한다.
     * 이유: 잘못된 입력을 사전에 차단하기 위함이다.
     * 유지보수: 필수 값 변경 시 항목을 조정한다.
     */
    private String validateCreate(Map<String, Object> inventory) {
        String message = ValidationUtils.require(inventory, "itemId", "품목 ID");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(inventory, "warehouseId", "창고 ID");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(inventory, "stockQty", "재고 수량");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(inventory, "lastInDate", "입고일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(inventory, "lastOutDate", "출고일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(inventory, "itemId", "품목 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(inventory, "warehouseId", "창고 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(inventory, "stockQty", "재고 수량", 0, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        return null;
    }

    /**
     * 목적: 재고 수정 필수 값을 검증한다.
     * 기능: 필수 값 누락 시 오류 메시지를 반환한다.
     * 이유: 수정 대상이 없는 상태를 방지하기 위함이다.
     * 유지보수: 필수 값 변경 시 항목을 조정한다.
     */
    private String validateUpdate(Map<String, Object> inventory) {
        String message = ValidationUtils.require(inventory, "id", "재고 ID");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(inventory, "id", "재고 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(inventory, "lastInDate", "입고일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(inventory, "lastOutDate", "출고일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(inventory, "stockQty", "재고 수량", 0, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        return null;
    }

    /**
     * 목적: 숫자 필드를 정규화한다.
     * 기능: 숫자 문자열을 Integer로 변환해 저장한다.
     * 이유: DB 타입 일관성을 유지하기 위함이다.
     * 유지보수: 숫자 필드 추가 시 이 메서드를 보완한다.
     */
    private void normalizeNumbers(Map<String, Object> inventory) {
        ValidationUtils.normalizeInt(inventory, "id");
        ValidationUtils.normalizeInt(inventory, "itemId");
        ValidationUtils.normalizeInt(inventory, "warehouseId");
        ValidationUtils.normalizeInt(inventory, "stockQty");
    }
    /**
     * 목적: 공백 여부를 확인한다.
     * 기능: null 또는 빈 문자열인지 검사한다.
     * 이유: 입력 검증을 단순화하기 위함이다.
     * 유지보수: 검증 규칙 변경 시 로직을 보완한다.
     */
    private boolean isBlank(Object value) {
        return ValidationUtils.isBlank(value);
    }

    /**
     * 목적: 실패 응답을 생성한다.
     * 기능: 실패 결과와 메시지를 반환한다.
     * 이유: 응답 형식을 통일하기 위함이다.
     * 유지보수: 응답 포맷 변경 시 수정한다.
     */
    private Map<String, Object> buildFail(String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", "fail");
        result.put("message", message);
        return result;
    }

    /**
     * 목적: 사용자 ID를 추출한다.
     * 기능: 요청 파라미터에서 userId를 찾는다.
     * 이유: 감사 로그에 최소한의 사용자 정보를 남기기 위함이다.
     * 유지보수: 세션 기반 추적으로 변경 시 수정한다.
     */
    private String getUserId(Map<String, Object> inventory) {
        Object userId = inventory.get("userId");
        if (userId == null) {
            return null;
        }
        return userId.toString();
    }
}
