package com.mes.web.common.crypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 목적: 암호문 컨테이너 포맷을 정의한다.
 * 기능: 컨테이너의 필드와 직렬화 규칙을 고정한다.
 * 이유: 무료 구현과 상용 모듈 전환 시 포맷 호환을 유지하기 위함이다.
 * 유지보수: 필드 추가가 필요하면 버전을 올리고 확장한다.
 */
@JsonInclude(Include.NON_NULL)
public class CryptoContainer {

    private String version;
    private String alg;
    private String kid;
    private String keyVersion;
    private String nonce;
    private String aad;
    private String ciphertext;
    private String tag;

    /**
     * 목적: 버전 정보를 조회한다.
     * 기능: 컨테이너 버전을 반환한다.
     * 이유: 포맷 변경 시 구분을 위해 필요하다.
     * 유지보수: 버전 규칙 변경 시 호출부를 점검한다.
     */
    public String getVersion() {
        return version;
    }

    /**
     * 목적: 버전 정보를 설정한다.
     * 기능: 컨테이너 버전을 저장한다.
     * 이유: 포맷 호환을 유지하기 위함이다.
     * 유지보수: 버전 관리 정책 변경 시 사용한다.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 목적: 알고리즘 정보를 조회한다.
     * 기능: 암호 알고리즘 값을 반환한다.
     * 이유: 복호 시 정책 검증에 사용한다.
     * 유지보수: 알고리즘 변경 시 관리한다.
     */
    public String getAlg() {
        return alg;
    }

    /**
     * 목적: 알고리즘 정보를 설정한다.
     * 기능: 암호 알고리즘 값을 저장한다.
     * 이유: 포맷 고정을 유지하기 위함이다.
     * 유지보수: 알고리즘 변경 시 호출부를 점검한다.
     */
    public void setAlg(String alg) {
        this.alg = alg;
    }

    /**
     * 목적: 키 ID를 조회한다.
     * 기능: 암호화에 사용한 키 ID를 반환한다.
     * 이유: 키 회전/폐기 추적을 위해 필요하다.
     * 유지보수: 키 정책 변경 시 갱신한다.
     */
    public String getKid() {
        return kid;
    }

    /**
     * 목적: 키 ID를 설정한다.
     * 기능: 암호화에 사용한 키 ID를 저장한다.
     * 이유: 키 관리 정책을 유지하기 위함이다.
     * 유지보수: 키 전략 변경 시 수정한다.
     */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * 목적: 키 버전을 조회한다.
     * 기능: 키 버전 값을 반환한다.
     * 이유: 회전/재암호화 추적을 위해 필요하다.
     * 유지보수: 버전 체계 변경 시 사용한다.
     */
    public String getKeyVersion() {
        return keyVersion;
    }

    /**
     * 목적: 키 버전을 설정한다.
     * 기능: 키 버전 값을 저장한다.
     * 이유: 포맷 호환성을 유지하기 위함이다.
     * 유지보수: 버전 규칙 변경 시 갱신한다.
     */
    public void setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
    }

    /**
     * 목적: nonce 값을 조회한다.
     * 기능: nonce(Base64)를 반환한다.
     * 이유: GCM/CCM에서 재사용 금지 정책을 유지하기 위함이다.
     * 유지보수: nonce 정책 변경 시 관리한다.
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * 목적: nonce 값을 설정한다.
     * 기능: nonce(Base64)를 저장한다.
     * 이유: 암호문 복호에 필요하기 때문이다.
     * 유지보수: 길이 정책 변경 시 점검한다.
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * 목적: AAD 값을 조회한다.
     * 기능: 추가 인증 데이터(Base64)를 반환한다.
     * 이유: 무결성 보호 범위를 확장하기 위함이다.
     * 유지보수: 정책 변경 시 추가 필드를 고려한다.
     */
    public String getAad() {
        return aad;
    }

    /**
     * 목적: AAD 값을 설정한다.
     * 기능: 추가 인증 데이터(Base64)를 저장한다.
     * 이유: 복호 시 동일한 AAD가 필요하기 때문이다.
     * 유지보수: 사용 정책 변경 시 관리한다.
     */
    public void setAad(String aad) {
        this.aad = aad;
    }

    /**
     * 목적: ciphertext 값을 조회한다.
     * 기능: 암호문(Base64)을 반환한다.
     * 이유: 복호에 필요한 본문을 제공하기 위함이다.
     * 유지보수: 인코딩 정책 변경 시 점검한다.
     */
    public String getCiphertext() {
        return ciphertext;
    }

    /**
     * 목적: ciphertext 값을 설정한다.
     * 기능: 암호문(Base64)을 저장한다.
     * 이유: 암호문 컨테이너 포맷을 유지하기 위함이다.
     * 유지보수: 저장 방식 변경 시 관리한다.
     */
    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }

    /**
     * 목적: tag 값을 조회한다.
     * 기능: 인증 태그(Base64)를 반환한다.
     * 이유: 무결성 검증에 필요하기 때문이다.
     * 유지보수: 태그 길이 변경 시 점검한다.
     */
    public String getTag() {
        return tag;
    }

    /**
     * 목적: tag 값을 설정한다.
     * 기능: 인증 태그(Base64)를 저장한다.
     * 이유: 복호 시 검증에 사용하기 위함이다.
     * 유지보수: 태그 정책 변경 시 관리한다.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
}
