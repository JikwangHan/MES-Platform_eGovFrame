package com.mes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 목적: 품질 관련 화면을 제공한다.
 * 기능: 불량현황/불량내역/불량유형 화면을 반환한다.
 * 이유: 품질 관리 흐름을 제공하기 위함이다.
 * 유지보수: 그래프/통계 연결 시 데이터 모델을 확장한다.
 */
@Controller
public class QualityController {

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
}
