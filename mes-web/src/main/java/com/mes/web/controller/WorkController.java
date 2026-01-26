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
import com.mes.web.service.WorkService;

/**
 * 목적: 작업 관련 화면을 제공한다.
 * 기능: 작업현황/작업관리/작업지시 화면을 반환한다.
 * 이유: 작업 흐름 화면을 기준 문서와 일치시키기 위함이다.
 * 유지보수: 공정 분배 기능 확장 시 모델 값을 추가한다.
 */
@Controller
public class WorkController {

    /**
     * 목적: 작업 상태 허용 값을 정의한다.
     * 기능: 조회/상태 변경 요청에서 허용 값 검증에 사용한다.
     * 이유: 상태 코드 오입력을 사전에 차단하기 위함이다.
     * 유지보수: 상태 코드 확정 시 목록을 조정한다.
     */
    private static final String[] WORK_STATUS_ALLOWED = new String[] {
        "planned", "released", "in_progress", "completed", "hold", "canceled"
    };

    private final WorkService workService;
    private final AuditLogService auditLogService;

    /**
     * 목적: 작업 서비스를 주입받는다.
     * 기능: 작업 조회/등록/수정/삭제를 호출할 수 있게 한다.
     * 이유: 화면과 서비스 로직을 연결하기 위함이다.
     * 유지보수: 서비스 교체 시 주입만 변경한다.
     */
    @Autowired
    public WorkController(WorkService workService, AuditLogService auditLogService) {
        this.workService = workService;
        this.auditLogService = auditLogService;
    }

    /**
     * 목적: 작업현황 화면을 반환한다.
     * 기능: /work/status 요청을 JSP로 연결한다.
     * 이유: 작업 상태 조회 화면을 제공하기 위함이다.
     * 유지보수: KPI 데이터 연결 시 서비스 호출을 추가한다.
     */
    @GetMapping("/work/status")
    public String workStatus() {
        return "work/status";
    }

    /**
     * 목적: 작업관리 화면을 반환한다.
     * 기능: /work/orders 요청을 JSP로 연결한다.
     * 이유: 작업 관리 CRUD 화면을 제공하기 위함이다.
     * 유지보수: 공정 분배 모달 연결 시 UI를 확장한다.
     */
    @GetMapping("/work/orders")
    public String workOrders() {
        return "work/orders";
    }

    /**
     * 목적: 작업지시 화면을 반환한다.
     * 기능: /work/orders/issue 요청을 JSP로 연결한다.
     * 이유: 작업 지시 관리 화면을 제공하기 위함이다.
     * 유지보수: 파일 첨부 기능 추가 시 컨트롤러를 확장한다.
     */
    @GetMapping("/work/orders/issue")
    public String workIssue() {
        return "work/issue";
    }

    /**
     * 목적: 작업 목록을 조회한다.
     * 기능: 조건에 맞는 작업 목록을 반환한다.
     * 이유: 작업 그리드 조회 기능을 제공하기 위함이다.
     * 유지보수: 검색 조건 확정 시 파라미터를 보완한다.
     */
    @PostMapping("/api/work/list")
    @ResponseBody
    public Map<String, Object> list(@RequestParam Map<String, Object> criteria) {
        String validationError = validateList(criteria);
        if (validationError != null) {
            return buildFail(validationError);
        }
        CriteriaUtils.applyPaging(criteria, 50, 200);
        List<Map<String, Object>> works = workService.findWorkOrders(criteria);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", "success");
        result.put("data", works);
        return result;
    }

