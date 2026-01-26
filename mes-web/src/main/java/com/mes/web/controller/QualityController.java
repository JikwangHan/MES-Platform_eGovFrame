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
import com.mes.web.service.QualityService;

/**
 * 목적: 품질 관련 화면을 제공한다.
 * 기능: 불량현황/불량내역/불량유형 화면을 반환한다.
 * 이유: 품질 관리 흐름을 제공하기 위함이다.
 * 유지보수: 그래프/통계 연결 시 데이터 모델을 확장한다.
 */
@Controller
public class QualityController {

    private final QualityService qualityService;
    private final AuditLogService auditLogService;

    /**
     * 목적: 품질 서비스를 주입받는다.
     * 기능: 불량 조회/등록/삭제를 호출할 수 있게 한다.
     * 이유: 화면과 서비스 로직을 연결하기 위함이다.
     * 유지보수: 서비스 교체 시 주입만 변경한다.
     */
    @Autowired
    public QualityController(QualityService qualityService, AuditLogService auditLogService) {
        this.qualityService = qualityService;
        this.auditLogService = auditLogService;
    }

    /**
     * 목적: 불량현황 화면을 반환한다.
     * 기능: /quality/defects/status 요청을 JSP로 연결한다.
     * 이유: 불량 현황 조회 화면을 제공하기 위함이다.
     * 유지보수: 차트 데이터 연동 시 서비스 호출을 추가한다.
     */
    @GetMapping("/quality/defects/status")
    public String defectStatus() {
        return "quality/defects_status";
    }

    /**
     * 목적: 불량내역 화면을 반환한다.
     * 기능: /quality/defects 요청을 JSP로 연결한다.
     * 이유: 불량 내역 관리 화면을 제공하기 위함이다.
     * 유지보수: 필터/검색 조건 추가 시 UI를 보완한다.
     */
    @GetMapping("/quality/defects")
    public String defects() {
        return "quality/defects";
    }

    /**
     * 목적: 불량유형 화면을 반환한다.
     * 기능: /quality/defect-types 요청을 JSP로 연결한다.
     * 이유: 불량 유형 트리 관리 화면을 제공하기 위함이다.
     * 유지보수: 유형 코드 정책 변경 시 UI를 수정한다.
     */
    @GetMapping("/quality/defect-types")
    public String defectTypes() {
        return "quality/defect_types";
    }

    /**
     * 목적: 불량 내역을 조회한다.
     * 기능: 조건에 맞는 불량 목록을 반환한다.
     * 이유: 품질 그리드 조회 기능을 제공하기 위함이다.
     * 유지보수: 검색 조건 확정 시 파라미터를 보완한다.
     */
    @PostMapping("/api/quality/defects/list")
    @ResponseBody
    public Map<String, Object> list(@RequestParam Map<String, Object> criteria) {
        String validationError = validateList(criteria);
        if (validationError != null) {
            return buildFail(validationError);
        }
        CriteriaUtils.applyPaging(criteria, 50, 200);
        List<Map<String, Object>> defects = qualityService.findDefects(criteria);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", "success");
        result.put("data", defects);
        return result;
    }

    /**
     * 목적: 불량을 등록한다.
     * 기능: 불량 정보를 저장하고 결과를 반환한다.
     * 이유: 불량 CRUD 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 필드 확정 시 검증 로직을 추가한다.
     */
    @PostMapping("/api/quality/defects/create")
    @ResponseBody
    public Map<String, Object> create(@RequestParam Map<String, Object> defect) {
        String validationError = validateCreate(defect);
        if (validationError != null) {
            return buildFail(validationError);
        }
        normalizeNumbers(defect);
        int count = qualityService.createDefect(defect);
        auditLogService.logEvent("defect_create", count > 0 ? "success" : "fail", getUserId(defect),
                "itemId=" + defect.get("itemId"));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 불량을 삭제한다.
     * 기능: 불량 ID 기준으로 삭제한다.
     * 이유: 불량 CRUD 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 로직을 보완한다.
     */
    @PostMapping("/api/quality/defects/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("id") long id) {
        if (id <= 0) {
            return buildFail("불량 ID는 필수입니다.");
        }
        int count = qualityService.deleteDefect(id);
        auditLogService.logEvent("defect_delete", count > 0 ? "success" : "fail", null, "id=" + id);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 불량 등록 필수 값을 검증한다.
     * 기능: 필수 값 누락 시 오류 메시지를 반환한다.
     * 이유: 잘못된 입력을 사전에 차단하기 위함이다.
     * 유지보수: 필수 값 변경 시 항목을 조정한다.
     */
    private String validateCreate(Map<String, Object> defect) {
        String message = ValidationUtils.require(defect, "defectDate", "불량일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(defect, "itemId", "품목 ID");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(defect, "defectQty", "불량 수량");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateDate(defect, "defectDate", "불량일자");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(defect, "itemId", "품목 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(defect, "processId", "공정 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(defect, "equipmentId", "설비 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(defect, "defectTypeId", "불량 유형 ID", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(defect, "defectQty", "불량 수량", 1, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        return null;
    }

    /**
     * 목적: 불량 조회 조건을 검증한다.
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
        return null;
    }

    /**
     * 목적: 숫자 필드를 정규화한다.
     * 기능: 숫자 문자열을 Integer로 변환해 저장한다.
     * 이유: DB 타입 일관성을 유지하기 위함이다.
     * 유지보수: 숫자 필드 추가 시 이 메서드를 보완한다.
     */
    private void normalizeNumbers(Map<String, Object> defect) {
        ValidationUtils.normalizeInt(defect, "itemId");
        ValidationUtils.normalizeInt(defect, "processId");
        ValidationUtils.normalizeInt(defect, "equipmentId");
        ValidationUtils.normalizeInt(defect, "defectTypeId");
        ValidationUtils.normalizeInt(defect, "defectQty");
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
    private String getUserId(Map<String, Object> defect) {
        Object userId = defect.get("userId");
        if (userId == null) {
            return null;
        }
        return userId.toString();
    }
}
