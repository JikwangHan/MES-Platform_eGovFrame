package com.mes.ai.model;

/**
 * 스캔 전용 서비스로 전달할 인바운드 객체입니다.
 * 목적: 프로토콜과 무관하게 수신 결과물을 하나의 구조로 표준화합니다.
 * 기능: 식별자, 콘텐츠 타입, 크기, 원본 위치/해시 정보를 제공합니다.
 * 이유: 스캔 서비스가 데이터 위치와 무결성 정보를 기준으로 처리하도록 하기 위함입니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class InboundObject {
    private String id;
    private String sourceId;
    private String contentType;
    private long sizeBytes;
    private String payloadRef;
    private String payloadBase64;
    private String payloadHash;

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public String getId() {
        return id;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public String getPayloadRef() {
        return payloadRef;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setPayloadRef(String payloadRef) {
        this.payloadRef = payloadRef;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public String getPayloadHash() {
        return payloadHash;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public String getPayloadBase64() {
        return payloadBase64;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setPayloadBase64(String payloadBase64) {
        this.payloadBase64 = payloadBase64;
    }
}
