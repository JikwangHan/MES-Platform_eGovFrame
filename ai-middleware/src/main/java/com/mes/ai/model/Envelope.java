package com.mes.ai.model;

import java.util.Map;

/**
 * 표준 메시지 봉투(Envelope) 모델입니다.
 * 목적: 장비 데이터가 어떤 유형의 메시지인지, 언제 발생했는지, 무엇을 담는지
 * 기능: protocolVersion/schemaVersion/messageType/deviceId/timestamp/payload를
 * 이유: 장비와 시스템이 달라도 동일한 메시지 규칙으로 처리할 수 있어
 * 유지보수: 확장/변경 시 이 클래스에서 정책을 조정합니다.
 */
public class Envelope {
    private Long rawId;
    private String protocolVersion;
    private String schemaVersion;
    private MessageType messageType;
    private String deviceId;
    private String timestamp;
    private Map<String, Object> payload;

    /**
     * 목적: 원본 데이터의 DB 식별자를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 표준 데이터와 원본 데이터의 연계를 유지합니다.
     */
    public Long getRawId() {
        return rawId;
    }

    /**
     * 목적: 원본 데이터의 DB 식별자를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 표준 저장 시 raw_data와의 연결을 보장합니다.
     */
    public void setRawId(Long rawId) {
        this.rawId = rawId;
    }

    /**
     * 목적: 통신 규격 버전을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 규격이 변경될 때도 호환성을 유지하기 위해 버전 정보를 분리 관리합니다.
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * 목적: 통신 규격 버전을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 수신/발신 양쪽에서 동일 버전을 기준으로 파싱/검증을 수행해야 합니다.
     */
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * 목적: 데이터 스키마 버전을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: payload 구조가 바뀌어도 버전으로 구분해 처리할 수 있게 합니다.
     */
    public String getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * 목적: 데이터 스키마 버전을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 표준 데이터 구조 변경 시 추적과 호환 처리를 위해 필요합니다.
     */
    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    /**
     * 목적: 메시지 유형(Telemetry/Event/Command/Ack)을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 처리 로직이 유형별로 다르므로 분기 기준이 됩니다.
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * 목적: 메시지 유형을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 수신/전송 시 어떤 처리 규칙을 적용할지 결정하기 위함입니다.
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * 목적: 장비 식별자를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 장비별 데이터 분리 및 추적을 위해 필수입니다.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 목적: 장비 식별자를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 메시지 단위로 장비를 명확히 구분하기 위함입니다.
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * 목적: 발생 시각을 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 시간 순서 재구성 및 지연 분석에 필요합니다.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * 목적: 발생 시각을 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 데이터 시계열 처리와 감사 로그 연계를 위해 필요합니다.
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 목적: 실제 데이터(payload)를 조회합니다.
     * 기능: 요청한 값을 반환합니다.
     * 이유: 표준 태그 기반의 값이 여기에 저장되어 이후 처리에 사용됩니다.
     */
    public Map<String, Object> getPayload() {
        return payload;
    }

    /**
     * 목적: 실제 데이터(payload)를 설정합니다.
     * 기능: 전달받은 값을 설정합니다.
     * 이유: 정규화된 표준 태그 값으로 저장·검증·표시를 수행합니다.
     */
    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
