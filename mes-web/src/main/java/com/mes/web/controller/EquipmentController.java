package com.mes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 목적: 설비 관련 화면을 제공한다.
 * 기능: 설비현황/모니터링/설비등록 화면을 반환한다.
 * 이유: 설비 관리 흐름을 제공하기 위함이다.
 * 유지보수: 제어 기능 추가 시 서비스 로직을 확장한다.
 */
@Controller
public class EquipmentController {

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
}
