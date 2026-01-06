package com.mes.ai.pipeline.impl;

import com.mes.ai.model.ClassificationResult;
import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.ValidationResult;
import com.mes.ai.pipeline.Validator;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * 필수 필드와 기본 범위를 검증합니다.
 * 목적: 최소 품질 기준을 통과한 데이터만 저장되도록 합니다.
 * 기능: 필수 필드 존재 여부와 분류 신뢰도를 확인합니다.
 * 이유: 오염된 데이터가 표준 저장소에 들어가는 것을 막기 위함입니다.
 */
public class BasicValidator implements Validator {
    /** 분류 신뢰도 최소 기준값입니다. */
    private static final double MIN_CONFIDENCE = 0.5;
    /** eventId 최소 길이 기준입니다. */
    private static final int MIN_EVENT_ID_LENGTH = 3;

    @Override
    public ValidationResult validate(EnvelopeCandidate candidate, ClassificationResult classificationResult) {
        // 검증 결과는 항상 반환해야 하므로 먼저 생성합니다.
        ValidationResult result = new ValidationResult();

        // 원본 존재 여부부터 확인하여 기본 입력 계약을 지킵니다.
        if (candidate == null || candidate.getRawEnvelope() == null) {
            return fail(result, "INGRESS_PAYLOAD_EMPTY:원본 데이터가 없습니다.");
        }
        // 메시지 유형이 없으면 데이터 해석이 불가능합니다.
        if (candidate.getMessageType() == null) {
            return fail(result, "VALIDATION_MISSING_FIELD:messageType");
        }
        Map<String, Object> payload = candidate.getNormalizedPayload();
        if (payload == null || payload.isEmpty()) {
            return fail(result, "NORMALIZE_PARSE_ERROR:정규화된 payload가 비어 있습니다.");
        }
        // 장비 식별자는 추적과 분석에 필수입니다.
        if (!hasValue(payload, "deviceId", "device_id")) {
            return fail(result, "VALIDATION_MISSING_FIELD:deviceId");
        }
        // 시간 정보는 순서 판단과 이력 관리에 필수입니다.
        if (!hasValue(payload, "timestamp")) {
            return fail(result, "VALIDATION_MISSING_FIELD:timestamp");
        }
        // 이벤트 ID는 동일 시각 중복을 구분하기 위한 기준입니다.
        if (!hasValue(payload, "eventId", "event_id")) {
            return fail(result, "VALIDATION_MISSING_FIELD:eventId");
        }
        // eventId 길이가 너무 짧으면 추적성이 떨어집니다.
        if (!hasMinLength(payload.get("eventId"), MIN_EVENT_ID_LENGTH)) {
            return fail(result, "VALIDATION_INVALID_TYPE:eventId 길이 부족");
        }
        // 프로토콜/스키마 버전은 해석 규칙을 고정하기 위한 필수 값입니다.
        if (!hasValue(payload, "protocolVersion")) {
            return fail(result, "VALIDATION_MISSING_FIELD:protocolVersion");
        }
        if (!hasValue(payload, "schemaVersion")) {
            return fail(result, "VALIDATION_MISSING_FIELD:schemaVersion");
        }
        // 버전 문자열은 숫자.숫자(또는 숫자.숫자.숫자) 형태를 기본으로 봅니다.
        if (!isVersionLike(payload.get("protocolVersion"))) {
            return fail(result, "VALIDATION_INVALID_TYPE:protocolVersion 형식 오류");
        }
        if (!isVersionLike(payload.get("schemaVersion"))) {
            return fail(result, "VALIDATION_INVALID_TYPE:schemaVersion 형식 오류");
        }
        // timestamp는 UTC ISO-8601 형식으로 처리 가능해야 합니다.
        if (!isIso8601(payload.get("timestamp"))) {
            return fail(result, "VALIDATION_INVALID_TYPE:timestamp 형식 오류");
        }
        // 분류 결과가 없으면 장비별 규칙 적용이 불가능합니다.
        if (classificationResult == null) {
            return fail(result, "CLASSIFICATION_MISSING_DEVICE_TYPE:분류 결과 없음");
        }
        if (isBlank(classificationResult.getDeviceTypeId())) {
            return fail(result, "CLASSIFICATION_MISSING_DEVICE_TYPE:deviceTypeId 없음");
        }
        // 신뢰도가 낮으면 잘못된 장비 분류일 가능성이 높습니다.
        if (classificationResult.getConfidence() < MIN_CONFIDENCE) {
            return fail(result, "CLASSIFICATION_LOW_CONFIDENCE:기준값 미만");
        }

        // 모든 조건을 통과하면 정상 처리로 표시합니다.
        result.setPass(true);
        result.setReason(null);
        return result;
    }

    /**
     * 여러 키 후보에서 값 존재 여부를 확인합니다.
     * 목적: 키 이름 차이를 흡수하여 호환성을 높입니다.
     */
    private boolean hasValue(Map<String, Object> payload, String key, String... aliases) {
        if (hasValue(payload.get(key))) {
            return true;
        }
        if (aliases == null) {
            return false;
        }
        for (String alias : aliases) {
            if (hasValue(payload.get(alias))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 값이 비어 있지 않은지 확인합니다.
     * 목적: null/공백 문자열을 구분합니다.
     */
    private boolean hasValue(Object value) {
        if (value == null) {
            return false;
        }
        return !String.valueOf(value).trim().isEmpty();
    }

    /**
     * 문자열이 비어 있는지 확인합니다.
     * 목적: deviceTypeId 등 필수 문자열 검증에 사용합니다.
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 문자열 길이가 최소 기준을 충족하는지 확인합니다.
     * 목적: 추적 키(eventId) 품질을 보장합니다.
     */
    private boolean hasMinLength(Object value, int minLength) {
        if (value == null) {
            return false;
        }
        String text = String.valueOf(value).trim();
        return text.length() >= minLength;
    }

    /**
     * 버전 문자열이 기본 패턴인지 확인합니다.
     * 목적: 스키마/프로토콜 버전 규칙을 최소 수준으로 강제합니다.
     */
    private boolean isVersionLike(Object value) {
        if (value == null) {
            return false;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return false;
        }
        return text.matches("\\d+\\.\\d+(\\.\\d+)?");
    }

    /**
     * ISO-8601 시간 형식인지 확인합니다.
     * 목적: 시간 필드의 표준화를 보장합니다.
     */
    private boolean isIso8601(Object value) {
        if (value == null) {
            return false;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return false;
        }
        try {
            Instant.parse(text);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    /**
     * 실패 결과를 일관된 방식으로 생성합니다.
     * 목적: 실패 사유를 반드시 기록하도록 강제합니다.
     */
    private ValidationResult fail(ValidationResult result, String reason) {
        result.setPass(false);
        result.setReason(reason);
        return result;
    }
}
