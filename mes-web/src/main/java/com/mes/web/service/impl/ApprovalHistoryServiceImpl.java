package com.mes.web.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mes.web.common.crypto.CryptoException;
import com.mes.web.common.crypto.CryptoService;
import com.mes.web.dao.ApprovalHistoryDao;
import com.mes.web.service.ApprovalHistoryService;

/**
 * 목적: 승인 이력 관리 로직을 구현한다.
 * 기능: 승인 이력 저장/조회 시 암호화/복호화를 처리한다.
 * 이유: 민감 정보 보호와 이력 조회를 동시에 만족하기 위함이다.
 * 유지보수: 암호화 정책 변경 시 로직을 보완한다.
 */
@Service
public class ApprovalHistoryServiceImpl implements ApprovalHistoryService {

    private final ApprovalHistoryDao approvalHistoryDao;
    private final CryptoService cryptoService;

    /**
     * 목적: 필요한 의존성을 주입받는다.
     * 기능: DAO와 암호화 서비스를 연결한다.
     * 이유: 데이터 저장과 암호화를 분리하기 위함이다.
     * 유지보수: 구현체 변경 시 주입만 교체한다.
     */
    @Autowired
    public ApprovalHistoryServiceImpl(ApprovalHistoryDao approvalHistoryDao, CryptoService cryptoService) {
        this.approvalHistoryDao = approvalHistoryDao;
        this.cryptoService = cryptoService;
    }

    /**
     * 목적: 승인 이력을 기록한다.
     * 기능: 사유를 암호화해 DB에 저장한다.
     * 이유: 이력 조회와 민감정보 보호를 동시에 만족하기 위함이다.
     * 유지보수: 암호화 정책 변경 시 로직을 수정한다.
     */
    @Override
    public void record(String userId, String action, String reasonCode, String reasonText, String actorUserId) {
        if (!isEnabled()) {
            return;
        }
        String encrypted = null;
        if (reasonText != null && !reasonText.trim().isEmpty()) {
            encrypted = cryptoService.encrypt(reasonText, userId);
        }
        approvalHistoryDao.insertHistory(userId, action, reasonCode, encrypted, actorUserId);
    }

    /**
     * 목적: 승인 이력 목록을 조회한다.
     * 기능: 저장된 암호문을 복호화해 반환한다.
     * 이유: 관리자 화면에서 사유를 확인하기 위함이다.
     * 유지보수: 복호 정책 변경 시 로직을 수정한다.
     */
    @Override
    public List<Map<String, Object>> loadHistory(Map<String, Object> filters) {
        if (!isEnabled()) {
            return new ArrayList<Map<String, Object>>();
        }
        List<Map<String, Object>> rows = approvalHistoryDao.findHistory(filters);
        for (Map<String, Object> row : rows) {
            Object enc = row.get("reason_text_enc");
            if (enc == null) {
                row.put("reason_text", null);
                continue;
            }
            try {
                row.put("reason_text", cryptoService.decrypt(enc.toString()));
            } catch (CryptoException ex) {
                row.put("reason_text", "복호화 실패");
            }
        }
        return rows;
    }

    /**
     * 목적: 승인 이력 개수를 조회한다.
     * 기능: 저장된 이력 건수를 반환한다.
     * 이유: 페이징 계산에 사용하기 위함이다.
     * 유지보수: 조건 정책 변경 시 SQL을 수정한다.
     */
    @Override
    public int countHistory(Map<String, Object> filters) {
        if (!isEnabled()) {
            return 0;
        }
        return approvalHistoryDao.countHistory(filters);
    }

    /**
     * 목적: 승인 이력 기능 활성화 여부를 확인한다.
     * 기능: 시스템 속성/환경 변수 값을 검사한다.
     * 이유: 테이블 미구성 상태에서 오류를 방지하기 위함이다.
     * 유지보수: 설정 키 변경 시 여기를 수정한다.
     */
    private boolean isEnabled() {
        String value = System.getProperty("mes.approval.history.enabled");
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv("MES_APPROVAL_HISTORY_ENABLED");
        }
        return Boolean.parseBoolean(value);
    }
}
