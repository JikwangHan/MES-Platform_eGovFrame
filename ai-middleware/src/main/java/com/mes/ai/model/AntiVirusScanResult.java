package com.mes.ai.model;

/**
 * 안티바이러스 스캔 결과 모델입니다.
 * 목적: 스캔 전용 서비스의 판정 결과를 표준화합니다.
 * 기능: 판정/위협명/시그니처/지연/에러 정보를 제공합니다.
 * 이유: 운영 로그/알림/격리 분기를 일관되게 처리하기 위함입니다.
 */
public class AntiVirusScanResult {
    /** 스캔 판정 상태입니다. */
    private AntiVirusVerdict verdict;
    /** 위협명(탐지 시)입니다. */
    private String threatName;
    /** 시그니처 버전입니다. */
    private String signatureVersion;
    /** 사용한 스캔 엔진 이름입니다. */
    private String engine;
    /** 스캔 소요 시간(ms)입니다. */
    private long durationMs;
    /** 오류 메시지(실패 시)입니다. */
    private String errorMessage;

    public AntiVirusVerdict getVerdict() {
        return verdict;
    }

    public void setVerdict(AntiVirusVerdict verdict) {
        this.verdict = verdict;
    }

    public String getThreatName() {
        return threatName;
    }

    public void setThreatName(String threatName) {
        this.threatName = threatName;
    }

    public String getSignatureVersion() {
        return signatureVersion;
    }

    public void setSignatureVersion(String signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
