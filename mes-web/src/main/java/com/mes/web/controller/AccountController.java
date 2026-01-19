package com.mes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 목적: 계정 관련 화면을 제공한다.
 * 기능: 비밀번호 변경 페이지를 반환한다.
 * 이유: 계정 보안 흐름을 제공하기 위함이다.
 * 유지보수: 화면 경로 또는 정책 변경 시 수정한다.
 */
@Controller
public class AccountController {

    /**
     * 목적: 비밀번호 변경 화면을 반환한다.
     * 기능: /account/change-password 요청을 JSP로 연결한다.
     * 이유: 라우트 기준을 문서와 일치시키기 위함이다.
     * 유지보수: 검증 규칙 변경 시 서비스 로직을 확장한다.
     */
    @GetMapping("/account/change-password")
    public String changePassword() {
        return "account/change_password";
    }
}
