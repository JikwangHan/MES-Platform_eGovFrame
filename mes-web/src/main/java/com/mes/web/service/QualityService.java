package com.mes.web.service;

import java.util.List;
import java.util.Map;

/**
 * 목적: 품질 관련 비즈니스 로직을 정의한다.
 * 기능: 불량 현황/내역 조회 계약을 제공한다.
 * 이유: 품질 화면 로직을 서비스 계층에 모으기 위함이다.
 * 유지보수: 그래프/통계 로직 확정 시 메서드를 확장한다.
 */
public interface QualityService {

    /**
     * 목적: 불량 내역을 조회한다.
     * 기능: 조건에 맞는 불량 목록을 반환한다.
     * 이유: 품질 화면 그리드에 데이터를 제공하기 위함이다.
     * 유지보수: 조건/필드 확정 시 파라미터를 구체화한다.
     */
    List<Map<String, Object>> findDefects(Map<String, Object> criteria);
}
