package com.mes.web.service;

import java.util.List;
import java.util.Map;

/**
 * 목적: 작업 관련 비즈니스 로직을 정의한다.
 * 기능: 작업 목록 조회/작업 상태 변경 계약을 제공한다.
 * 이유: 작업 흐름 제어를 서비스 계층에 모으기 위함이다.
 * 유지보수: 작업 지시/분배 로직 확정 시 메서드를 확장한다.
 */
public interface WorkService {

    /**
     * 목적: 작업 목록을 조회한다.
     * 기능: 조건에 맞는 작업 목록을 반환한다.
     * 이유: 작업현황/관리 화면에 데이터를 제공하기 위함이다.
     * 유지보수: 조건/필터 확정 시 파라미터를 구체화한다.
     */
    List<Map<String, Object>> findWorkOrders(Map<String, Object> criteria);
}
