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
 * 유지보수: 스키마 저장소/검증기 교체 시 이 클래스에서 조정합니다.
 */
public class JsonSchemaValidator implements Validator {
    /** 스키마 미등록 처리 정책(System Property) 키입니다. */
    private static final String MISSING_POLICY_KEY = "ai.schema.missingPolicy";
    /** 스키마 미등록 시 기본 정책입니다. */
    private static final String DEFAULT_MISSING_POLICY = "fail";
    private final Validator baseValidator;
    private final SchemaRegistry schemaRegistry;
    private final SchemaValidator schemaValidator;

    /**
     * 목적: 기본 검증기와 스키마 검증기를 주입받습니다.
     * 기능: 주입받은 검증기/레지스트리를 내부 필드에 저장합니다.
     * 이유: 구성에 따라 검증 수준을 유연하게 조절하기 위함입니다.
     * 유지보수: 검증 구성 변경 시 생성자 파라미터를 확장합니다.
     */
    public JsonSchemaValidator(Validator baseValidator, SchemaRegistry schemaRegistry, SchemaValidator schemaValidator) {
        this.baseValidator = baseValidator;
        this.schemaRegistry = schemaRegistry;
        this.schemaValidator = schemaValidator;
    }

    /**
     * 목적: 스키마 기반 검증을 수행합니다.
     * 기능: 기본 검증 통과 후 스키마 조회/검증 결과를 반영합니다.
     * 이유: 기본 검증만으로는 구조적 오류를 걸러낼 수 없기 때문입니다.
     * 유지보수: 정책 분기/오류 코드 규칙 변경 시 이 메서드를 수정합니다.
     */
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
        if (isSchemaMissing(schemaJson)) {
            // 스키마가 없을 때는 운영 정책에 따라 통과/격리로 분기합니다.
            return handleMissingSchema(key);
        }
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
     * 기능: 기본 키와 별칭 키를 순차적으로 확인합니다.
     * 이유: 다양한 키 표현을 흡수하기 위함입니다.
     * 유지보수: 키 별칭 추가는 호출부에서 확장합니다.
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
     * 기능: null 안전하게 name 값을 반환합니다.
     * 이유: 스키마 키를 일관된 형식으로 생성하기 위함입니다.
     * 유지보수: 메시지 타입 규칙 변경 시 이 메서드를 조정합니다.
     */
    private String extractMessageType(MessageType messageType) {
        return messageType == null ? null : messageType.name();
    }

    /**
     * 스키마가 비어 있는지 확인합니다.
     * 목적: 미등록 스키마 처리 정책을 적용하기 위함입니다.
     * 기능: 스키마 문자열의 null/공백 여부를 검사합니다.
     * 이유: 비어 있는 스키마는 유효하지 않기 때문입니다.
     * 유지보수: 스키마 로딩 방식 변경 시 기준을 수정합니다.
     */
    private boolean isSchemaMissing(String schemaJson) {
        return schemaJson == null || schemaJson.trim().isEmpty();
    }

    /**
     * 스키마 미등록 시 정책에 따라 처리합니다.
     * 목적: 운영 환경에서 격리/통과 정책을 유연하게 선택합니다.
     * 기능: 정책 값에 따라 pass/warn/fail 결과를 생성합니다.
     * 이유: 스키마 미등록 상황의 처리 기준이 환경마다 다르기 때문입니다.
     * 유지보수: 신규 정책이 추가되면 이 메서드를 확장합니다.
     * 정책 값:
     *  - fail: 격리(기본값)
     *  - pass: 스키마 없이 통과
     *  - warn: 통과하되 경고 사유를 기록
     */
    private ValidationResult handleMissingSchema(SchemaKey key) {
        String policy = resolveMissingPolicy();
        if ("pass".equals(policy)) {
            ValidationResult result = new ValidationResult();
            result.setPass(true);
            result.setReason(null);
            return result;
        }
        if ("warn".equals(policy)) {
            ValidationResult result = new ValidationResult();
            result.setPass(true);
            result.setReason("SCHEMA_MISSING_WARN:" + formatKey(key));
            return result;
        }
        ValidationResult result = new ValidationResult();
        result.setPass(false);
        result.setReason("VALIDATION_SCHEMA_MISMATCH:스키마 없음(" + formatKey(key) + ")");
        return result;
    }

    /**
     * 정책 값을 조회합니다.
     * 목적: 시스템 속성으로 정책을 변경 가능하게 합니다.
     * 기능: 미설정 시 기본 정책을 반환합니다.
     * 이유: 운영 환경에서 유연하게 정책을 바꾸기 위함입니다.
     * 유지보수: 정책 키 변경 시 상수를 함께 수정합니다.
     */
    private String resolveMissingPolicy() {
        String raw = System.getProperty(MISSING_POLICY_KEY);
        if (raw == null || raw.trim().isEmpty()) {
            return DEFAULT_MISSING_POLICY;
        }
        return raw.trim().toLowerCase();
    }

    /**
     * 스키마 키를 요약 문자열로 변환합니다.
     * 목적: 경고 메시지에 컨텍스트를 포함하기 위함입니다.
     * 기능: schemaVersion/messageType/deviceTypeId를 결합합니다.
     * 이유: 운영자가 어떤 스키마가 부족한지 빠르게 파악하기 위함입니다.
     * 유지보수: 표시 포맷 변경 시 이 메서드를 수정합니다.
     */
    private String formatKey(SchemaKey key) {
        if (key == null) {
            return "schemaKey=null";
        }
        return "schemaVersion=" + key.getSchemaVersion()
                + ",messageType=" + key.getMessageType()
                + ",deviceTypeId=" + key.getDeviceTypeId();
    }
}
