package com.mes.web.service;

import java.util.List;
import java.util.Map;

/**
 * 목적: 작업 관련 비즈니스 로직을 정의한다.
 * 기능: 작업 목록 조회/등록/수정/삭제 계약을 제공한다.
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

    /**
     * 목적: 작업을 등록한다.
     * 기능: 작업 정보를 저장한다.
     * 이유: 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 컬럼 확정 시 파라미터를 보완한다.
     */
    int createWorkOrder(Map<String, Object> work);

    /**
     * 목적: 작업을 수정한다.
     * 기능: 작업 정보를 갱신한다.
     * 이유: 수정 기능을 제공하기 위함이다.
     * 유지보수: 수정 가능 컬럼 변경 시 SQL을 수정한다.
     */
    int updateWorkOrder(Map<String, Object> work);

    /**
     * 목적: 작업 상태를 수정한다.
     * 기능: 상태 값을 갱신한다.
     * 이유: 작업 흐름 제어를 지원하기 위함이다.
     * 유지보수: 상태 코드 체계 변경 시 SQL을 수정한다.
     */
    int updateWorkStatus(String workNo, String status);

    /**
     * 목적: 작업을 삭제한다.
     * 기능: 작업 번호 기준으로 삭제한다.
     * 이유: 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 수정한다.
     */
    int deleteWorkOrder(String workNo);
}
