package com.mes.ai.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * CryptoService 생성 팩토리입니다.
 * 목적: 시스템 속성 기반으로 암호화 구현체를 선택합니다.
 * 기능: FreeCryptoProviderImpl 또는 NoopCryptoService를 반환합니다.
 * 이유: 운영 환경에서 코드 수정 없이 암호화 on/off를 전환하기 위함입니다.
 * 유지보수: 속성 키/정책 변경 시 이 클래스만 수정합니다.
 */
public final class CryptoServiceFactory {
    /** 활성화 여부 시스템 속성 키입니다. */
    private static final String ENABLED_KEY = "ai.crypto.enabled";
    /** 키(Base64) 시스템 속성 키입니다. */
    private static final String KEY_BASE64 = "ai.crypto.key.base64";
    /** 키 식별자 시스템 속성 키입니다. */
    private static final String KEY_ID = "ai.crypto.key.id";
    /** 키 버전 시스템 속성 키입니다. */
    private static final String KEY_VERSION = "ai.crypto.key.version";

    /** 싱글톤 인스턴스입니다. */
    private static volatile CryptoService instance;

    private CryptoServiceFactory() {
    }

    /**
     * 목적: 암호화 서비스 인스턴스를 반환합니다.
     * 기능: 시스템 속성 기준으로 구현체를 선택합니다.
     * 이유: 개발/운영 환경에 따라 암호화 적용 여부가 다르기 때문입니다.
     * 유지보수: 초기화 정책 변경 시 이 메서드를 수정합니다.
     */
    public static CryptoService getInstance() {
        if (instance == null) {
            synchronized (CryptoServiceFactory.class) {
                if (instance == null) {
                    instance = createService();
                }
            }
        }
        return instance;
    }

    /**
     * 목적: 속성 기반으로 CryptoService를 생성합니다.
     * 기능: enabled 설정과 키 존재 여부를 확인합니다.
     * 이유: 잘못된 설정으로 인한 암호화 실패를 조기에 감지하기 위함입니다.
     * 유지보수: 정책 키/기본값 변경 시 이 메서드를 수정합니다.
     */
    private static CryptoService createService() {
        String enabled = System.getProperty(ENABLED_KEY);
        if (enabled == null || enabled.trim().isEmpty() || !Boolean.parseBoolean(enabled.trim())) {
            return new NoopCryptoService();
        }
        String keyBase64 = System.getProperty(KEY_BASE64);
        if (keyBase64 == null || keyBase64.trim().isEmpty()) {
            throw new IllegalStateException("CRYPTO_KEY_MISSING: ai.crypto.key.base64가 필요합니다.");
        }
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64.trim());
        if (keyBytes.length != 16 && keyBytes.length != 32) {
            throw new IllegalStateException("CRYPTO_KEY_INVALID: AES 키 길이는 16 또는 32바이트여야 합니다.");
        }
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        String keyId = normalizeProperty(KEY_ID, "dev-key");
        String keyVersion = normalizeProperty(KEY_VERSION, "v1");
        String algName = keyBytes.length == 32 ? "AES-256-GCM" : "AES-128-GCM";
        return new FreeCryptoProviderImpl(secretKey, keyId, keyVersion, algName);
    }

    /**
     * 목적: 시스템 속성 기본값을 적용합니다.
     * 기능: 빈 문자열일 경우 기본값을 반환합니다.
     * 이유: 필수 값이 없을 때도 최소 식별자를 유지하기 위함입니다.
     * 유지보수: 기본값 정책 변경 시 이 메서드를 수정합니다.
     */
    private static String normalizeProperty(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
