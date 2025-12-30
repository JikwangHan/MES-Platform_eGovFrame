package com.mes.ai.model;

/**
 * 원본 수신 메시지 봉투입니다.
 * 원본을 그대로 보관하기 위해 payload를 변경하지 않습니다.
 */
public class RawEnvelope {
    private String receivedAt;
    private String ingressType;
    private String sourceIdHash;
    private String contentType;
    private String payloadBase64;
    private String payloadHash;

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getIngressType() {
        return ingressType;
    }

    public void setIngressType(String ingressType) {
        this.ingressType = ingressType;
    }

    public String getSourceIdHash() {
        return sourceIdHash;
    }

    public void setSourceIdHash(String sourceIdHash) {
        this.sourceIdHash = sourceIdHash;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getPayloadBase64() {
        return payloadBase64;
    }

    public void setPayloadBase64(String payloadBase64) {
        this.payloadBase64 = payloadBase64;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }
}
