package com.mes.ai.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 외부 라이브러리 없이 동작하는 최소 JSON 파서입니다.
 * 목적: 의존성 없이 JSON을 해석할 수 있게 합니다.
 * 기능: 최상위 평면 객체의 키/값을 Map으로 변환합니다.
 * 이유: 초기 단계에서 빠르게 포맷 검증을 수행하기 위함입니다.
 * 제한: 중첩 객체/배열은 문자열 형태로 유지됩니다.
 */
public final class SimpleJsonParser {
    /** 유틸리티 클래스이므로 외부에서 인스턴스화하지 못하게 합니다. */
    private SimpleJsonParser() {
    }

    /**
     * JSON 문자열을 Map으로 파싱합니다.
     * 목적: 최상위 객체의 키/값을 추출합니다.
     * 기능: 문자열, 숫자, 불리언, null을 기본 타입으로 변환합니다.
     */
    public static Map<String, Object> parseObject(String json) {
        if (json == null) {
            throw new IllegalArgumentException("JSON 문자열이 비어 있습니다.");
        }
        String source = json.trim();
        if (source.length() < 2 || source.charAt(0) != '{' || source.charAt(source.length() - 1) != '}') {
            throw new IllegalArgumentException("JSON 객체 형식이 아닙니다.");
        }

        // 입력 순서를 유지하기 위해 LinkedHashMap을 사용합니다.
        Map<String, Object> result = new LinkedHashMap<>();
        int index = 1;
        while (index < source.length() - 1) {
            // 공백을 건너뛰어 실제 데이터 시작 위치를 찾습니다.
            index = skipWhitespace(source, index);
            if (index >= source.length() - 1) {
                break;
            }
            if (source.charAt(index) == '}') {
                break;
            }

            // 키는 JSON 규격상 문자열이므로 문자열 파서를 사용합니다.
            ParseResult keyResult = parseString(source, index);
            String key = (String) keyResult.value;
            index = skipWhitespace(source, keyResult.index);

            if (index >= source.length() || source.charAt(index) != ':') {
                throw new IllegalArgumentException("JSON 키/값 구분자가 없습니다.");
            }
            index++;
            index = skipWhitespace(source, index);

            // 값은 타입에 따라 다른 파서를 호출합니다.
            ParseResult valueResult = parseValue(source, index);
            result.put(key, valueResult.value);
            index = skipWhitespace(source, valueResult.index);

            // 다음 항목이 있으면 콤마를 소비합니다.
            if (index < source.length() && source.charAt(index) == ',') {
                index++;
            }
        }
        return result;
    }

    /**
     * JSON 값의 타입을 판별하여 적절한 파서를 호출합니다.
     * 목적: 문자열/숫자/불리언/null/구조를 분기 처리합니다.
     */
    private static ParseResult parseValue(String source, int index) {
        if (index >= source.length()) {
            throw new IllegalArgumentException("JSON 값이 누락되었습니다.");
        }
        char ch = source.charAt(index);
        if (ch == '"') {
            return parseString(source, index);
        }
        if (ch == '{' || ch == '[') {
            return parseRawStructure(source, index);
        }
        if (source.startsWith("true", index)) {
            return new ParseResult(Boolean.TRUE, index + 4);
        }
        if (source.startsWith("false", index)) {
            return new ParseResult(Boolean.FALSE, index + 5);
        }
        if (source.startsWith("null", index)) {
            return new ParseResult(null, index + 4);
        }
        return parseNumber(source, index);
    }

