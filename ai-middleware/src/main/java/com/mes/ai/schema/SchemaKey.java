package com.mes.ai.schema;

/**
 * 스키마를 구분하는 키입니다.
 * 목적: 스키마 버전/메시지 유형/장비 유형을 조합해 식별합니다.
 * 기능: Map 키로 사용될 수 있도록 동등성 비교를 제공합니다.
 * 이유: 동일한 기준으로 스키마를 안정적으로 찾기 위함입니다.
 */
public class SchemaKey {
    private final String schemaVersion;
    private final String messageType;
    private final String deviceTypeId;

    /**
     * 목적: 스키마 식별 값을 설정합니다.
     * 이유: 조회 기준을 명확히 고정하기 위함입니다.
     */
    public SchemaKey(String schemaVersion, String messageType, String deviceTypeId) {
        this.schemaVersion = schemaVersion;
        this.messageType = messageType;
        this.deviceTypeId = deviceTypeId;
    }

    /**
     * 목적: 스키마 버전을 조회합니다.
     * 이유: 동일 버전 스키마를 찾기 위함입니다.
     */
    public String getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * 목적: 메시지 유형을 조회합니다.
     * 이유: 유형별 스키마 구분을 위해 필요합니다.
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * 목적: 장비 유형 ID를 조회합니다.
     * 이유: 장비별 스키마를 찾기 위함입니다.
     */
    public String getDeviceTypeId() {
        return deviceTypeId;
    }

    /**
     * 목적: 스키마 키 동등성을 비교합니다.
     * 이유: Map 조회 시 정확한 키 매칭을 위해 필요합니다.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SchemaKey)) {
            return false;
        }
        SchemaKey other = (SchemaKey) object;
        return safeEquals(schemaVersion, other.schemaVersion)
                && safeEquals(messageType, other.messageType)
                && safeEquals(deviceTypeId, other.deviceTypeId);
    }

    /**
     * 목적: 스키마 키 해시를 계산합니다.
     * 이유: Map 키로 사용할 때 성능과 일관성을 확보합니다.
     */
    @Override
    public int hashCode() {
        int result = safeHash(schemaVersion);
        result = 31 * result + safeHash(messageType);
        result = 31 * result + safeHash(deviceTypeId);
        return result;
    }

    /**
     * 목적: null-safe 문자열 비교를 수행합니다.
     * 이유: null 값 포함 상황에서도 예외 없이 비교합니다.
     */
    private boolean safeEquals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    /**
     * 목적: null-safe 해시 값을 계산합니다.
     * 이유: null 값 포함 상황에서도 안정적인 해시를 제공합니다.
     */
    private int safeHash(String value) {
        return value == null ? 0 : value.hashCode();
    }
}
