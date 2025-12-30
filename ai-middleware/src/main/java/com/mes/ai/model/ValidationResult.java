package com.mes.ai.model;

/**
 * 검증 결과입니다.
 * 실패 사유를 함께 기록하여 격리/재처리에 활용합니다.
 */
public class ValidationResult {
    private boolean pass;
    private String reason;

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
