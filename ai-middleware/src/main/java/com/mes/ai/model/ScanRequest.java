package com.mes.ai.model;

/**
 * 보안 스캔 요청 정보를 담는 모델입니다.
 * 목적: 스캔에 필요한 최소 입력 정보를 한 곳에 모읍니다.
 * 기능: 원본 식별자와 페이로드 정보를 함께 전달합니다.
 * 이유: 스캔 엔진 연동 시 일관된 입력 형식을 제공하기 위함입니다.
 */
public class ScanRequest {
    private Long rawId;
    private String payloadPath;
    private String payloadBase64;
    private String payloadHash;
    private String contentType;

    /**
     * 목적: 원본 데이터 식별자를 조회합니다.
     * 이유: 스캔 결과를 원본 데이터와 연결하기 위함입니다.
     */
    public Long getRawId() {
        return rawId;
    }

    /**
     * 목적: 원본 데이터 식별자를 설정합니다.
     * 이유: 스캔 기록과 원본 데이터를 연계하기 위함입니다.
     */
    public void setRawId(Long rawId) {
        this.rawId = rawId;
    }

    /**
     * 목적: 페이로드 파일 경로를 조회합니다.
     * 이유: 파일 기반 스캔 엔진과 연동할 수 있습니다.
     */
    public String getPayloadPath() {
        return payloadPath;
    }

    /**
     * 목적: 페이로드 파일 경로를 설정합니다.
     * 이유: 파일 기반 스캔을 수행하기 위함입니다.
     */
    public void setPayloadPath(String payloadPath) {
        this.payloadPath = payloadPath;
    }

    /**
     * 목적: base64 페이로드를 조회합니다.
     * 이유: 파일 없이 메모리 스캔을 수행할 때 사용합니다.
     */
    public String getPayloadBase64() {
        return payloadBase64;
    }

    /**
     * 목적: base64 페이로드를 설정합니다.
     * 이유: 수신 원문을 손상 없이 전달하기 위함입니다.
     */
    public void setPayloadBase64(String payloadBase64) {
        this.payloadBase64 = payloadBase64;
    }

    /**
     * 목적: 페이로드 해시를 조회합니다.
     * 이유: 중복/무결성 확인에 활용합니다.
     */
    public String getPayloadHash() {
        return payloadHash;
    }

    /**
     * 목적: 페이로드 해시를 설정합니다.
     * 이유: 스캔 로그와 원문 무결성을 연계하기 위함입니다.
     */
    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    /**
     * 목적: 콘텐츠 타입을 조회합니다.
     * 이유: 스캔 엔진 선택 또는 스캔 정책에 활용합니다.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 목적: 콘텐츠 타입을 설정합니다.
     * 이유: 포맷별 정책을 적용하기 위함입니다.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
