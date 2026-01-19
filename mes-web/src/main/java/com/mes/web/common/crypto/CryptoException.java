package com.mes.web.common.crypto;

/**
 * 목적: 암호 처리 예외를 표준화한다.
 * 기능: 암호화/복호화 실패를 공통 예외로 감싼다.
 * 이유: 호출부에서 일관된 예외 처리를 하기 위함이다.
 * 유지보수: 예외 분류가 필요하면 확장한다.
 */
public class CryptoException extends RuntimeException {

    /**
     * 목적: 메시지를 포함한 예외를 생성한다.
     * 기능: 원인 메시지를 저장한다.
     * 이유: 실패 원인을 추적하기 위함이다.
     * 유지보수: 메시지 구조 변경 시 호출부를 점검한다.
     */
    public CryptoException(String message) {
        super(message);
    }

    /**
     * 목적: 메시지와 원인을 포함한 예외를 생성한다.
     * 기능: 예외 체인을 저장한다.
     * 이유: 상세 원인을 보존하기 위함이다.
     * 유지보수: 예외 체인 정책 변경 시 수정한다.
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
