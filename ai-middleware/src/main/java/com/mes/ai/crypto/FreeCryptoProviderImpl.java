package com.mes.ai.crypto;

import com.mes.ai.util.JacksonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 무료 오픈소스 기반 암호화 구현체입니다.
 * 목적: 개발/PoC 단계에서 비용 없이 암호화를 적용합니다.
 * 기능: AES-GCM 기반 AEAD 암호화를 수행하고 컨테이너 포맷으로 출력합니다.
 * 이유: 상용화 단계에서 KCMVP 모듈로 교체 가능한 구조를 유지하기 위함입니다.
 * 유지보수: 알고리즘/포맷 변경 시 이 클래스만 수정합니다.
 */
public class FreeCryptoProviderImpl implements CryptoService {
    /** 컨테이너 버전 값입니다. */
    private static final int CONTAINER_VERSION = 1;
    /** GCM 태그 길이(비트)입니다. */
    private static final int GCM_TAG_LENGTH_BITS = 128;
    /** GCM nonce 길이(바이트)입니다. */
    private static final int GCM_NONCE_LENGTH_BYTES = 12;

    /** 암호 키입니다. */
    private final SecretKey secretKey;
    /** 키 식별자입니다. */
    private final String keyId;
    /** 키 버전입니다. */
    private final String keyVersion;
    /** 알고리즘 표기 문자열입니다. */
    private final String algorithmName;
    /** 난수 생성기입니다. */
    private final SecureRandom secureRandom;
    /** JSON 직렬화 도구입니다. */
    private final ObjectMapper objectMapper;

    /**
     * 목적: 필수 의존성을 주입받아 암호화 구현체를 구성합니다.
     * 기능: 키/알고리즘/난수/직렬화 도구를 내부에 보관합니다.
     * 이유: 암호화 설정을 외부에서 제어할 수 있게 하기 위함입니다.
     * 유지보수: 키 정책 변경 시 생성자 인자를 확장합니다.
     */
    public FreeCryptoProviderImpl(SecretKey secretKey, String keyId, String keyVersion, String algorithmName) {
        this(secretKey, keyId, keyVersion, algorithmName, new SecureRandom(), JacksonUtils.getObjectMapper());
    }

    /**
     * 목적: 테스트/운영 환경에서 필요한 구성 요소를 주입합니다.
     * 기능: 난수/직렬화기를 외부에서 주입받아 동작을 고정합니다.
     * 이유: 테스트 재현성과 운영 정책 분리를 위해 필요합니다.
     * 유지보수: 의존성이 늘어나면 이 생성자를 확장합니다.
     */
    public FreeCryptoProviderImpl(
            SecretKey secretKey,
            String keyId,
            String keyVersion,
            String algorithmName,
            SecureRandom secureRandom,
            ObjectMapper objectMapper
    ) {
        this.secretKey = secretKey;
        this.keyId = keyId;
        this.keyVersion = keyVersion;
        this.algorithmName = algorithmName;
        this.secureRandom = secureRandom;
        this.objectMapper = objectMapper;
    }

    /**
     * 목적: 문자열을 AEAD 암호문 컨테이너로 변환합니다.
     * 기능: AES-GCM으로 암호화한 뒤 컨테이너 JSON을 반환합니다.
     * 이유: 컨테이너 포맷을 고정하여 상용 모듈 교체를 대비합니다.
     * 유지보수: 컨테이너 필드가 바뀌면 이 메서드를 수정합니다.
     */
    @Override
    public String encrypt(String plainText, String aad) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] nonce = generateNonce();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
            byte[] aadBytes = toAadBytes(aad);
            if (aadBytes != null) {
                cipher.updateAAD(aadBytes);
            }
            byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.doFinal(plainBytes);
            int tagLengthBytes = GCM_TAG_LENGTH_BITS / 8;
            byte[] ciphertext = new byte[encrypted.length - tagLengthBytes];
            byte[] tag = new byte[tagLengthBytes];
            System.arraycopy(encrypted, 0, ciphertext, 0, ciphertext.length);
            System.arraycopy(encrypted, ciphertext.length, tag, 0, tag.length);

            CryptoEnvelope envelope = new CryptoEnvelope();
            envelope.setVersion(CONTAINER_VERSION);
            envelope.setAlg(algorithmName);
            envelope.setKid(keyId);
            envelope.setKeyVersion(keyVersion);
            envelope.setNonce(Base64.getEncoder().encodeToString(nonce));
            envelope.setAad(aadBytes == null ? null : Base64.getEncoder().encodeToString(aadBytes));
            envelope.setCiphertext(Base64.getEncoder().encodeToString(ciphertext));
            envelope.setTag(Base64.getEncoder().encodeToString(tag));
            return objectMapper.writeValueAsString(envelope);
        } catch (Exception ex) {
            throw new IllegalStateException("CRYPTO_ENCRYPT_FAILED:암호화에 실패했습니다.", ex);
        }
    }

    /**
     * 목적: 암호화 활성 상태를 반환합니다.
     * 기능: true를 반환합니다.
     * 이유: 활성 구현체임을 명시하기 위함입니다.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 목적: GCM nonce를 생성합니다.
     * 기능: SecureRandom으로 12바이트 nonce를 생성합니다.
     * 이유: nonce 재사용 금지 정책을 만족하기 위함입니다.
     * 유지보수: nonce 길이 정책 변경 시 이 메서드를 수정합니다.
     */
    private byte[] generateNonce() {
        byte[] nonce = new byte[GCM_NONCE_LENGTH_BYTES];
        secureRandom.nextBytes(nonce);
        return nonce;
    }

    /**
     * 목적: AAD 문자열을 바이트 배열로 변환합니다.
     * 기능: UTF-8 인코딩으로 변환합니다.
     * 이유: AAD는 암호문 무결성 검증에 필요하기 때문입니다.
     * 유지보수: 인코딩 정책 변경 시 이 메서드를 수정합니다.
     */
    private byte[] toAadBytes(String aad) {
        if (aad == null || aad.trim().isEmpty()) {
            return null;
        }
        return aad.getBytes(StandardCharsets.UTF_8);
    }
}
