package com.mes.ai.model;

/**
 * 장비 유형/포맷 분류 결과입니다.
 * 목적: 데이터가 어떤 장비 유형인지 추정 결과를 저장합니다.
 * 기능: 후보 장비 ID와 신뢰도(confidence)를 함께 보관합니다.
 * 이유: 신뢰도 기준으로 표준 저장 여부를 결정하기 위함입니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class ClassificationResult {
    private String deviceTypeId;
    private double confidence;

    /**
     * 목적: 분류된 장비 유형 ID를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 표준 매핑 규칙을 찾는 키로 사용됩니다.
     */
    public String getDeviceTypeId() {
        return deviceTypeId;
    }

    /**
     * 목적: 분류된 장비 유형 ID를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 이후 매핑/검증에 활용됩니다.
     */
    public void setDeviceTypeId(String deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    /**
     * 목적: 분류 신뢰도를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 임계치 미만이면 격리(Quarantine) 처리합니다.
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * 목적: 분류 신뢰도를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 판단 기준을 명시적으로 기록하기 위함입니다.
     */
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
