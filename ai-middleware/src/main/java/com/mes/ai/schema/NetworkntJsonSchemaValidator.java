package com.mes.ai.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mes.ai.util.JacksonUtils;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.util.Map;
import java.util.Set;

/**
 * networknt JSON Schema 검증기입니다.
 * 목적: 표준 JSON Schema 규격으로 검증 품질을 높입니다.
 * 기능: 스키마와 payload를 JsonNode로 변환해 검증합니다.
 * 이유: 실서비스 수준의 스키마 검증이 필요하기 때문입니다.
 * 유지보수: 스키마 버전/검증 정책 변경 시 이 클래스에서 조정합니다.
 */
public class NetworkntJsonSchemaValidator implements SchemaValidator {
    private final ObjectMapper objectMapper = JacksonUtils.getObjectMapper();
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    /**
     * 목적: JSON payload가 스키마를 만족하는지 검증합니다.
     * 기능: 스키마 로딩 후 payload를 JsonNode로 변환해 검증합니다.
     * 이유: 표준 스키마 규칙을 기준으로 데이터 품질을 확보하기 위함입니다.
     * 유지보수: 오류 메시지 포맷/검증 방식 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public SchemaValidationResult validate(Map<String, Object> payload, String schemaJson) {
        if (schemaJson == null || schemaJson.trim().isEmpty()) {
            return new SchemaValidationResult(false, "VALIDATION_SCHEMA_MISMATCH:스키마 없음");
        }
        if (payload == null) {
            return new SchemaValidationResult(false, "VALIDATION_SCHEMA_MISMATCH:payload 없음");
        }
        try {
            JsonSchema schema = schemaFactory.getSchema(schemaJson);
            JsonNode payloadNode = objectMapper.valueToTree(payload);
            Set<ValidationMessage> errors = schema.validate(payloadNode);
            if (!errors.isEmpty()) {
                ValidationMessage first = errors.iterator().next();
                return new SchemaValidationResult(false, "VALIDATION_SCHEMA_MISMATCH:" + first.getMessage());
            }
            return new SchemaValidationResult(true, null);
        } catch (Exception ex) {
            return new SchemaValidationResult(false, "VALIDATION_SCHEMA_MISMATCH:스키마 처리 실패");
        }
    }
}
