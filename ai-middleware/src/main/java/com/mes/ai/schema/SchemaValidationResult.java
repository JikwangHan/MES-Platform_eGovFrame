package com.mes.ai.schema;

/**
 * 스키마 검증 결과입니다.
 * 목적: 통과 여부와 실패 사유를 명확히 전달합니다.
 * 기능: pass/reason을 보관합니다.
 * 이유: Validator가 실패 사유를 일관되게 처리하기 위함입니다.
 */
public class SchemaValidationResult {
    private final boolean pass;
    private final String reason;

    /**
     * 목적: 검증 결과를 생성합니다.
     * 이유: 성공/실패와 사유를 함께 전달합니다.
     */
    public SchemaValidationResult(boolean pass, String reason) {
        this.pass = pass;
        this.reason = reason;
    }

    /**
     * 목적: 검증 통과 여부를 조회합니다.
     * 이유: Validator의 처리 분기 기준이 됩니다.
     */
    public boolean isPass() {
        return pass;
    }

    /**
     * 목적: 실패 사유를 조회합니다.
     * 이유: 격리 및 로그 기록에 사용됩니다.
     */
    public String getReason() {
        return reason;
    }
}
