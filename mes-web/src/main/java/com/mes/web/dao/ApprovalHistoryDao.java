package com.mes.web.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mes.web.dao.mapper.ApprovalHistoryMapper;

/**
 * 목적: 승인 이력 데이터 접근을 담당한다.
 * 기능: 승인 이력 저장/조회 기능을 제공한다.
 * 이유: 승인 처리 기록을 일관되게 관리하기 위함이다.
 * 유지보수: 스키마 변경 시 매퍼와 함께 수정한다.
 */
@Repository
public class ApprovalHistoryDao {

    private final ApprovalHistoryMapper approvalHistoryMapper;

    /**
     * 목적: 매퍼를 주입받는다.
     * 기능: DAO 내부에서 매퍼를 사용할 수 있게 한다.
     * 이유: 데이터 접근을 인터페이스로 분리하기 위함이다.
     * 유지보수: 매퍼 교체 시 주입만 변경한다.
     */
    @Autowired
    public ApprovalHistoryDao(ApprovalHistoryMapper approvalHistoryMapper) {
        this.approvalHistoryMapper = approvalHistoryMapper;
    }

    /**
     * 목적: 승인 이력을 저장한다.
     * 기능: 승인/반려 기록을 DB에 저장한다.
     * 이유: 이력 조회 화면에 사용하기 위함이다.
     * 유지보수: 컬럼 추가 시 파라미터를 보완한다.
     */
    public void insertHistory(String userId, String action, String reasonCode, String reasonTextEnc, String actorUserId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("action", action);
        params.put("reasonCode", reasonCode);
        params.put("reasonTextEnc", reasonTextEnc);
        params.put("actorUserId", actorUserId);
        approvalHistoryMapper.insertHistory(params);
    }

    /**
     * 목적: 승인 이력 목록을 조회한다.
     * 기능: 검색 조건/페이징을 적용해 목록을 반환한다.
     * 이유: 관리자 이력 조회에 사용하기 위함이다.
     * 유지보수: 필터 추가 시 파라미터를 확장한다.
     */
    public List<Map<String, Object>> findHistory(Map<String, Object> params) {
        return approvalHistoryMapper.findHistory(params);
    }

    /**
     * 목적: 승인 이력 개수를 조회한다.
     * 기능: 검색 조건에 해당하는 총 건수를 반환한다.
     * 이유: 페이징 계산에 사용하기 위함이다.
     * 유지보수: 조건 변경 시 SQL을 수정한다.
     */
    public int countHistory(Map<String, Object> params) {
        return approvalHistoryMapper.countHistory(params);
    }
}
