package com.mes.ai.pipeline.impl;

import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.MessageType;
import com.mes.ai.model.RawEnvelope;
import com.mes.ai.pipeline.Normalizer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mes.ai.util.Base64Utils;
import com.mes.ai.util.JacksonUtils;
import com.mes.ai.util.PayloadNormalizationUtils;

import java.util.Collections;
import java.util.Map;

/**
 * JSON 포맷을 기준으로 정규화합니다.
 * 목적: 다양한 입력 중 JSON을 표준 Map 형태로 변환합니다.
 * 기능: Base64 원본을 복원하고 JSON 파싱을 수행합니다.
 * 이유: 후속 단계에서 공통 키/값 구조로 처리하기 위함입니다.
 * 유지보수: JSON 파서 설정 변경 시 이 클래스에서 조정합니다.
 */
public class JsonNormalizer implements Normalizer {
    private static final ObjectMapper OBJECT_MAPPER = JacksonUtils.getObjectMapper();

    /**
     * 목적: JSON payload를 표준 Map 구조로 변환합니다.
     * 기능: Base64 복원 후 JSON 파싱과 messageType 추출을 수행합니다.
     * 이유: 분류/검증 단계가 동일한 구조를 요구하기 때문입니다.
     * 유지보수: 파싱 실패 처리 정책 변경 시 이 메서드를 수정합니다.
     */
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
            // 표준 키 별칭을 적용해 검증 단계의 실패를 줄입니다.
            Map<String, Object> normalized = PayloadNormalizationUtils.applyStandardAliases(parsed);
            candidate.setNormalizedPayload(normalized);
            // messageType은 표준 메시지 분류에 필요하므로 여기서 추출합니다.
            candidate.setMessageType(extractMessageType(normalized));
        } catch (Exception ex) {
            // 파싱 실패 시 검증 단계에서 실패 처리하도록 빈 데이터로 반환합니다.
            candidate.setNormalizedPayload(Collections.emptyMap());
        }
        return candidate;
    }

    /**
     * 메시지 유형을 다양한 키 후보에서 찾아 MessageType으로 변환합니다.
     * 목적: 입력 포맷의 키 차이를 흡수하여 분류 일관성을 높입니다.
     * 기능: 후보 키를 순차 조회해 MessageType으로 변환합니다.
     * 이유: 다양한 제조장비가 서로 다른 키 이름을 사용하기 때문입니다.
     * 유지보수: 새 키가 추가되면 여기에서 별칭을 늘립니다.
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
