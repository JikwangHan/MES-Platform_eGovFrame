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

    /**
     * 목적: 품질 서비스를 주입받는다.
     * 기능: 불량 조회/등록/삭제를 호출할 수 있게 한다.
     * 이유: 화면과 서비스 로직을 연결하기 위함이다.
     * 유지보수: 서비스 교체 시 주입만 변경한다.
     */
    @Autowired
    public QualityController(QualityService qualityService) {
        this.qualityService = qualityService;
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
        int count = qualityService.createDefect(defect);
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
        int count = qualityService.deleteDefect(id);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }
}
