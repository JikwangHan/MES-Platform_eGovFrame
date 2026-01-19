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

import com.mes.web.service.EquipmentService;

/**
 * 목적: 설비 관련 화면을 제공한다.
 * 기능: 설비현황/모니터링/설비등록 화면을 반환한다.
 * 이유: 설비 관리 흐름을 제공하기 위함이다.
 * 유지보수: 제어 기능 추가 시 서비스 로직을 확장한다.
 */
@Controller
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * 목적: 설비 서비스를 주입받는다.
     * 기능: 설비 조회/등록/수정/삭제를 호출할 수 있게 한다.
     * 이유: 화면과 서비스 로직을 연결하기 위함이다.
     * 유지보수: 서비스 교체 시 주입만 변경한다.
     */
    @Autowired
    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
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
        int count = equipmentService.createEquipment(equipment);
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
        int count = equipmentService.updateEquipment(equipment);
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
        int count = equipmentService.deleteEquipment(equipmentCode);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }
}
