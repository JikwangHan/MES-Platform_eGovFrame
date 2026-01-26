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
    private final AuditLogService auditLogService;

    /**
     * 목적: 수주 서비스를 주입받는다.
     * 기능: 수주 조회/등록/수정/삭제를 호출할 수 있게 한다.
     * 이유: 화면과 서비스 로직을 연결하기 위함이다.
     * 유지보수: 서비스 교체 시 주입만 변경한다.
     */
    @Autowired
    public OrdersController(OrderService orderService, AuditLogService auditLogService) {
        this.orderService = orderService;
        this.auditLogService = auditLogService;
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
        CriteriaUtils.applyPaging(criteria, 50, 200);
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
        String validationError = validateCreate(order);
        if (validationError != null) {
            return buildFail(validationError);
        }
        normalizeNumbers(order);
        int count = orderService.createOrder(order);
        auditLogService.logEvent("order_create", count > 0 ? "success" : "fail", getUserId(order),
                "orderNo=" + order.get("orderNo"));
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
        String validationError = validateUpdate(order);
        if (validationError != null) {
            return buildFail(validationError);
        }
        normalizeNumbers(order);
        int count = orderService.updateOrder(order);
        auditLogService.logEvent("order_update", count > 0 ? "success" : "fail", getUserId(order),
                "orderNo=" + order.get("orderNo"));
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
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return buildFail("수주번호는 필수입니다.");
        }
        int count = orderService.deleteOrder(orderNo);
        auditLogService.logEvent("order_delete", count > 0 ? "success" : "fail", null, "orderNo=" + orderNo);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 수주 등록 필수 값을 검증한다.
     * 기능: 필수 값 누락 시 오류 메시지를 반환한다.
     * 이유: 잘못된 입력을 사전에 차단하기 위함이다.
     * 유지보수: 필수 값 변경 시 항목을 조정한다.
     */
    private String validateCreate(Map<String, Object> order) {
        String message = ValidationUtils.require(order, "orderNo", "수주번호");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(order, "orderDate", "수주일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(order, "itemId", "품목 ID");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(order, "orderQty", "수주수량");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(order, "orderDate", "수주일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(order, "dueDate", "납기일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(order, "itemId", "품목 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(order, "orderQty", "수주수량", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(order, "partnerId", "거래처 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(order, "ownerId", "담당자 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        return null;
    }

    /**
     * 목적: 수주 수정 필수 값을 검증한다.
     * 기능: 필수 값 누락 시 오류 메시지를 반환한다.
     * 이유: 수정 대상이 없는 상태를 방지하기 위함이다.
     * 유지보수: 필수 값 변경 시 항목을 조정한다.
     */
    private String validateUpdate(Map<String, Object> order) {
        String message = ValidationUtils.require(order, "orderNo", "수주번호");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(order, "orderDate", "수주일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(order, "dueDate", "납기일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(order, "itemId", "품목 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(order, "orderQty", "수주수량", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(order, "partnerId", "거래처 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(order, "ownerId", "담당자 ID", 1, Integer.MAX_VALUE);
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
    private void normalizeNumbers(Map<String, Object> order) {
        ValidationUtils.normalizeInt(order, "itemId");
        ValidationUtils.normalizeInt(order, "orderQty");
        ValidationUtils.normalizeInt(order, "partnerId");
        ValidationUtils.normalizeInt(order, "ownerId");
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
    private String getUserId(Map<String, Object> order) {
        Object userId = order.get("userId");
        if (userId == null) {
            return null;
        }
        return userId.toString();
    }
}
