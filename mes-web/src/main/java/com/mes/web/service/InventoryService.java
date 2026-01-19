package com.mes.web.service;

import java.util.List;
import java.util.Map;

/**
 * 목적: 재고 관련 비즈니스 로직을 정의한다.
 * 기능: 재고 조회/입출고 조회 계약을 제공한다.
 * 이유: 재고 화면 로직을 서비스 계층에 모으기 위함이다.
 * 유지보수: 소요산출 로직 확정 시 메서드를 확장한다.
 */
public interface InventoryService {

    /**
     * 목적: 재고 현황을 조회한다.
     * 기능: 조건에 맞는 재고 목록을 반환한다.
     * 이유: 재고 현황 화면에 데이터를 제공하기 위함이다.
     * 유지보수: 필터 항목 확정 시 파라미터를 구체화한다.
     */
    List<Map<String, Object>> findInventoryStatus(Map<String, Object> criteria);
}
