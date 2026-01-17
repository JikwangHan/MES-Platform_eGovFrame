package com.mes.ai.crypto;

/**
 * 암호화를 수행하지 않는 기본 구현체입니다.
 * 목적: 암호화 미사용 환경에서도 호출부를 단순하게 유지합니다.
 * 기능: 입력 문자열을 그대로 반환합니다.
 * 이유: 개발 초기/테스트 환경에서 암호화를 선택적으로 비활성화하기 위함입니다.
 * 유지보수: 암호화 기본 정책이 바뀌면 이 클래스의 반환 정책을 조정합니다.
 */
public class NoopCryptoService implements CryptoService {
    /**
     * 목적: 암호화 비활성 상태에서 원문을 그대로 반환합니다.
     * 기능: 전달된 문자열을 수정 없이 반환합니다.
     * 이유: 암호화 미사용 모드에서도 저장 흐름을 유지하기 위함입니다.
     * 유지보수: 기본 동작 변경 시 이 메서드를 수정합니다.
     */
    @Override
    public String encrypt(String plainText, String aad) {
        return plainText;
    }

    /**
     * 목적: 암호화 비활성 상태를 반환합니다.
     * 기능: false를 반환합니다.
     * 이유: 운영 점검에서 비활성 상태를 구분하기 위함입니다.
     */
    @Override
    public boolean isEnabled() {
        return false;
    }
}
