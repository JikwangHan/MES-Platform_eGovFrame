package com.mes.ai.model;

/**
 * 보안 스캔 결과를 담는 모델입니다.
 * 목적: 스캔 결과를 표준 형식으로 저장/전달합니다.
 * 기능: 상태, 시그니처, 엔진, 시간 정보를 보관합니다.
 * 이유: 운영 감사와 후속 분기 처리를 정확히 하기 위함입니다.
 */
public class ScanResult {
    private ScanStatus status;
    private String signature;
    private String scannedAt;
    private String engine;
    private Long durationMs;
    private String error;

    /**
     * 목적: 스캔 상태를 조회합니다.
     * 이유: 파이프라인 분기 기준이 됩니다.
     */
    public ScanStatus getStatus() {
        return status;
    }

    /**
     * 목적: 스캔 상태를 설정합니다.
     * 이유: 결과 분기와 기록을 위해 필요합니다.
     */
    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    /**
     * 목적: 탐지된 시그니처를 조회합니다.
     * 이유: 악성코드 식별 근거를 남기기 위함입니다.
     */
    public String getSignature() {
        return signature;
    }

    /**
     * 목적: 탐지된 시그니처를 설정합니다.
     * 이유: 감사 로그에 명확한 근거를 남기기 위함입니다.
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * 목적: 스캔 수행 시각을 조회합니다.
     * 이유: 지연/지속시간 분석에 활용합니다.
     */
    public String getScannedAt() {
        return scannedAt;
    }

    /**
     * 목적: 스캔 수행 시각을 설정합니다.
     * 이유: 감사 로그의 시간 축을 맞추기 위함입니다.
     */
    public void setScannedAt(String scannedAt) {
        this.scannedAt = scannedAt;
    }

    /**
     * 목적: 스캔 엔진명을 조회합니다.
     * 이유: 엔진별 결과 차이를 추적하기 위함입니다.
     */
    public String getEngine() {
        return engine;
    }

    /**
     * 목적: 스캔 엔진명을 설정합니다.
     * 이유: 엔진 버전과 결과를 연계하기 위함입니다.
     */
    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * 목적: 스캔 소요 시간을 조회합니다.
     * 이유: 성능 모니터링과 타임아웃 판단에 필요합니다.
     */
    public Long getDurationMs() {
        return durationMs;
    }

    /**
     * 목적: 스캔 소요 시간을 설정합니다.
     * 이유: 엔진 성능과 장애 여부를 판단하기 위함입니다.
     */
    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    /**
     * 목적: 스캔 오류 메시지를 조회합니다.
     * 이유: 오류 분석 및 재시도 판단에 사용합니다.
     */
    public String getError() {
        return error;
    }

    /**
     * 목적: 스캔 오류 메시지를 설정합니다.
     * 이유: 실패 원인을 기록해 재발을 줄이기 위함입니다.
     */
    public void setError(String error) {
        this.error = error;
    }
}
