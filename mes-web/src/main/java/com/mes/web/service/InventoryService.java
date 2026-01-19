package com.mes.web.service;

import java.util.List;
import java.util.Map;

/**
 * 목적: 재고 관련 비즈니스 로직을 정의한다.
 * 기능: 재고 조회/등록/수정/삭제 계약을 제공한다.
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

    /**
     * 목적: 재고를 등록한다.
     * 기능: 재고 정보를 저장한다.
     * 이유: 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 컬럼 확정 시 파라미터를 보완한다.
     */
    int createInventoryStatus(Map<String, Object> inventory);

    /**
     * 목적: 재고를 수정한다.
     * 기능: 재고 수량과 상태를 갱신한다.
     * 이유: 입출고 흐름을 반영하기 위함이다.
     * 유지보수: 수정 가능 컬럼 변경 시 SQL을 수정한다.
     */
    int updateInventoryStatus(Map<String, Object> inventory);

    /**
     * 목적: 재고를 삭제한다.
     * 기능: 재고 ID 기준으로 삭제한다.
     * 이유: 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 수정한다.
     */
    int deleteInventoryStatus(long id);
}
