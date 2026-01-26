package com.mes.web.common.validation;

import java.util.Map;

/**
 * 목적: 입력값 검증을 공통화한다.
 * 기능: 필수값/숫자/날짜 검증과 정규화를 제공한다.
 * 이유: 컨트롤러 중복 코드를 줄이고 검증 품질을 높이기 위함이다.
 * 유지보수: 검증 규칙 변경 시 이 클래스만 수정한다.
 */
public final class ValidationUtils {

    private static final String DATE_REGEX = "^\\d{4}-\\d{2}-\\d{2}$";

    private ValidationUtils() {
        // 유틸리티 클래스이므로 인스턴스 생성을 막는다.
    }

    /**
     * 목적: 필수값 여부를 검증한다.
     * 기능: 값이 비어있으면 오류 메시지를 반환한다.
     * 이유: 필수 입력 누락을 사전에 차단하기 위함이다.
     * 유지보수: 메시지 형식 변경 시 이 메서드를 수정한다.
     */
    public static String require(Map<String, Object> data, String key, String label) {
        Object value = data.get(key);
        if (isBlank(value)) {
            return label + "은(는) 필수입니다.";
        }
        return null;
    }

    /**
     * 목적: 날짜 형식을 검증한다.
     * 기능: 값이 있을 때 YYYY-MM-DD 형식인지 확인한다.
     * 이유: 잘못된 날짜 입력을 방지하기 위함이다.
     * 유지보수: 날짜 포맷 변경 시 정규식을 수정한다.
     */
    public static String validateDate(Map<String, Object> data, String key, String label) {
        Object value = data.get(key);
        if (isBlank(value)) {
            return null;
        }
        String text = value.toString().trim();
        if (!text.matches(DATE_REGEX)) {
            return label + " 형식은 YYYY-MM-DD입니다.";
        }
        return null;
    }

    /**
     * 목적: 숫자 형식을 검증한다.
     * 기능: 값이 숫자인지 확인하고 범위를 검사한다.
     * 이유: 숫자 필드 입력 오류를 방지하기 위함이다.
     * 유지보수: 범위 정책 변경 시 파라미터를 조정한다.
     */
    public static String validateInt(Map<String, Object> data, String key, String label, int min, int max) {
        Object value = data.get(key);
        if (isBlank(value)) {
            return null;
        }
        String text = value.toString().trim();
        try {
            int parsed = Integer.parseInt(text);
            if (parsed < min || parsed > max) {
                return label + " 값이 허용 범위를 벗어났습니다.";
            }
            return null;
        } catch (NumberFormatException ex) {
            return label + " 값은 숫자만 입력 가능합니다.";
        }
    }

    /**
     * 목적: 정수 값을 정규화한다.
     * 기능: 문자열 숫자를 Integer로 변환해 Map에 저장한다.
     * 이유: DB 저장 시 타입 일관성을 유지하기 위함이다.
     * 유지보수: 파싱 정책 변경 시 이 메서드를 수정한다.
     */
    public static void normalizeInt(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (isBlank(value)) {
            return;
        }
        String text = value.toString().trim();
        try {
            data.put(key, Integer.valueOf(text));
        } catch (NumberFormatException ex) {
            // 검증 단계에서 오류가 반환되므로 여기서는 무시한다.
        }
    }

    /**
     * 목적: 공백 여부를 확인한다.
     * 기능: null 또는 빈 문자열인지 검사한다.
     * 이유: 검증 로직을 단순화하기 위함이다.
     * 유지보수: 공백 기준 변경 시 로직을 보완한다.
     */
    public static boolean isBlank(Object value) {
        return value == null || value.toString().trim().isEmpty();
    }
}
