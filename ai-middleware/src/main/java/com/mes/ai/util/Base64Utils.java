package com.mes.ai.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 인코딩/디코딩 유틸리티입니다.
 * 목적: 원본 payload를 안전하게 저장하기 위해 텍스트로 변환합니다.
 * 기능: 문자열을 Base64로 인코딩/디코딩합니다.
 * 이유: 바이너리/특수문자 포함 데이터를 안정적으로 보관하기 위함입니다.
 */
public final class Base64Utils {
    /** 유틸리티 클래스이므로 외부에서 인스턴스화하지 못하게 합니다. */
    private Base64Utils() {
    }

    /**
     * 문자열을 Base64로 인코딩합니다.
     * 목적: 원본 문자열을 안전한 저장 형태로 변환합니다.
     */
    public static String encode(String value) {
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64 문자열을 원문 문자열로 복원합니다.
     * 목적: 저장된 원본을 다시 처리할 수 있게 합니다.
     */
    public static String decodeToString(String base64) {
        if (base64 == null) {
            return null;
        }
        byte[] decoded = Base64.getDecoder().decode(base64);
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
