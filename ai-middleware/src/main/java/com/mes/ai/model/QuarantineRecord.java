package com.mes.ai.model;

/**
 * 격리 데이터를 저장하기 위한 간단한 모델입니다.
 * 목적: 실패 데이터를 재처리/분석할 수 있도록 정보를 묶어 보관합니다.
 * 기능: 원본, 실패 사유, 격리 시각을 함께 담습니다.
 * 이유: 실패 원인을 파악하고 품질 개선에 활용하기 위함입니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class QuarantineRecord {
    private RawEnvelope rawEnvelope;
    private String reason;
    private String quarantinedAt;
    private String scanStatus;
    private String scanEngine;
    private String scanSignature;
    private Long scanDurationMs;
    private String scanError;

    /**
     * 목적: 격리된 원본 데이터를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 재처리 시 원본이 필요합니다.
     */
    public RawEnvelope getRawEnvelope() {
        return rawEnvelope;
    }

    /**
     * 목적: 격리된 원본 데이터를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 실패 원본과 사유를 함께 보관합니다.
     */
    public void setRawEnvelope(RawEnvelope rawEnvelope) {
        this.rawEnvelope = rawEnvelope;
    }

    /**
     * 목적: 실패 사유를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 원인 분석과 재처리 판단에 필요합니다.
     */
    public String getReason() {
        return reason;
    }

    /**
     * 목적: 실패 사유를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 격리 기록에 원인을 명확히 남깁니다.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 목적: 격리 시각을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 격리 시점 기준으로 조회/정렬에 사용됩니다.
     */
    public String getQuarantinedAt() {
        return quarantinedAt;
    }

    /**
     * 목적: 격리 시각을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 기록의 시간 기준을 통일하기 위함입니다.
     */
    public void setQuarantinedAt(String quarantinedAt) {
        this.quarantinedAt = quarantinedAt;
    }

    /**
     * 목적: 보안 스캔 상태를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 격리 사유와 함께 보안 상태를 추적하기 위함입니다.
     */
    public String getScanStatus() {
        return scanStatus;
    }

    /**
     * 목적: 보안 스캔 상태를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 격리 기록에 스캔 결과를 남기기 위함입니다.
     */
    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    /**
     * 목적: 보안 스캔 엔진명을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 엔진별 결과를 추적하기 위함입니다.
     */
    public String getScanEngine() {
        return scanEngine;
    }

    /**
     * 목적: 보안 스캔 엔진명을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 감사 로그에 엔진 정보를 남기기 위함입니다.
     */
    public void setScanEngine(String scanEngine) {
        this.scanEngine = scanEngine;
    }

    /**
     * 목적: 보안 스캔 시그니처를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 감염 근거를 확인하기 위함입니다.
     */
    public String getScanSignature() {
        return scanSignature;
    }

    /**
     * 목적: 보안 스캔 시그니처를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 격리 기록에 근거를 남기기 위함입니다.
     */
    public void setScanSignature(String scanSignature) {
        this.scanSignature = scanSignature;
    }

    /**
     * 목적: 스캔 소요 시간을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 성능 분석과 타임아웃 판단에 필요합니다.
     */
    public Long getScanDurationMs() {
        return scanDurationMs;
    }

    /**
     * 목적: 스캔 소요 시간을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 운영 지표를 기록하기 위함입니다.
     */
    public void setScanDurationMs(Long scanDurationMs) {
        this.scanDurationMs = scanDurationMs;
    }

    /**
     * 목적: 스캔 오류 메시지를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 실패 원인을 빠르게 확인하기 위함입니다.
     */
    public String getScanError() {
        return scanError;
    }

    /**
     * 목적: 스캔 오류 메시지를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 격리 기록에 장애 정보를 남기기 위함입니다.
     */
    public void setScanError(String scanError) {
        this.scanError = scanError;
    }
}
