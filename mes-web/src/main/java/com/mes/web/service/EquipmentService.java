package com.mes.web.service;

import java.util.List;
import java.util.Map;

/**
 * 목적: 설비 관련 비즈니스 로직을 정의한다.
 * 기능: 설비 조회/등록/수정/삭제 계약을 제공한다.
 * 이유: 설비 화면 로직을 서비스 계층에 모으기 위함이다.
 * 유지보수: 설비 제어 로직 확정 시 메서드를 확장한다.
 */
public interface EquipmentService {

    /**
     * 목적: 설비 현황을 조회한다.
     * 기능: 조건에 맞는 설비 상태 목록을 반환한다.
     * 이유: 설비 현황 화면에 데이터를 제공하기 위함이다.
     * 유지보수: 상태 코드 확정 시 파라미터를 구체화한다.
     */
    List<Map<String, Object>> findEquipmentStatus(Map<String, Object> criteria);

    /**
     * 목적: 설비를 등록한다.
     * 기능: 설비 정보를 저장한다.
     * 이유: 등록 기능을 제공하기 위함이다.
     * 유지보수: 필수 컬럼 확정 시 파라미터를 보완한다.
     */
    int createEquipment(Map<String, Object> equipment);

    /**
     * 목적: 설비를 수정한다.
     * 기능: 설비 정보를 갱신한다.
     * 이유: 수정 기능을 제공하기 위함이다.
     * 유지보수: 수정 가능 컬럼 변경 시 SQL을 수정한다.
     */
    int updateEquipment(Map<String, Object> equipment);

    /**
     * 목적: 설비를 삭제한다.
     * 기능: 설비 코드 기준으로 삭제한다.
     * 이유: 삭제 기능을 제공하기 위함이다.
     * 유지보수: 삭제 정책 변경 시 SQL을 수정한다.
     */
    int deleteEquipment(String equipmentCode);
}
