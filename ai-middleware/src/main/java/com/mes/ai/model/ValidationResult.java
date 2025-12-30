package com.mes.ai.model;

/**
 * 검증 결과입니다.
 * 목적: 표준 저장 가능 여부를 판단하기 위한 결과를 보관합니다.
 * 기능: 통과 여부와 실패 사유를 함께 기록합니다.
 * 이유: 실패 데이터는 격리(Quarantine)되어 재처리에 활용해야 하기 때문입니다.
 */
public class ValidationResult {
    private boolean pass;
    private String reason;

    /**
     * 목적: 검증 통과 여부를 조회합니다.
     * 이유: 통과 시 표준 DB에 저장, 실패 시 격리 처리합니다.
     */
    public boolean isPass() {
        return pass;
    }

    /**
     * 목적: 검증 통과 여부를 설정합니다.
     * 이유: 후속 처리 분기 기준이 됩니다.
     */
    public void setPass(boolean pass) {
        this.pass = pass;
    }

    /**
     * 목적: 실패 사유를 조회합니다.
     * 이유: 운영자가 원인을 파악하고 재처리할 수 있도록 돕습니다.
     */
    public String getReason() {
        return reason;
    }

    /**
     * 목적: 실패 사유를 설정합니다.
     * 이유: 격리 저장 시 상세 원인을 함께 남깁니다.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
}
