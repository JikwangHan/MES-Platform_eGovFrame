package com.mes.ai.model;

/**
 * 스캔 전용 서비스로 전달할 인바운드 객체입니다.
 * 목적: 프로토콜과 무관하게 수신 결과물을 하나의 구조로 표준화합니다.
 * 기능: 식별자, 콘텐츠 타입, 크기, 원본 위치/해시 정보를 제공합니다.
 * 이유: 스캔 서비스가 데이터 위치와 무결성 정보를 기준으로 처리하도록 하기 위함입니다.
 */
public class InboundObject {
    /** 인바운드 객체의 고유 식별자입니다. */
    private String id;
    /** 수신 출처(장비/게이트웨이) 식별자입니다. */
    private String sourceId;
    /** 콘텐츠 타입(MIME)입니다. */
    private String contentType;
    /** 원본 크기(바이트)입니다. */
    private long sizeBytes;
    /** 원본 위치(파일 경로 또는 메모리 핸들)입니다. */
    private String payloadRef;
    /** 원본 본문(Base64 인코딩)입니다. */
    private String payloadBase64;
    /** 원본 해시 값(무결성 확인용)입니다. */
    private String payloadHash;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getPayloadRef() {
        return payloadRef;
    }

    public void setPayloadRef(String payloadRef) {
        this.payloadRef = payloadRef;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    public String getPayloadBase64() {
        return payloadBase64;
    }

    public void setPayloadBase64(String payloadBase64) {
        this.payloadBase64 = payloadBase64;
    }
}
