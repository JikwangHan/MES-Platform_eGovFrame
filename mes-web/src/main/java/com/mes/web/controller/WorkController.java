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

import com.mes.web.service.WorkService;

/**
 * 목적: 작업 관련 화면을 제공한다.
 * 기능: 작업현황/작업관리/작업지시 화면을 반환한다.
 * 이유: 작업 흐름 화면을 기준 문서와 일치시키기 위함이다.
 * 유지보수: 공정 분배 기능 확장 시 모델 값을 추가한다.
 */
@Controller
public class WorkController {

    private final WorkService workService;

    /**
     * 목적: 작업 서비스를 주입받는다.
     * 기능: 작업 조회/등록/수정/삭제를 호출할 수 있게 한다.
     * 이유: 화면과 서비스 로직을 연결하기 위함이다.
     * 유지보수: 서비스 교체 시 주입만 변경한다.
     */
    @Autowired
    public WorkController(WorkService workService) {
        this.workService = workService;
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
        int count = workService.createWorkOrder(work);
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
        int count = workService.updateWorkOrder(work);
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
        int count = workService.updateWorkStatus(workNo, status);
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
        int count = workService.deleteWorkOrder(workNo);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", count > 0 ? "success" : "fail");
        result.put("affectedRows", count);
        return result;
    }
}
