package com.mes.web.common.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 목적: 개발 단계 무료 오픈소스 기반 암호화를 제공한다.
 * 기능: AES-GCM 암복호화와 SHA-256 해시를 수행한다.
 * 이유: 상용화 시 KCMVP 모듈로 교체 가능하도록 경계를 고정하기 위함이다.
 * 유지보수: KCMVP 구현체로 교체 시 이 클래스만 교체한다.
 */
@Service
public class FreeCryptoServiceImpl implements CryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreeCryptoServiceImpl.class);
    private static final String DEFAULT_ALG = "AES-128-GCM";
    private static final String DEFAULT_VERSION = "1";
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 목적: 평문을 암호문 컨테이너로 변환한다.
     * 기능: AES-GCM으로 암호화하고 컨테이너 JSON을 생성한다.
     * 이유: 암호문 포맷을 고정해 교체 가능성을 유지하기 위함이다.
     * 유지보수: 알고리즘 변경 시 알고리즘/키 길이를 점검한다.
     */
    @Override
    public String encrypt(String plaintext, String aad) {
        try {
            SecretKey key = getSecretKey();
            byte[] nonce = new byte[NONCE_LENGTH];
            new SecureRandom().nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            if (aad != null) {
                cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
            }
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            int tagLengthBytes = TAG_LENGTH_BITS / 8;
            byte[] ciphertext = new byte[encrypted.length - tagLengthBytes];
            byte[] tag = new byte[tagLengthBytes];
            System.arraycopy(encrypted, 0, ciphertext, 0, ciphertext.length);
            System.arraycopy(encrypted, ciphertext.length, tag, 0, tag.length);

            CryptoContainer container = new CryptoContainer();
            container.setVersion(DEFAULT_VERSION);
            container.setAlg(DEFAULT_ALG);
            container.setKid(getKeyId());
            container.setKeyVersion(getKeyVersion());
            container.setNonce(Base64.getEncoder().encodeToString(nonce));
            if (aad != null) {
                container.setAad(Base64.getEncoder().encodeToString(aad.getBytes(StandardCharsets.UTF_8)));
            }
            container.setCiphertext(Base64.getEncoder().encodeToString(ciphertext));
            container.setTag(Base64.getEncoder().encodeToString(tag));
            return objectMapper.writeValueAsString(container);
        } catch (Exception ex) {
            throw new CryptoException("암호화 실패", ex);
        }
    }

    /**
     * 목적: 암호문 컨테이너를 복호화한다.
     * 기능: 컨테이너 JSON을 파싱해 AES-GCM 복호를 수행한다.
     * 이유: 컨테이너 포맷 고정을 유지하기 위함이다.
     * 유지보수: 포맷 버전이 증가하면 파싱 조건을 보완한다.
     */
    @Override
    public String decrypt(String container) {
        try {
            CryptoContainer parsed = objectMapper.readValue(container, CryptoContainer.class);
            byte[] nonce = Base64.getDecoder().decode(parsed.getNonce());
            byte[] ciphertext = Base64.getDecoder().decode(parsed.getCiphertext());
            byte[] tag = Base64.getDecoder().decode(parsed.getTag());

            byte[] combined = new byte[ciphertext.length + tag.length];
            System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
            System.arraycopy(tag, 0, combined, ciphertext.length, tag.length);

            SecretKey key = getSecretKey();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            if (parsed.getAad() != null) {
                byte[] aadBytes = Base64.getDecoder().decode(parsed.getAad());
                cipher.updateAAD(aadBytes);
            }
            byte[] decrypted = cipher.doFinal(combined);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new CryptoException("복호화 실패", ex);
        }
    }

    /**
     * 목적: 입력 문자열의 SHA-256 해시를 생성한다.
     * 기능: Base64 인코딩된 해시 값을 반환한다.
     * 이유: 무결성 검증과 로그 추적에 사용하기 위함이다.
     * 유지보수: 알고리즘 교체 시 구현을 변경한다.
     */
    @Override
    public String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception ex) {
            throw new CryptoException("해시 생성 실패", ex);
        }
    }

    /**
     * 목적: 암호 키를 조회한다.
     * 기능: 시스템 속성 또는 환경 변수에서 Base64 키를 읽는다.
     * 이유: 개발/운영 환경에서 안전하게 키를 주입하기 위함이다.
     * 유지보수: 키 관리 정책 변경 시 읽기 경로를 조정한다.
     */
    private SecretKey getSecretKey() {
        String base64Key = System.getProperty("mes.crypto.key.base64");
        if (base64Key == null || base64Key.trim().isEmpty()) {
            base64Key = System.getenv("MES_CRYPTO_KEY_BASE64");
        }
        if (base64Key == null || base64Key.trim().isEmpty()) {
            LOGGER.warn("암호 키가 설정되지 않아 기본 키를 사용한다. 운영 전 반드시 교체해야 한다.");
            base64Key = "AAAAAAAAAAAAAAAAAAAAAA==";
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 목적: 키 ID를 조회한다.
     * 기능: 시스템 속성 또는 환경 변수에서 키 ID를 읽는다.
     * 이유: 키 회전 추적을 위해 필요하다.
     * 유지보수: 키 정책 변경 시 키 ID 관리 규칙을 조정한다.
     */
    private String getKeyId() {
        String keyId = System.getProperty("mes.crypto.key.id");
        if (keyId == null || keyId.trim().isEmpty()) {
            keyId = System.getenv("MES_CRYPTO_KEY_ID");
        }
        return keyId == null || keyId.trim().isEmpty() ? "dev-key" : keyId;
    }

    /**
     * 목적: 키 버전을 조회한다.
     * 기능: 시스템 속성 또는 환경 변수에서 키 버전을 읽는다.
     * 이유: 재암호화/회전 정책을 유지하기 위함이다.
     * 유지보수: 버전 정책 변경 시 기본값을 조정한다.
     */
    private String getKeyVersion() {
        String keyVersion = System.getProperty("mes.crypto.key.version");
        if (keyVersion == null || keyVersion.trim().isEmpty()) {
            keyVersion = System.getenv("MES_CRYPTO_KEY_VERSION");
        }
        return keyVersion == null || keyVersion.trim().isEmpty() ? "v1" : keyVersion;
    }
}
