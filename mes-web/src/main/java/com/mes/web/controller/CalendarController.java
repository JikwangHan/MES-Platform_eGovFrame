package com.mes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 목적: 일정 달력 화면을 제공한다.
 * 기능: /calendar 요청을 JSP로 연결한다.
 * 이유: 공통 일정 관리 화면을 제공하기 위함이다.
 * 유지보수: 화면 경로 변경 시 반환 값만 수정한다.
 */
@Controller
public class CalendarController {

    /**
     * 목적: 일정 달력 화면을 반환한다.
     * 기능: 캘린더 페이지로 이동시킨다.
     * 이유: 라우트 기준을 문서와 일치시키기 위함이다.
     * 유지보수: 모달/컨텍스트 기능 확장 시 모델 값을 추가한다.
     */
    @GetMapping("/calendar")
    public String calendar() {
        return "calendar/index";
    }
}
