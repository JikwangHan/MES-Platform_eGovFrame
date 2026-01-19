package com.mes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 목적: 기준정보(마스터) 화면을 제공한다.
 * 기능: 품목/품목유형/공정 화면을 반환한다.
 * 이유: 마스터 데이터 관리 화면을 제공하기 위함이다.
 * 유지보수: 탭 구조 변경 시 JSP 구성을 수정한다.
 */
@Controller
public class MasterController {

    /**
     * 목적: 품목내역 화면을 반환한다.
     * 기능: /master/items 요청을 JSP로 연결한다.
     * 이유: 품목 리스트 관리 화면을 제공하기 위함이다.
     * 유지보수: BOM 연계 기능 추가 시 컨트롤러를 확장한다.
     */
    @GetMapping("/master/items")
    public String items() {
        return "master/items";
    }

    /**
     * 목적: 품목유형 화면을 반환한다.
     * 기능: /master/item-types 요청을 JSP로 연결한다.
     * 이유: 품목 유형 트리 화면을 제공하기 위함이다.
     * 유지보수: 트리 구조 변경 시 UI를 수정한다.
     */
    @GetMapping("/master/item-types")
    public String itemTypes() {
        return "master/item_types";
    }

    /**
     * 목적: 작업공정 화면을 반환한다.
     * 기능: /master/processes 요청을 JSP로 연결한다.
     * 이유: 공정 트리 관리 화면을 제공하기 위함이다.
     * 유지보수: 공정 상세 정보 추가 시 UI를 보완한다.
     */
    @GetMapping("/master/processes")
    public String processes() {
        return "master/processes";
    }
}
