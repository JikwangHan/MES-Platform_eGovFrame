package com.mes.ai.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mes.ai.crypto.CryptoService;
import com.mes.ai.crypto.CryptoServiceFactory;
import com.mes.ai.util.JacksonUtils;

import java.util.Map;

/**
 * CryptoService 스모크 테스트 실행기입니다.
 * 목적: 무료 오픈소스 암호화가 정상 동작하는지 빠르게 확인합니다.
 * 기능: 컨테이너 포맷/nonce 재사용 방지 여부를 점검합니다.
 * 이유: 운영 전 최소 품질을 확보하기 위함입니다.
 * 유지보수: 컨테이너 포맷 변경 시 검증 항목을 수정합니다.
 */
public class CryptoServiceSmokeRunner {
    /**
     * 목적: 암호화 스모크 테스트를 실행합니다.
     * 기능: 동일 입력 2회 암호화 후 nonce 차이를 확인합니다.
     * 이유: nonce 재사용 금지 정책을 보장하기 위함입니다.
     */
    public static void main(String[] args) throws Exception {
        CryptoService cryptoService = CryptoServiceFactory.getInstance();
        if (!cryptoService.isEnabled()) {
            throw new IllegalStateException("암호화가 비활성 상태입니다. ai.crypto.enabled=true로 설정해야 합니다.");
        }
        ObjectMapper mapper = JacksonUtils.getObjectMapper();
        String aad = "crypto_smoke";
        String input = "AI_MIDDLEWARE_CRYPTO_SMOKE";

        String encrypted1 = cryptoService.encrypt(input, aad);
        String encrypted2 = cryptoService.encrypt(input, aad);

        Map<String, Object> env1 = mapper.readValue(encrypted1, Map.class);
        Map<String, Object> env2 = mapper.readValue(encrypted2, Map.class);

        String nonce1 = safeText(env1.get("nonce"));
        String nonce2 = safeText(env2.get("nonce"));
        String ciphertext1 = safeText(env1.get("ciphertext"));
        String tag1 = safeText(env1.get("tag"));

        boolean nonceDifferent = !nonce1.equals(nonce2);
        boolean hasCiphertext = !ciphertext1.isEmpty();
        boolean hasTag = !tag1.isEmpty();

        System.out.println("=== CryptoService 스모크 테스트 결과 ===");
        System.out.println("암호화 활성화: " + cryptoService.isEnabled());
        System.out.println("nonce 재사용 방지: " + (nonceDifferent ? "PASS" : "FAIL"));
        System.out.println("ciphertext 존재: " + (hasCiphertext ? "PASS" : "FAIL"));
        System.out.println("tag 존재: " + (hasTag ? "PASS" : "FAIL"));
        System.out.println("======================================");

        if (!nonceDifferent || !hasCiphertext || !hasTag) {
            throw new IllegalStateException("CRYPTO_SMOKE_FAILED: 암호화 스모크 테스트 실패");
        }
    }

    /**
     * 목적: null 값을 안전한 문자열로 변환합니다.
     * 기능: null이면 빈 문자열을 반환합니다.
     * 이유: 간단한 비교 로직에서 NPE를 방지하기 위함입니다.
     */
    private static String safeText(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value).trim();
    }
}
