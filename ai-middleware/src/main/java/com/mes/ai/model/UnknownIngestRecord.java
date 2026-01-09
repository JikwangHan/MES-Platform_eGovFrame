package com.mes.ai.model;

/**
 * 미정의 통신/비정형 데이터 수신 기록입니다.
 * 목적: 정의되지 않은 입력을 안전하게 격리하고 재처리 근거를 확보합니다.
 * 기능: 원문과 보안 스캔 결과를 함께 보관합니다.
 * 이유: 보안 사고 예방과 신규 프로토콜 온보딩에 활용됩니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class UnknownIngestRecord {
    private Long id;
    private String receivedAt;
    private String ingressType;
    private String payloadBase64;
    private String payloadHash;
    private String sourceIdHash;
    private String contentType;
    private String scanStatus;
    private String scanEngine;
    private String scanSignature;
    private Long scanDurationMs;
    private String quarantineReason;
    private String createdAt;

    /**
     * 목적: 레코드 식별자를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 후속 처리 및 감사 추적에 필요합니다.
     */
    public Long getId() {
        return id;
    }

    /**
     * 목적: 레코드 식별자를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 저장 후 생성된 키를 전달하기 위함입니다.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 목적: 수신 시각을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 수집 지연 분석과 재처리 순서에 필요합니다.
     */
    public String getReceivedAt() {
        return receivedAt;
    }

    /**
     * 목적: 수신 시각을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 기록의 시간 기준을 유지하기 위함입니다.
     */
    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    /**
     * 목적: 수신 경로를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 경로별 대응 정책을 분리하기 위함입니다.
     */
    public String getIngressType() {
        return ingressType;
    }

    /**
     * 목적: 수신 경로를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 통신별 재처리 정책을 적용하기 위함입니다.
     */
    public void setIngressType(String ingressType) {
        this.ingressType = ingressType;
    }

    /**
     * 목적: 원문 페이로드(base64)를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 원문 보존 원칙을 지키기 위함입니다.
     */
    public String getPayloadBase64() {
        return payloadBase64;
    }

    /**
     * 목적: 원문 페이로드(base64)를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 재처리 시 원문 재사용을 보장하기 위함입니다.
     */
    public void setPayloadBase64(String payloadBase64) {
        this.payloadBase64 = payloadBase64;
    }

    /**
     * 목적: 페이로드 해시를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 중복/변조 여부를 확인하기 위함입니다.
     */
    public String getPayloadHash() {
        return payloadHash;
    }

    /**
     * 목적: 페이로드 해시를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 무결성 검증과 재처리 식별에 사용합니다.
     */
    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    /**
     * 목적: 송신 원본 식별자 해시를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 민감정보를 직접 보관하지 않고 추적합니다.
     */
    public String getSourceIdHash() {
        return sourceIdHash;
    }

    /**
     * 목적: 송신 원본 식별자 해시를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 안전한 추적성을 확보하기 위함입니다.
     */
    public void setSourceIdHash(String sourceIdHash) {
        this.sourceIdHash = sourceIdHash;
    }

    /**
     * 목적: 콘텐츠 타입을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 포맷 식별과 온보딩 분석에 활용합니다.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 목적: 콘텐츠 타입을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 분류 기준을 유지하기 위함입니다.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 목적: 스캔 상태를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 파이프라인 차단/승인 판단에 필요합니다.
     */
    public String getScanStatus() {
        return scanStatus;
    }

    /**
     * 목적: 스캔 상태를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 감사 로그와 운영 정책에 반영하기 위함입니다.
     */
    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    /**
     * 목적: 스캔 엔진명을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 엔진별 이슈 추적을 위해 필요합니다.
     */
    public String getScanEngine() {
        return scanEngine;
    }

    /**
     * 목적: 스캔 엔진명을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 스캔 결과의 근거를 기록하기 위함입니다.
     */
    public void setScanEngine(String scanEngine) {
        this.scanEngine = scanEngine;
    }

    /**
     * 목적: 탐지 시그니처를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 악성코드 판별 근거를 남기기 위함입니다.
     */
    public String getScanSignature() {
        return scanSignature;
    }

    /**
     * 목적: 탐지 시그니처를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 감사 로그에 세부 근거를 기록하기 위함입니다.
     */
    public void setScanSignature(String scanSignature) {
        this.scanSignature = scanSignature;
    }

    /**
     * 목적: 스캔 소요 시간을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 성능 모니터링 지표로 활용합니다.
     */
    public Long getScanDurationMs() {
        return scanDurationMs;
    }

    /**
     * 목적: 스캔 소요 시간을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 타임아웃 정책 분석에 활용합니다.
     */
    public void setScanDurationMs(Long scanDurationMs) {
        this.scanDurationMs = scanDurationMs;
    }

    /**
     * 목적: 격리 사유를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 운영자가 정확한 사유를 확인할 수 있게 합니다.
     */
    public String getQuarantineReason() {
        return quarantineReason;
    }

    /**
     * 목적: 격리 사유를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 재처리 판단에 필요한 근거를 남깁니다.
     */
    public void setQuarantineReason(String quarantineReason) {
        this.quarantineReason = quarantineReason;
    }

    /**
     * 목적: 생성 시각을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 보관 기간 정책과 감사 추적에 활용합니다.
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * 목적: 생성 시각을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: DB 저장 후 생성 시각을 기록하기 위함입니다.
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
