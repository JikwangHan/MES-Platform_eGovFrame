package com.mes.web.service;

/**
 * 목적: 회원가입 처리를 표준화한다.
 * 기능: 사용자 등록에 필요한 검증과 저장을 수행한다.
 * 이유: 컨트롤러와 데이터 저장 로직을 분리하기 위함이다.
 * 유지보수: 입력 항목/정책 변경 시 구현체를 수정한다.
 */
public interface UserRegistrationService {

    /**
     * 목적: 신규 사용자를 등록한다.
     * 기능: 입력값 검증, 중복 체크, 암호화/해시 처리, DB 저장을 수행한다.
     * 이유: 일관된 회원가입 정책을 적용하기 위함이다.
     * 유지보수: 필드 확장 시 파라미터와 저장 로직을 보완한다.
     */
    void registerUser(String userId,
                      String userName,
                      String password,
                      String role,
                      String phone,
                      String email,
                      String status);
}
