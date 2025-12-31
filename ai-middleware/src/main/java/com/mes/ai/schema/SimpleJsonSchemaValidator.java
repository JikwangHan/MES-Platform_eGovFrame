package com.mes.ai.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 최소 JSON Schema 검증기입니다.
 * 목적: 외부 라이브러리 없이 기본 스키마 검증을 수행합니다.
 * 기능: required/properties/type 규칙만 지원합니다.
 * 이유: 초기 단계에서 스키마 검증 흐름을 확보하기 위함입니다.
 * 제한: 복잡한 스키마(참조/조건/배열 상세 등)는 지원하지 않습니다.
 */
public class SimpleJsonSchemaValidator implements SchemaValidator {
    private static final Pattern REQUIRED_SECTION = Pattern.compile("\"required\"\\s*:\\s*\\[(.*?)\\]");
    private static final Pattern PROPERTY_SECTION = Pattern.compile("\"properties\"\\s*:\\s*\\{(.*?)\\}\\s*(,|\\})");
    private static final Pattern PROPERTY_ENTRY = Pattern.compile("\"(.*?)\"\\s*:\\s*\\{(.*?)\\}");
    private static final Pattern TYPE_FIELD = Pattern.compile("\"type\"\\s*:\\s*\"(.*?)\"");

    @Override
    public SchemaValidationResult validate(Map<String, Object> payload, String schemaJson) {
        if (schemaJson == null || schemaJson.trim().isEmpty()) {
            return new SchemaValidationResult(false, "VALIDATION_SCHEMA_MISMATCH:스키마 없음");
        }
        if (payload == null) {
            return new SchemaValidationResult(false, "VALIDATION_SCHEMA_MISMATCH:payload 없음");
        }

        List<String> requiredFields = parseRequired(schemaJson);
        for (String field : requiredFields) {
            if (!payload.containsKey(field) || payload.get(field) == null) {
                return new SchemaValidationResult(false, "VALIDATION_MISSING_FIELD:" + field);
            }
        }

        Map<String, String> typeRules = parsePropertyTypes(schemaJson);
        for (Map.Entry<String, String> entry : typeRules.entrySet()) {
            String field = entry.getKey();
            String expectedType = entry.getValue();
            if (!payload.containsKey(field) || payload.get(field) == null) {
                continue;
            }
            if (!matchesType(payload.get(field), expectedType)) {
                return new SchemaValidationResult(false, "VALIDATION_INVALID_TYPE:" + field);
            }
        }

        return new SchemaValidationResult(true, null);
    }

    /**
     * required 배열을 추출합니다.
     * 목적: 필수 필드 목록을 얻기 위함입니다.
     */
    private List<String> parseRequired(String schemaJson) {
        Matcher matcher = REQUIRED_SECTION.matcher(schemaJson);
        if (!matcher.find()) {
            return new ArrayList<>();
        }
        String requiredBody = matcher.group(1);
        return extractQuotedStrings(requiredBody);
    }

    /**
     * properties/type 규칙을 추출합니다.
     * 목적: 필드별 타입 검증을 수행하기 위함입니다.
     */
    private Map<String, String> parsePropertyTypes(String schemaJson) {
        Matcher propertiesMatcher = PROPERTY_SECTION.matcher(schemaJson);
        if (!propertiesMatcher.find()) {
            return new HashMap<>();
        }
        String propertiesBody = propertiesMatcher.group(1);
        Matcher entryMatcher = PROPERTY_ENTRY.matcher(propertiesBody);
        Map<String, String> rules = new HashMap<>();
        while (entryMatcher.find()) {
            String fieldName = entryMatcher.group(1);
            String fieldBody = entryMatcher.group(2);
            Matcher typeMatcher = TYPE_FIELD.matcher(fieldBody);
            if (typeMatcher.find()) {
                rules.put(fieldName, typeMatcher.group(1));
            }
        }
        return rules;
    }

    /**
     * 따옴표로 감싸진 문자열 리스트를 추출합니다.
     * 목적: required 배열의 요소를 파싱하기 위함입니다.
     */
    private List<String> extractQuotedStrings(String source) {
        List<String> result = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"(.*?)\"").matcher(source);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    /**
     * 값이 스키마 타입과 일치하는지 확인합니다.
     * 목적: 기본 타입 검증을 수행합니다.
     */
    private boolean matchesType(Object value, String expectedType) {
        if (expectedType == null) {
            return true;
        }
        switch (expectedType) {
            case "string":
                return value instanceof String;
            case "number":
                return value instanceof Number;
            case "boolean":
                return value instanceof Boolean;
            case "object":
                return value instanceof Map;
            case "array":
                return value instanceof List || value.getClass().isArray();
            default:
                return true;
        }
    }
}
