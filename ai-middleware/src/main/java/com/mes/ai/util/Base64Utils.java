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
     * 문자열을 Base64로 인코딩합니다.
     * 목적: 기존 호출부 호환을 유지하면서 인코딩 기능을 제공합니다.
     * 이유: 메서드 이름 차이로 인한 컴파일 오류를 예방합니다.
     */
    public static String encodeString(String value) {
        return encode(value);
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

    /**
     * Base64 문자열을 바이트 배열로 복원합니다.
     * 목적: 바이너리 파일 스캔 등 바이트 단위 처리가 필요할 때 사용합니다.
     * 이유: 문자열 복원 시 손실될 수 있는 바이너리 데이터를 그대로 유지합니다.
     */
    public static byte[] decodeToBytes(String base64) {
        if (base64 == null) {
            return null;
        }
        return Base64.getDecoder().decode(base64);
    }
}
