package com.mes.ai.pipeline.impl;

import com.mes.ai.model.ClassificationResult;
import com.mes.ai.model.EnvelopeCandidate;
import com.mes.ai.model.MessageType;
import com.mes.ai.model.ValidationResult;
import com.mes.ai.pipeline.Validator;
import com.mes.ai.schema.SchemaKey;
import com.mes.ai.schema.SchemaRegistry;
import com.mes.ai.schema.SchemaValidationResult;
import com.mes.ai.schema.SchemaValidator;

import java.util.Map;

/**
 * JSON Schema 기반 검증 구현입니다.
 * 목적: 스키마 버전 기준으로 데이터 구조를 검증합니다.
 * 기능: 기본 검증 후 스키마 규칙을 적용합니다.
 * 이유: 장비/버전별 데이터 품질을 표준화하기 위함입니다.
 */
public class JsonSchemaValidator implements Validator {
    private final Validator baseValidator;
    private final SchemaRegistry schemaRegistry;
    private final SchemaValidator schemaValidator;

    /**
     * 목적: 기본 검증기와 스키마 검증기를 주입받습니다.
     * 이유: 구성에 따라 검증 수준을 유연하게 조절하기 위함입니다.
     */
    public JsonSchemaValidator(Validator baseValidator, SchemaRegistry schemaRegistry, SchemaValidator schemaValidator) {
        this.baseValidator = baseValidator;
        this.schemaRegistry = schemaRegistry;
        this.schemaValidator = schemaValidator;
    }

    @Override
    public ValidationResult validate(EnvelopeCandidate candidate, ClassificationResult classificationResult) {
        ValidationResult baseResult = baseValidator.validate(candidate, classificationResult);
        if (!baseResult.isPass()) {
            return baseResult;
        }

        Map<String, Object> payload = candidate.getNormalizedPayload();
        String schemaVersion = findString(payload, "schemaVersion", "schema_version");
        String messageType = extractMessageType(candidate.getMessageType());
        String deviceTypeId = classificationResult == null ? null : classificationResult.getDeviceTypeId();

        SchemaKey key = new SchemaKey(schemaVersion, messageType, deviceTypeId);
        String schemaJson = schemaRegistry.findSchema(key);
        SchemaValidationResult schemaResult = schemaValidator.validate(payload, schemaJson);
        if (!schemaResult.isPass()) {
            ValidationResult result = new ValidationResult();
            result.setPass(false);
            result.setReason(schemaResult.getReason());
            return result;
        }

        ValidationResult result = new ValidationResult();
        result.setPass(true);
        result.setReason(null);
        return result;
    }

    /**
     * 목적: payload에서 문자열 값을 조회합니다.
     * 이유: 다양한 키 표현을 흡수하기 위함입니다.
     */
    private String findString(Map<String, Object> payload, String key, String alias) {
        if (payload == null) {
            return null;
        }
        Object value = payload.get(key);
        if (value == null && alias != null) {
            value = payload.get(alias);
        }
        return value == null ? null : String.valueOf(value).trim();
    }

    /**
     * 목적: MessageType을 문자열로 변환합니다.
     * 이유: 스키마 키를 일관된 형식으로 생성하기 위함입니다.
     */
    private String extractMessageType(MessageType messageType) {
        return messageType == null ? null : messageType.name();
    }
}
