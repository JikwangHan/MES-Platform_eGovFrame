package com.mes.ai.model;

/**
 * 격리 데이터를 저장하기 위한 간단한 모델입니다.
 * 목적: 실패 데이터를 재처리/분석할 수 있도록 정보를 묶어 보관합니다.
 * 기능: 원본, 실패 사유, 격리 시각을 함께 담습니다.
 * 이유: 실패 원인을 파악하고 품질 개선에 활용하기 위함입니다.
 */
public class QuarantineRecord {
    /** 실패한 원본 데이터입니다. */
    private RawEnvelope rawEnvelope;
    /** 실패 사유입니다. */
    private String reason;
    /** 격리 처리 시각(UTC)입니다. */
    private String quarantinedAt;

    /**
     * 목적: 격리된 원본 데이터를 조회합니다.
     * 이유: 재처리 시 원본이 필요합니다.
     */
    public RawEnvelope getRawEnvelope() {
        return rawEnvelope;
    }

    /**
     * 목적: 격리된 원본 데이터를 설정합니다.
     * 이유: 실패 원본과 사유를 함께 보관합니다.
     */
    public void setRawEnvelope(RawEnvelope rawEnvelope) {
        this.rawEnvelope = rawEnvelope;
    }

    /**
     * 목적: 실패 사유를 조회합니다.
     * 이유: 원인 분석과 재처리 판단에 필요합니다.
     */
    public String getReason() {
        return reason;
    }

    /**
     * 목적: 실패 사유를 설정합니다.
     * 이유: 격리 기록에 원인을 명확히 남깁니다.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 목적: 격리 시각을 조회합니다.
     * 이유: 격리 시점 기준으로 조회/정렬에 사용됩니다.
     */
    public String getQuarantinedAt() {
        return quarantinedAt;
    }

    /**
     * 목적: 격리 시각을 설정합니다.
     * 이유: 기록의 시간 기준을 통일하기 위함입니다.
     */
    public void setQuarantinedAt(String quarantinedAt) {
        this.quarantinedAt = quarantinedAt;
    }
}