    /**
     * 숫자 값을 파싱합니다.
     * 목적: 정수/실수를 구분해 적절한 타입으로 변환합니다.
     */
    private static ParseResult parseNumber(String source, int index) {
        int start = index;
        while (index < source.length()) {
            char ch = source.charAt(index);
            if (Character.isDigit(ch) || ch == '-' || ch == '+' || ch == '.' || ch == 'e' || ch == 'E') {
                index++;
            } else {
                break;
            }
        }
        String token = source.substring(start, index).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("JSON 숫자 형식이 잘못되었습니다.");
        }
        try {
            if (token.contains(".") || token.contains("e") || token.contains("E")) {
                return new ParseResult(Double.parseDouble(token), index);
            }
            return new ParseResult(Long.parseLong(token), index);
        } catch (NumberFormatException ex) {
            // 숫자 파싱 실패 시 원문 토큰을 그대로 반환합니다.
            return new ParseResult(token, index);
        }
    }

    /**
     * 중첩 객체/배열을 그대로 문자열로 반환합니다.
     * 목적: 최소 파서이므로 내부 구조는 추후 처리로 넘깁니다.
     */
    private static ParseResult parseRawStructure(String source, int index) {
        char open = source.charAt(index);
        char close = open == '{' ? '}' : ']';
        int depth = 0;
        int i = index;
        boolean inString = false;
        while (i < source.length()) {
            char ch = source.charAt(i);
            if (ch == '"' && !isEscaped(source, i)) {
                inString = !inString;
            }
            if (!inString) {
                // 중첩 깊이를 계산해 정확히 닫히는 지점을 찾습니다.
                if (ch == open) {
                    depth++;
                } else if (ch == close) {
                    depth--;
                    if (depth == 0) {
                        i++;
                        break;
                    }
                }
            }
            i++;
        }
        if (depth != 0) {
            throw new IllegalArgumentException("JSON 구조가 닫히지 않았습니다.");
        }
        return new ParseResult(source.substring(index, i), i);
    }

    /**
     * JSON 문자열을 파싱합니다.
     * 목적: 이스케이프 시퀀스를 해석해 실제 문자열을 복원합니다.
     */
    private static ParseResult parseString(String source, int index) {
        if (source.charAt(index) != '"') {
            throw new IllegalArgumentException("문자열 시작 구분자가 없습니다.");
        }
        StringBuilder builder = new StringBuilder();
        int i = index + 1;
        while (i < source.length()) {
            char ch = source.charAt(i);
            if (ch == '"' && !isEscaped(source, i)) {
                return new ParseResult(builder.toString(), i + 1);
            }
            if (ch == '\\' && i + 1 < source.length()) {
                char next = source.charAt(i + 1);
                // 이스케이프 문자를 실제 문자로 변환합니다.
                builder.append(unescape(next, source, i));
                if (next == 'u') {
                    // \\uXXXX 형태는 총 6글자를 소비합니다.
                    i += 6;
                } else {
                    i += 2;
                }
                continue;
            }
            builder.append(ch);
            i++;
        }
        throw new IllegalArgumentException("JSON 문자열이 닫히지 않았습니다.");
    }

    /**
     * 현재 따옴표가 이스케이프인지 판단합니다.
     * 목적: 문자열 종료 지점을 정확히 찾기 위함입니다.
     */
    private static boolean isEscaped(String source, int index) {
        int backslashCount = 0;
        int i = index - 1;
        while (i >= 0 && source.charAt(i) == '\\') {
            backslashCount++;
            i--;
        }
        return backslashCount % 2 == 1;
    }

    /**
     * JSON 이스케이프 문자를 실제 문자로 변환합니다.
     * 목적: 파싱 결과를 사람이 읽을 수 있게 복원합니다.
     */
    private static char unescape(char next, String source, int index) {
        switch (next) {
            case '"':
            case '\\':
            case '/':
                return next;
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'u':
                if (index + 5 >= source.length()) {
                    throw new IllegalArgumentException("유니코드 이스케이프가 잘못되었습니다.");
                }
                String hex = source.substring(index + 2, index + 6);
                return (char) Integer.parseInt(hex, 16);
            default:
                return next;
        }
    }

    /**
     * 공백 문자를 건너뜁니다.
     * 목적: 다음 토큰의 시작 위치를 찾습니다.
     */
    private static int skipWhitespace(String source, int index) {
        int i = index;
        while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
            i++;
        }
        return i;
    }

    /**
     * 파싱 결과(값 + 다음 인덱스)를 묶는 내부 클래스입니다.
     * 목적: 파서가 다음 처리 위치를 알 수 있게 합니다.
     */
    private static final class ParseResult {
        /** 파싱된 값입니다. */
        private final Object value;
        /** 다음 파싱 시작 위치입니다. */
        private final int index;

        /**
         * 파싱 결과를 생성합니다.
         * 목적: 값과 다음 위치를 함께 전달합니다.
         */
        private ParseResult(Object value, int index) {
            this.value = value;
            this.index = index;
        }
    }
}
