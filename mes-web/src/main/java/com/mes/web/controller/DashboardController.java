package com.mes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 목적: 대시보드 화면을 제공한다.
 * 기능: 생산 현황판 페이지로 이동한다.
 * 이유: 로그인 성공 후 기본 랜딩을 제공하기 위함이다.
 * 유지보수: 대시보드 구성 변경 시 반환 경로를 수정한다.
 */
@Controller
public class DashboardController {

    /**
     * 목적: 생산 현황판 화면을 반환한다.
     * 기능: /dashboard/production 요청을 JSP로 연결한다.
     * 이유: 라우트 기준을 문서와 일치시키기 위함이다.
     * 유지보수: 화면 경로 변경 시 반환 값만 조정한다.
     */
    @GetMapping("/dashboard/production")
    public String productionDashboard() {
        return "dashboard/production";
    }
}
