package com.mes.web.common.auth;

/**
 * 목적: 로그인 검증 로직을 추상화한다.
 * 기능: 로그인 성공 여부를 판단하는 인터페이스를 제공한다.
 * 이유: 향후 DB 연동/암호화 적용 시 교체를 쉽게 하기 위함이다.
 * 유지보수: 정책 변경 시 구현체만 교체한다.
 */
public interface AuthService {

    /**
     * 목적: 아이디/비밀번호를 검증한다.
     * 기능: 성공 시 사용자 표시 값을 반환한다.
     * 이유: 세션에 최소한의 사용자 식별 정보를 저장하기 위함이다.
     * 유지보수: 반환 정보 확장 시 객체로 교체한다.
     */
    AuthUser authenticate(String userId, String password);
}
