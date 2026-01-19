package com.mes.web.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 목적: BCrypt 해시를 생성한다.
 * 기능: 입력 문자열을 BCrypt로 해시하고 출력한다.
 * 이유: seed.sql에 안전한 비밀번호 해시를 넣기 위함이다.
 * 유지보수: 해시 정책 변경 시 이 클래스만 수정한다.
 */
public class BcryptTool {

    /**
     * 목적: CLI로 입력받은 평문을 해시한다.
     * 기능: BCrypt 해시를 표준 출력으로 제공한다.
     * 이유: 개발 환경에서 빠르게 해시를 생성하기 위함이다.
     * 유지보수: 입력 방식 변경 시 파라미터 로직을 보완한다.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("ERROR: 패스워드를 인자로 전달하세요.");
            return;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(args[0]);
        System.out.println(hash);
    }
}
