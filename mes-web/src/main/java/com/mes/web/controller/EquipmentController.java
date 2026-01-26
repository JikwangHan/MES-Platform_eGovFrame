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
import com.mes.web.service.EquipmentService;

/**
 * 목적: 설비 관련 화면을 제공한다.
 * 기능: 설비현황/모니터링/설비등록 화면을 반환한다.
 * 이유: 설비 관리 흐름을 제공하기 위함이다.
 * 유지보수: 제어 기능 추가 시 서비스 로직을 확장한다.
 */
@Controller
public class EquipmentController {

    /**
     * 목적: 설비 상태 허용 값을 정의한다.
     * 기능: 조회/등록/수정 요청에서 상태값 검증에 사용한다.
     * 이유: 상태 코드 오입력을 사전에 차단하기 위함이다.
     * 유지보수: 상태 코드 확정 시 목록을 조정한다.
     */
    private static final String[] EQUIPMENT_STATUS_ALLOWED = new String[] {
        "idle", "running", "stop", "maintenance"
    };

    private final EquipmentService equipmentService;
    private final AuditLogService auditLogService;

    /**
     * 목적: 설비 서비스를 주입받는다.
     * 기능: 설비 조회/등록/수정/삭제를 호출할 수 있게 한다.
     * 이유: 화면과 서비스 로직을 연결하기 위함이다.
     * 유지보수: 서비스 교체 시 주입만 변경한다.
     */
    @Autowired
    public EquipmentController(EquipmentService equipmentService, AuditLogService auditLogService) {
        this.equipmentService = equipmentService;
        this.auditLogService = auditLogService;
    }

    /**
     * 목적: 설비현황 화면을 반환한다.
     * 기능: /equipment/status 요청을 JSP로 연결한다.
     * 이유: 설비 상태 조회 화면을 제공하기 위함이다.
     * 유지보수: 상태 갱신 주기 변경 시 UI를 수정한다.
     */
    @GetMapping("/equipment/status")
    public String equipmentStatus() {
        return "equipment/status";
    }

    /**
     * 목적: 모니터링 현황 화면을 반환한다.
     * 기능: /equipment/monitoring 요청을 JSP로 연결한다.
     * 이유: RAW 데이터 모니터링 화면을 제공하기 위함이다.
     * 유지보수: 데이터 소스 변경 시 서비스 로직을 확장한다.
     */
    @GetMapping("/equipment/monitoring")
    public String equipmentMonitoring() {
        return "equipment/monitoring";
    }

    /**
     * 목적: 설비등록 화면을 반환한다.
     * 기능: /equipment 요청을 JSP로 연결한다.
     * 이유: 설비 등록 관리 화면을 제공하기 위함이다.
     * 유지보수: Restart 제어 버튼 로직 추가 시 UI를 확장한다.
     */
    @GetMapping("/equipment")
    public String equipmentRegister() {
        return "equipment/index";
    }

    /**
     * 목적: 설비 현황을 조회한다.
     * 기능: 조건에 맞는 설비 목록을 반환한다.
     * 이유: 설비 그리드 조회 기능을 제공하기 위함이다.
     * 유지보수: 검색 조건 확정 시 파라미터를 보완한다.
     */
    @PostMapping("/api/equipment/list")
    @ResponseBody
    public Map<String, Object> list(@RequestParam Map<String, Object> criteria) {
        String validationError = validateList(criteria);
        if (validationError != null) {
            return buildFail(validationError);
        }
        CriteriaUtils.applyPaging(criteria, 50, 200);
        List<Map<String, Object>> equipments = equipmentService.findEquipmentStatus(criteria);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", "success");
        result.put("data", equipments);
        return result;
    }

