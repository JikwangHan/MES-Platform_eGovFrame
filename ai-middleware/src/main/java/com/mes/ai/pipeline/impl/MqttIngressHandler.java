package com.mes.ai.pipeline.impl;

import com.mes.ai.model.RawEnvelope;
import com.mes.ai.pipeline.IngressHandler;
import com.mes.ai.util.Base64Utils;
import com.mes.ai.util.HashUtils;
import com.mes.ai.util.TimeUtils;

/**
 * MQTT 수신 기본 구현입니다.
 * 목적: MQTT로 수신한 원본 payload를 RawEnvelope로 변환합니다.
 * 기능: 수신 시각, ingress 유형, 해시, Base64 원본을 생성합니다.
 * 이유: 원본 보존과 추적성을 확보하기 위함입니다.
 */
public class MqttIngressHandler implements IngressHandler {
    /** 장비/게이트웨이 식별자(해시 생성용)입니다. */
    private final String sourceId;
    /** 수신 payload의 MIME 타입입니다. */
    private final String contentType;

    /**
     * 기본 contentType은 JSON으로 설정합니다.
     * 목적: 가장 보편적인 포맷을 기본값으로 제공합니다.
     */
    public MqttIngressHandler(String sourceId) {
        this(sourceId, "application/json");
    }

    /**
     * 수신 소스와 contentType을 명시적으로 설정합니다.
     * 목적: 다양한 장비 포맷을 유연하게 수용합니다.
     */
    public MqttIngressHandler(String sourceId, String contentType) {
        this.sourceId = sourceId;
        this.contentType = contentType;
    }

    @Override
    public RawEnvelope receive(String payload) {
        // payload가 null이면 원본 보관을 위해 빈 문자열로 치환합니다.
        String safePayload = payload == null ? "" : payload;

        RawEnvelope rawEnvelope = new RawEnvelope();
        // 수신 시각은 UTC ISO-8601 기준으로 기록합니다.
        rawEnvelope.setReceivedAt(TimeUtils.nowIsoUtc());
        // 어떤 경로로 수신되었는지 명확히 남깁니다.
        rawEnvelope.setIngressType("mqtt");
        // 컨텐츠 타입을 기록하여 후속 파싱에 활용합니다.
        rawEnvelope.setContentType(contentType);
        // sourceId는 개인정보 보호를 위해 해시로 저장합니다.
        rawEnvelope.setSourceIdHash(HashUtils.sha256Hex(sourceId));
        // 원본 payload는 변조 방지를 위해 Base64로 보관합니다.
        rawEnvelope.setPayloadBase64(Base64Utils.encode(safePayload));
        // 원본 무결성 확인을 위해 해시를 함께 보관합니다.
        rawEnvelope.setPayloadHash(HashUtils.sha256Hex(safePayload));
        return rawEnvelope;
    }
}
