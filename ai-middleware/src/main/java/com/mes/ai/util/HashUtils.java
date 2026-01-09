package com.mes.ai.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 해시를 생성합니다.
 * 목적: 민감 정보 보호와 무결성 확인에 활용합니다.
 * 기능: 문자열을 SHA-256 해시(16진수)로 변환합니다.
 * 이유: 원문을 저장하지 않고도 동일성 확인이 가능하게 합니다.
 * 유지보수: 구조 변경 시 이 클래스에서 조정합니다.
 */
public final class HashUtils {
    /**
     * 목적: 유틸리티 클래스의 인스턴스화를 방지합니다.
     * 기능: 외부에서 생성자를 호출할 수 없게 합니다.
     * 이유: 모든 기능을 정적 메서드로 제공하기 위함입니다.
     */
    private HashUtils() {
    }

    /**
     * 문자열을 SHA-256 해시(16진수)로 변환합니다.
     * 목적: 원본 문자열을 노출하지 않고도 식별할 수 있게 합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    public static String sha256Hex(String value) {
        if (value == null) {
            return null;
        }
        try {
            // 표준 알고리즘을 사용하여 안정적인 해시를 생성합니다.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환합니다.
     * 목적: 로그/저장에 사용하기 쉬운 형식으로 변환합니다.
     * 기능: 필요한 처리를 수행합니다.
     * 이유: 기능 흐름을 한 곳에서 담당하기 위함입니다.
     * 유지보수: 로직 변경 시 이 메서드를 수정합니다.
     */
    private static String toHex(byte[] data) {
        StringBuilder builder = new StringBuilder(data.length * 2);
        for (byte value : data) {
            String hex = Integer.toHexString(value & 0xff);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }
}
