package com.mes.ai.schema;

import java.util.Map;

/**
 * JSON Schema 검증 인터페이스입니다.
 * 목적: 스키마 검증 로직을 표준화합니다.
 * 기능: payload와 스키마 JSON을 받아 검증 결과를 반환합니다.
 * 이유: 구현 교체가 쉬운 구조를 만들기 위함입니다.
 */
public interface SchemaValidator {
    /**
     * 스키마 검증을 수행합니다.
     * 목적: 스키마 규칙을 만족하는지 확인합니다.
     */
    SchemaValidationResult validate(Map<String, Object> payload, String schemaJson);
}
