package com.mes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 목적: 작업 관련 화면을 제공한다.
 * 기능: 작업현황/작업관리/작업지시 화면을 반환한다.
 * 이유: 작업 흐름 화면을 기준 문서와 일치시키기 위함이다.
 * 유지보수: 공정 분배 기능 확장 시 모델 값을 추가한다.
 */
@Controller
public class WorkController {

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
}
