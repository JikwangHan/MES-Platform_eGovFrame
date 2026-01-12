package com.mes.ai.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 정규화 단계에서 공통 키 별칭을 표준 키로 맞추는 유틸리티입니다.
 * 목적: 입력 포맷마다 다른 키 이름을 표준 키로 통일합니다.
 * 기능: 별칭 키를 찾아 표준 키에 매핑합니다.
 * 이유: 검증/저장 단계가 동일한 키를 기준으로 동작하기 때문입니다.
 * 유지보수: 새로운 별칭이 생기면 이 클래스에만 추가하면 됩니다.
 */
public final class PayloadNormalizationUtils {
    /**
     * 목적: 유틸리티 클래스의 인스턴스화를 방지합니다.
     * 기능: 외부에서 생성자를 호출할 수 없게 합니다.
     * 이유: 모든 기능을 정적 메서드로 제공하기 위함입니다.
     */
    private PayloadNormalizationUtils() {
    }

    /**
     * 표준 키 별칭을 적용한 payload를 반환합니다.
     * 목적: 표준 키(deviceId 등)를 항상 확보하도록 보완합니다.
     * 기능: 기존 키가 없을 때만 별칭 값을 표준 키로 복사합니다.
     * 이유: 입력 포맷 차이로 인한 검증 실패를 줄이기 위함입니다.
     * 유지보수: 별칭 목록이 늘어나면 copyIfMissing 호출만 추가하면 됩니다.
     */
    public static Map<String, Object> applyStandardAliases(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> normalized = new LinkedHashMap<>(payload);

        copyIfMissing(normalized, "deviceId", payload, "device_id", "device-id", "device");
        copyIfMissing(normalized, "deviceTypeId", payload, "device_type_id", "device_type", "device-type");
        copyIfMissing(normalized, "eventId", payload, "event_id", "event-id", "event");
        copyIfMissing(normalized, "timestamp", payload, "eventTime", "event_time", "time", "ts");
        copyIfMissing(normalized, "protocolVersion", payload, "protocol_version", "protocol");
        copyIfMissing(normalized, "schemaVersion", payload, "schema_version", "schema");
        copyIfMissing(normalized, "messageType", payload, "message_type", "type", "msgType", "msg_type");

        return normalized;
    }

    /**
     * 목적: 표준 키가 없을 때만 별칭 값을 복사합니다.
     * 기능: 표준 키를 기준으로 별칭 키를 순차적으로 확인합니다.
     * 이유: 기존 표준 키 값을 덮어쓰지 않도록 보호하기 위함입니다.
     * 유지보수: 복사 규칙 변경 시 이 메서드를 수정합니다.
     */
    private static void copyIfMissing(
            Map<String, Object> target,
            String standardKey,
            Map<String, Object> source,
            String... aliases
    ) {
        if (target == null || source == null || standardKey == null) {
            return;
        }
        if (hasValue(target.get(standardKey))) {
            return;
        }
        if (aliases == null) {
            return;
        }
        for (String alias : aliases) {
            if (alias == null) {
                continue;
            }
            Object value = source.get(alias);
            if (hasValue(value)) {
                target.put(standardKey, value);
                return;
            }
        }
    }

    /**
     * 값이 비어 있지 않은지 확인합니다.
     * 목적: null/공백 문자열을 구분합니다.
     * 기능: 문자열로 변환해 공백 여부를 검사합니다.
     * 이유: 빈 값이 표준 키에 복사되지 않도록 하기 위함입니다.
     * 유지보수: 값 판단 규칙 변경 시 이 메서드를 수정합니다.
     */
    private static boolean hasValue(Object value) {
        if (value == null) {
            return false;
        }
        return !String.valueOf(value).trim().isEmpty();
    }
}
