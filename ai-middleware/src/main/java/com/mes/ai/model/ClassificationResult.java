package com.mes.ai.model;

/**
 * 장비 유형/포맷 분류 결과입니다.
 */
public class ClassificationResult {
    private String deviceTypeId;
    private double confidence;

    public String getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(String deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
