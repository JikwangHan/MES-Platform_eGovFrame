package com.mes.ai.crypto;

/**
 * 암호문 컨테이너 포맷을 표현하는 모델입니다.
 * 목적: 암호문을 표준 필드 구조로 일관되게 저장합니다.
 * 기능: 버전/알고리즘/키 정보/nonce/aad/ciphertext/tag를 보관합니다.
 * 이유: 무료 구현과 상용 모듈 간 포맷 호환성을 유지하기 위함입니다.
 * 유지보수: 포맷 필드 확장 시 이 클래스에서 조정합니다.
 */
public class CryptoEnvelope {
    private int version;
    private String alg;
    private String kid;
    private String keyVersion;
    private String nonce;
    private String aad;
    private String ciphertext;
    private String tag;

    /**
     * 목적: 컨테이너 버전을 조회합니다.
     * 기능: 버전 값을 반환합니다.
     * 이유: 포맷 호환성 판단에 필요합니다.
     */
    public int getVersion() {
        return version;
    }

    /**
     * 목적: 컨테이너 버전을 설정합니다.
     * 기능: 전달받은 버전을 저장합니다.
     * 이유: 버전별 파싱/검증 정책을 구분하기 위함입니다.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * 목적: 알고리즘 식별자를 조회합니다.
     * 기능: 알고리즘 문자열을 반환합니다.
     * 이유: 상용화 전환 시 알고리즘 호환을 확인하기 위함입니다.
     */
    public String getAlg() {
        return alg;
    }

    /**
     * 목적: 알고리즘 식별자를 설정합니다.
     * 기능: 전달받은 문자열을 저장합니다.
     * 이유: 암호문 해석 기준을 명확히 하기 위함입니다.
     */
    public void setAlg(String alg) {
        this.alg = alg;
    }

    /**
     * 목적: 키 ID를 조회합니다.
     * 기능: 키 ID 문자열을 반환합니다.
     * 이유: 키 회전/폐기 추적에 필요합니다.
     */
    public String getKid() {
        return kid;
    }

    /**
     * 목적: 키 ID를 설정합니다.
     * 기능: 전달받은 문자열을 저장합니다.
     * 이유: 동일 데이터의 키 출처를 추적하기 위함입니다.
     */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * 목적: 키 버전을 조회합니다.
     * 기능: 키 버전 문자열을 반환합니다.
     * 이유: 회전/재암호화 정책 적용에 사용합니다.
     */
    public String getKeyVersion() {
        return keyVersion;
    }

    /**
     * 목적: 키 버전을 설정합니다.
     * 기능: 전달받은 문자열을 저장합니다.
     * 이유: 키 버전 기반의 감사/추적을 위해 필요합니다.
     */
    public void setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
    }

    /**
     * 목적: nonce 값을 조회합니다.
     * 기능: Base64 인코딩된 nonce를 반환합니다.
     * 이유: 복호화 시 동일 nonce가 필요하기 때문입니다.
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * 목적: nonce 값을 설정합니다.
     * 기능: Base64 인코딩된 nonce를 저장합니다.
     * 이유: 컨테이너 포맷 고정을 위해 필요합니다.
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * 목적: AAD 값을 조회합니다.
     * 기능: Base64 인코딩된 AAD를 반환합니다.
     * 이유: 무결성 검증 시 동일 AAD가 필요하기 때문입니다.
     */
    public String getAad() {
        return aad;
    }

    /**
     * 목적: AAD 값을 설정합니다.
     * 기능: Base64 인코딩된 AAD를 저장합니다.
     * 이유: 컨테이너에 AAD를 명시적으로 보관하기 위함입니다.
     */
    public void setAad(String aad) {
        this.aad = aad;
    }

    /**
     * 목적: 암호문을 조회합니다.
     * 기능: Base64 인코딩된 ciphertext를 반환합니다.
     * 이유: 복호화를 위한 원문 데이터이기 때문입니다.
     */
    public String getCiphertext() {
        return ciphertext;
    }

    /**
     * 목적: 암호문을 설정합니다.
     * 기능: Base64 인코딩된 ciphertext를 저장합니다.
     * 이유: 암호문 컨테이너의 핵심 데이터이기 때문입니다.
     */
    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }

    /**
     * 목적: 인증 태그를 조회합니다.
     * 기능: Base64 인코딩된 tag를 반환합니다.
     * 이유: 복호화 시 무결성 검증에 필요합니다.
     */
    public String getTag() {
        return tag;
    }

    /**
     * 목적: 인증 태그를 설정합니다.
     * 기능: Base64 인코딩된 tag를 저장합니다.
     * 이유: 변조 방지를 위해 필요합니다.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
}