    /**
     * 목적: 설비를 등록한다.
     * 기능: 설비 정보를 저장하고 결과를 반환한다.
     * 이유: 설비 CRUD 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 필드 확정 시 검증 로직을 추가한다.
     */
    @PostMapping("/api/equipment/create")
    @ResponseBody
    public Map<String, Object> create(@RequestParam Map<String, Object> equipment) {
        String validationError = validateCreate(equipment);
        if (validationError != null) {
            return buildFail(validationError);
        }
        normalizeNumbers(equipment);
        int count = equipmentService.createEquipment(equipment);
        auditLogService.logEvent("equipment_create", count > 0 ? "success" : "fail", getUserId(equipment),
                "equipmentCode=" + equipment.get("equipmentCode"));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 설비를 수정한다.
     * 기능: 설비 정보를 갱신하고 결과를 반환한다.
     * 이유: 설비 CRUD 수정 기능을 제공하기 위함이다.
     * 유지보수: 수정 가능 필드 확정 시 검증 로직을 추가한다.
     */
    @PostMapping("/api/equipment/update")
    @ResponseBody
    public Map<String, Object> update(@RequestParam Map<String, Object> equipment) {
        String validationError = validateUpdate(equipment);
        if (validationError != null) {
            return buildFail(validationError);
        }
        normalizeNumbers(equipment);
        int count = equipmentService.updateEquipment(equipment);
        auditLogService.logEvent("equipment_update", count > 0 ? "success" : "fail", getUserId(equipment),
                "equipmentCode=" + equipment.get("equipmentCode"));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 설비를 삭제한다.
     * 기능: 설비 코드 기준으로 삭제한다.
     * 이유: 설비 CRUD 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 로직을 보완한다.
     */
    @PostMapping("/api/equipment/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("equipmentCode") String equipmentCode) {
        if (ValidationUtils.isBlank(equipmentCode)) {
            return buildFail("설비 코드는 필수입니다.");
        }
        int count = equipmentService.deleteEquipment(equipmentCode);
        auditLogService.logEvent("equipment_delete", count > 0 ? "success" : "fail", null,
                "equipmentCode=" + equipmentCode);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }

    /**
     * 목적: 설비 등록 필수 값을 검증한다.
     * 기능: 필수 값 누락 시 오류 메시지를 반환한다.
     * 이유: 잘못된 입력을 사전에 차단하기 위함이다.
     * 유지보수: 필수 값 변경 시 항목을 조정한다.
     */
    private String validateCreate(Map<String, Object> equipment) {
        String message = ValidationUtils.require(equipment, "equipmentCode", "설비 코드");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(equipment, "equipmentName", "설비명");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.require(equipment, "equipmentType", "설비 유형");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateIn(equipment, "status", "상태", EQUIPMENT_STATUS_ALLOWED);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(equipment, "portNumber", "포트", 0, 65535);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(equipment, "baudRate", "통신 속도", 0, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(equipment, "pollingInterval", "폴링 주기", 0, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        return null;
    }

    /**
     * 목적: 설비 수정 필수 값을 검증한다.
     * 기능: 필수 값 누락 시 오류 메시지를 반환한다.
     * 이유: 수정 대상이 없는 상태를 방지하기 위함이다.
     * 유지보수: 필수 값 변경 시 항목을 조정한다.
     */
    private String validateUpdate(Map<String, Object> equipment) {
        String message = ValidationUtils.require(equipment, "equipmentCode", "설비 코드");
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateIn(equipment, "status", "상태", EQUIPMENT_STATUS_ALLOWED);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(equipment, "portNumber", "포트", 0, 65535);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(equipment, "baudRate", "통신 속도", 0, Integer.MAX_VALUE);
        if (message != null) {
            return message;
        }
        message = ValidationUtils.validateInt(equipment, "pollingInterval", "폴링 주기", 0, Integer.MAX_VALUE);
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
    private void normalizeNumbers(Map<String, Object> equipment) {
        ValidationUtils.normalizeInt(equipment, "portNumber");
        ValidationUtils.normalizeInt(equipment, "baudRate");
        ValidationUtils.normalizeInt(equipment, "pollingInterval");
    }

    /**
     * 목적: 설비 조회 조건을 검증한다.
     * 기능: 상태 코드가 허용 값인지 확인한다.
     * 이유: 잘못된 조회 조건으로 인한 오류를 방지하기 위함이다.
     * 유지보수: 조회 조건 확정 시 검증 항목을 추가한다.
     */
    private String validateList(Map<String, Object> criteria) {
        String message = ValidationUtils.validateIn(criteria, "status", "상태", EQUIPMENT_STATUS_ALLOWED);
        if (message != null) {
            return message;
        }
        return null;
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
    private String getUserId(Map<String, Object> equipment) {
        Object userId = equipment.get("userId");
        if (userId == null) {
            return null;
        }
        return userId.toString();
    }
}
