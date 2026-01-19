package com.mes.web.service;

import java.util.List;
import java.util.Map;

/**
 * 목적: 수주 관련 비즈니스 로직을 정의한다.
 * 기능: 수주 목록 조회/등록/수정/삭제 계약을 제공한다.
 * 이유: 컨트롤러와 데이터 접근을 분리하기 위함이다.
 * 유지보수: 상세 요구사항 확정 시 메서드를 확장한다.
 */
public interface OrderService {

    /**
     * 목적: 수주 목록을 조회한다.
     * 기능: 검색 조건에 맞는 수주 목록을 반환한다.
     * 이유: 화면 그리드 데이터 바인딩을 위해 필요하다.
     * 유지보수: 검색 조건 확정 시 파라미터를 구체화한다.
     */
    List<Map<String, Object>> findOrders(Map<String, Object> criteria);

    /**
     * 목적: 수주를 등록한다.
     * 기능: 수주 정보를 저장한다.
     * 이유: 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 컬럼 확정 시 파라미터를 보완한다.
     */
    int createOrder(Map<String, Object> order);

    /**
     * 목적: 수주를 수정한다.
     * 기능: 수주 정보를 갱신한다.
     * 이유: 수정 기능을 제공하기 위함이다.
     * 유지보수: 수정 가능 컬럼 변경 시 SQL을 수정한다.
     */
    int updateOrder(Map<String, Object> order);

    /**
     * 목적: 수주를 삭제한다.
     * 기능: 수주 번호 기준으로 삭제한다.
     * 이유: 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 수정한다.
     */
    int deleteOrder(String orderNo);
}
