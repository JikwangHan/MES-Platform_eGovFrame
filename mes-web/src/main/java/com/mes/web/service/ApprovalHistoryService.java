package com.mes.web.service;

import java.util.List;
import java.util.Map;

/**
 * 목적: 승인 이력 관리 기능을 제공한다.
 * 기능: 승인 이력 저장/조회 인터페이스를 정의한다.
 * 이유: 컨트롤러와 데이터 접근을 분리하기 위함이다.
 * 유지보수: 정책 변경 시 구현체를 수정한다.
 */
public interface ApprovalHistoryService {

    /**
     * 목적: 승인 이력을 기록한다.
     * 기능: 승인/반려 정보를 저장한다.
     * 이유: 이력 조회에 사용하기 위함이다.
     * 유지보수: 저장 필드 변경 시 시그니처를 보완한다.
     */
    void record(String userId, String action, String reasonCode, String reasonText, String actorUserId);

    /**
     * 목적: 승인 이력 목록을 조회한다.
     * 기능: 검색 조건/페이징을 적용해 목록을 반환한다.
     * 이유: 관리자 이력 조회 화면에 사용하기 위함이다.
     * 유지보수: 조건 확장 시 파라미터를 보완한다.
     */
    List<Map<String, Object>> loadHistory(Map<String, Object> filters);

    /**
     * 목적: 승인 이력 개수를 조회한다.
     * 기능: 검색 조건에 따른 총 건수를 반환한다.
     * 이유: 페이징 계산을 위해 필요하다.
     * 유지보수: 조건 확장 시 파라미터를 보완한다.
     */
    int countHistory(Map<String, Object> filters);
}
