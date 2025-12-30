package com.mes.ai.model;

import java.util.Map;

/**
 * 정규화 단계에서 생성되는 중간 모델입니다.
 * 원본과 표준 구조 사이의 변환 결과를 담습니다.
 */
public class EnvelopeCandidate {
    private RawEnvelope rawEnvelope;
    private MessageType messageType;
    private Map<String, Object> normalizedPayload;

    public RawEnvelope getRawEnvelope() {
        return rawEnvelope;
    }

    public void setRawEnvelope(RawEnvelope rawEnvelope) {
        this.rawEnvelope = rawEnvelope;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Map<String, Object> getNormalizedPayload() {
        return normalizedPayload;
    }

    public void setNormalizedPayload(Map<String, Object> normalizedPayload) {
        this.normalizedPayload = normalizedPayload;
    }
}
