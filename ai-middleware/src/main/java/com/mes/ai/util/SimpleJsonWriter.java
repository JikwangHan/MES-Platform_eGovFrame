package com.mes.ai.util;

import java.util.Iterator;
import java.util.Map;

/**
 * 간단한 JSON 직렬화 유틸리티입니다.
 * 목적: 외부 라이브러리 없이 Map을 JSON 문자열로 변환합니다.
 * 기능: 문자열/숫자/불리언/null을 기본 타입으로 직렬화합니다.
 * 이유: DB 저장 시 표준 payload를 JSON으로 보관하기 위함입니다.
 */
public final class SimpleJsonWriter {
    /** 유틸리티 클래스이므로 외부에서 인스턴스화하지 못하게 합니다. */
    private SimpleJsonWriter() {
    }

    /**
     * Map을 JSON 문자열로 변환합니다.
     * 목적: 표준 payload를 DB에 저장 가능한 텍스트로 만들기 위함입니다.
     */
    public static String toJson(Map<String, Object> payload) {
        if (payload == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        Iterator<Map.Entry<String, Object>> iterator = payload.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            builder.append('"').append(escape(entry.getKey())).append('"').append(':');
            builder.append(toJsonValue(entry.getValue()));
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append('}');
        return builder.toString();
    }

    /**
     * 값 타입에 맞게 JSON 값을 생성합니다.
     * 목적: 기본 타입을 JSON 규격에 맞게 변환합니다.
     */
    private static String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return toJson(map);
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    /**
     * JSON 문자열 이스케이프 처리입니다.
     * 목적: 큰따옴표/역슬래시 등이 깨지지 않도록 합니다.
     */
    private static String escape(String value) {
        StringBuilder builder = new StringBuilder();
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    builder.append(ch);
                    break;
            }
        }
        return builder.toString();
    }
}