    /**
     * 목적: 작업을 등록한다.
     * 기능: 작업 정보를 저장하고 결과를 반환한다.
     * 이유: 작업 CRUD 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 필드 확정 시 검증 로직을 추가한다.
     */
    @PostMapping("/api/work/create")
    @ResponseBody
    public Map<String, Object> create(@RequestParam Map<String, Object> work) {
        String validationError = validateCreate(work);
        if (validationError != null) {
            return buildFail(validationError);
        }
        normalizeNumbers(work);
        int count = workService.createWorkOrder(work);
        auditLogService.logEvent("work_create", count > 0 ? "success" : "fail", getUserId(work),
                "workNo=" + work.get("workNo"));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 작업을 수정한다.
     * 기능: 작업 정보를 갱신하고 결과를 반환한다.
     * 이유: 작업 CRUD 수정 기능을 제공하기 위함이다.
     * 유지보수: 수정 가능 필드 확정 시 검증 로직을 추가한다.
     */
    @PostMapping("/api/work/update")
    @ResponseBody
    public Map<String, Object> update(@RequestParam Map<String, Object> work) {
        String validationError = validateUpdate(work);
        if (validationError != null) {
            return buildFail(validationError);
        }
        normalizeNumbers(work);
        int count = workService.updateWorkOrder(work);
        auditLogService.logEvent("work_update", count > 0 ? "success" : "fail", getUserId(work),
                "workNo=" + work.get("workNo"));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 작업 상태를 변경한다.
     * 기능: 작업 상태 값을 갱신하고 결과를 반환한다.
     * 이유: 작업 흐름 제어 버튼을 지원하기 위함이다.
     * 유지보수: 상태 코드 체계 변경 시 검증 로직을 보완한다.
     */
    @PostMapping("/api/work/status")
    @ResponseBody
    public Map<String, Object> updateStatus(@RequestParam("workNo") String workNo,
                                            @RequestParam("status") String status) {
        if (ValidationUtils.isBlank(workNo)) {
            return buildFail("작업번호는 필수입니다.");
        }
        if (ValidationUtils.isBlank(status)) {
            return buildFail("상태는 필수입니다.");
        }
        Map<String, Object> validation = new HashMap<String, Object>();
        validation.put("status", status);
        String message = ValidationUtils.validateIn(validation, "status", "상태", WORK_STATUS_ALLOWED);
        if (message != null) {
            return buildFail(message);
        }
        int count = workService.updateWorkStatus(workNo, status);
        auditLogService.logEvent("work_status", count > 0 ? "success" : "fail", null,
                "workNo=" + workNo + ",status=" + status);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 작업을 삭제한다.
     * 기능: 작업 번호 기준으로 삭제한다.
     * 이유: 작업 CRUD 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 로직을 보완한다.
     */
    @PostMapping("/api/work/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("workNo") String workNo) {
        if (ValidationUtils.isBlank(workNo)) {
            return buildFail("작업번호는 필수입니다.");
        }
        int count = workService.deleteWorkOrder(workNo);
        auditLogService.logEvent("work_delete", count > 0 ? "success" : "fail", null, "workNo=" + workNo);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 작업 등록 필수 값을 검증한다.
     * 기능: 필수 값 누락 시 오류 메시지를 반환한다.
     * 이유: 잘못된 입력을 사전에 차단하기 위함이다.
     * 유지보수: 필수 값 변경 시 항목을 조정한다.
     */
    private String validateCreate(Map<String, Object> work) {
        String message = ValidationUtils.require(work, "workNo", "작업번호");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(work, "orderId", "수주 ID");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(work, "planQty", "계획 수량");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(work, "planStartDate", "계획 시작일");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(work, "planEndDate", "계획 종료일");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(work, "orderId", "수주 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(work, "planQty", "계획 수량", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(work, "ownerId", "담당자 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        return null;
    }

    /**
     * 목적: 작업 수정 필수 값을 검증한다.
     * 기능: 필수 값 누락 시 오류 메시지를 반환한다.
     * 이유: 수정 대상이 없는 상태를 방지하기 위함이다.
     * 유지보수: 필수 값 변경 시 항목을 조정한다.
     */
    private String validateUpdate(Map<String, Object> work) {
        String message = ValidationUtils.require(work, "workNo", "작업번호");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(work, "planStartDate", "계획 시작일");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(work, "planEndDate", "계획 종료일");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(work, "orderId", "수주 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(work, "planQty", "계획 수량", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(work, "ownerId", "담당자 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        return null;
    }

    /**
     * 목적: 작업 조회 조건을 검증한다.
     * 기능: 날짜 조건 형식을 점검한다.
     * 이유: 잘못된 조회 파라미터로 인한 오류를 방지하기 위함이다.
     * 유지보수: 조회 조건 확장 시 검증 항목을 추가한다.
     */
    private String validateList(Map<String, Object> criteria) {
        String message = ValidationUtils.validateDate(criteria, "fromDate", "조회 시작일");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(criteria, "toDate", "조회 종료일");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDateRange(criteria, "fromDate", "toDate", "조회 기간");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateIn(criteria, "status", "상태", WORK_STATUS_ALLOWED);
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
    private void normalizeNumbers(Map<String, Object> work) {
        ValidationUtils.normalizeInt(work, "orderId");
        ValidationUtils.normalizeInt(work, "planQty");
        ValidationUtils.normalizeInt(work, "ownerId");
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
    private String getUserId(Map<String, Object> work) {
        Object userId = work.get("userId");
        if (userId == null) {
            return null;
        }
        return userId.toString();
    }
}
