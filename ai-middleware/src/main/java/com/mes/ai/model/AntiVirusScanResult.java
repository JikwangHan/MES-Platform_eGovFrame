package com.mes.ai.model;

/**
 * 안티바이러스 스캔 결과 모델입니다.
 * 목적: 스캔 전용 서비스의 판정 결과를 표준화합니다.
 * 기능: 판정/위협명/시그니처/지연/에러 정보를 제공합니다.
 * 이유: 운영 로그/알림/격리 분기를 일관되게 처리하기 위함입니다.
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class AntiVirusScanResult {
    private AntiVirusVerdict verdict;
    private String threatName;
    private String signatureVersion;
    private String engine;
    private long durationMs;
    private String errorMessage;

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public AntiVirusVerdict getVerdict() {
        return verdict;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setVerdict(AntiVirusVerdict verdict) {
        this.verdict = verdict;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public String getThreatName() {
        return threatName;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setThreatName(String threatName) {
        this.threatName = threatName;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public String getSignatureVersion() {
        return signatureVersion;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setSignatureVersion(String signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public String getEngine() {
        return engine;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public long getDurationMs() {
        return durationMs;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    /**
     * 목적: 값을 조회합니다.
     * 기능: 현재 설정된 값을 반환합니다.
     * 이유: 외부에서 상태를 확인할 수 있도록 하기 위함입니다.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 목적: 값을 설정합니다.
     * 기능: 전달받은 값을 내부 필드에 저장합니다.
     * 이유: 모델 상태를 갱신하기 위함입니다.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
