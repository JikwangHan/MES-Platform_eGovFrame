package com.mes.ai.model;

/**
 * 원본 수신 메시지 봉투입니다.
 * 목적: 수신된 원본 데이터를 변경 없이 보관하기 위한 최소 단위입니다.
 * 기능: 수신 시간, 수신 경로, 원본 데이터(base64), 해시를 함께 저장합니다.
 * 이유: 재처리/감사/오류 분석 시 원본이 반드시 필요하기 때문입니다.
 */
public class RawEnvelope {
    private Long id;
    private String receivedAt;
    private String ingressType;
    private String sourceIdHash;
    private String contentType;
    private String payloadBase64;
    private String payloadHash;

    /**
     * 목적: 원본 데이터의 DB 식별자를 조회합니다.
     * 이유: 표준/격리 데이터와의 연계를 위해 필요합니다.
     */
    public Long getId() {
        return id;
    }

    /**
     * 목적: 원본 데이터의 DB 식별자를 설정합니다.
     * 이유: 저장 후 생성된 키를 파이프라인에서 재사용하기 위함입니다.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 목적: 수신 시각을 조회합니다.
     * 이유: 데이터 지연/누락 분석과 재처리에 필요합니다.
     */
    public String getReceivedAt() {
        return receivedAt;
    }

    /**
     * 목적: 수신 시각을 설정합니다.
     * 이유: 수신 순서와 시간 흐름을 추적하기 위함입니다.
     */
    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    /**
     * 목적: 수신 경로(HTTP/MQTT/OPCUA 등)를 조회합니다.
     * 이유: 통신 경로별 장애 추적과 정책 적용을 위해 필요합니다.
     */
    public String getIngressType() {
        return ingressType;
    }

    /**
     * 목적: 수신 경로를 설정합니다.
     * 이유: 처리 규칙을 경로별로 분리하기 위함입니다.
     */
    public void setIngressType(String ingressType) {
        this.ingressType = ingressType;
    }

    /**
     * 목적: 송신 원본 식별자 해시를 조회합니다.
     * 이유: 개인정보/민감정보를 직접 저장하지 않고 추적성을 확보합니다.
     */
    public String getSourceIdHash() {
        return sourceIdHash;
    }

    /**
     * 목적: 송신 원본 식별자 해시를 설정합니다.
     * 이유: 안전한 식별을 위해 원본 값을 해시로만 보관합니다.
     */
    public void setSourceIdHash(String sourceIdHash) {
        this.sourceIdHash = sourceIdHash;
    }

    /**
     * 목적: 페이로드 타입(JSON/CSV/바이너리 등)을 조회합니다.
     * 이유: 정규화 단계에서 파서 선택을 위한 단서가 됩니다.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 목적: 페이로드 타입을 설정합니다.
     * 이유: 수신 시점의 포맷 힌트를 유지하기 위함입니다.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 목적: 원본 페이로드(base64)를 조회합니다.
     * 이유: 원본을 손상 없이 보관하고 재처리하기 위함입니다.
     */
    public String getPayloadBase64() {
        return payloadBase64;
    }

    /**
     * 목적: 원본 페이로드(base64)를 설정합니다.
     * 이유: 바이너리/텍스트 모두 안전하게 저장하기 위함입니다.
     */
    public void setPayloadBase64(String payloadBase64) {
        this.payloadBase64 = payloadBase64;
    }

    /**
     * 목적: 원본 페이로드 해시를 조회합니다.
     * 이유: 중복/변조 여부 확인에 사용합니다.
     */
    public String getPayloadHash() {
        return payloadHash;
    }

    /**
     * 목적: 원본 페이로드 해시를 설정합니다.
     * 이유: 무결성 검증과 중복 제거를 위해 필요합니다.
     */
    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }
}
