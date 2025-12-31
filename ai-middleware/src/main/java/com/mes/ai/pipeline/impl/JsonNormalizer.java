package com.mes.ai.pipeline.impl;

import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.MessageType;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.pipeline.Normalizer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mes.ai.util.Base64Utils;
import com.mes.ai.util.JacksonUtils;

import java.util.Collections;
import java.util.Map;

/**
 * JSON 포맷을 기준으로 정규화합니다.
 * 목적: 다양한 입력 중 JSON을 표준 Map 형태로 변환합니다.
 * 기능: Base64 원본을 복원하고 JSON 파싱을 수행합니다.
 * 이유: 후속 단계에서 공통 키/값 구조로 처리하기 위함입니다.
 */
public class JsonNormalizer implements Normalizer {
    private static final ObjectMapper OBJECT_MAPPER = JacksonUtils.getObjectMapper();

    @Override
    public EnvelopeCandidate normalize(RawEnvelope rawEnvelope) {
        // 후보 객체는 항상 생성해 반환하여 다음 단계와의 계약을 지킵니다.
        EnvelopeCandidate candidate = new EnvelopeCandidate();
        candidate.setRawEnvelope(rawEnvelope);

        if (rawEnvelope == null || rawEnvelope.getPayloadBase64() == null) {
            // 원본이 없으면 빈 payload로 넘겨 검증 단계에서 실패 처리합니다.
            candidate.setNormalizedPayload(Collections.emptyMap());
            return candidate;
        }

        // Base64로 보관된 원본을 복원합니다.
        String payload = Base64Utils.decodeToString(rawEnvelope.getPayloadBase64());
        try {
            // Jackson을 사용해 JSON을 안전하게 파싱합니다.
            Map<String, Object> parsed = OBJECT_MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {});
            candidate.setNormalizedPayload(parsed);
            // messageType은 표준 메시지 분류에 필요하므로 여기서 추출합니다.
            candidate.setMessageType(extractMessageType(parsed));
        } catch (Exception ex) {
            // 파싱 실패 시 검증 단계에서 실패 처리하도록 빈 데이터로 반환합니다.
            candidate.setNormalizedPayload(Collections.emptyMap());
        }
        return candidate;
    }

    /**
     * 메시지 유형을 다양한 키 후보에서 찾아 MessageType으로 변환합니다.
     * 목적: 입력 포맷의 키 차이를 흡수하여 분류 일관성을 높입니다.
     */
    private MessageType extractMessageType(Map<String, Object> payload) {
        // 우선 순위에 따라 대표 키를 순차적으로 확인합니다.
        Object value = payload.get("messageType");
        if (value == null) {
            value = payload.get("message_type");
        }
        if (value == null) {
            value = payload.get("type");
        }
        if (value == null) {
            return null;
        }
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            return null;
        }
        try {
            // 대소문자 차이를 흡수하기 위해 대문자로 변환합니다.
            return MessageType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            // 알 수 없는 값은 null 처리해 검증 단계에서 거절되도록 합니다.
            return null;
        }
    }
}
