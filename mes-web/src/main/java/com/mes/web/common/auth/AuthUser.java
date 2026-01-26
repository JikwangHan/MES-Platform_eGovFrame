package com.mes.web.common.auth;

/**
 * 목적: 인증된 사용자 정보를 담는다.
 * 기능: 로그인 세션에 필요한 최소 정보를 제공한다.
 * 이유: 화면 접근 제어와 감사 로그 기록에 사용하기 위함이다.
 * 유지보수: 필요 정보가 늘어나면 필드를 확장한다.
 */
public class AuthUser {

    private final String userId;
    private final String userName;
    private final String role;

    /**
     * 목적: 인증 사용자 객체를 생성한다.
     * 기능: 필수 사용자 정보를 저장한다.
     * 이유: 세션에서 일관된 사용자 정보를 사용하기 위함이다.
     * 유지보수: 생성 파라미터가 늘어나면 생성자를 확장한다.
     */
    public AuthUser(String userId, String userName, String role) {
        this.userId = userId;
        this.userName = userName;
        this.role = role;
    }

    /**
     * 목적: 사용자 ID를 반환한다.
     * 기능: 로그인 식별자를 제공한다.
     * 이유: 감사 로그와 권한 제어에 필요하다.
     * 유지보수: 식별 규칙 변경 시 호출부를 점검한다.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 목적: 사용자 이름을 반환한다.
     * 기능: 화면 표시 이름을 제공한다.
     * 이유: 화면 상단/로그 등에 표시하기 위함이다.
     * 유지보수: 마스킹 정책 변경 시 호출부를 점검한다.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 목적: 사용자 역할을 반환한다.
     * 기능: 권한 구분 값을 제공한다.
     * 이유: 권한별 메뉴/버튼 제어에 필요하다.
     * 유지보수: 역할 체계 변경 시 값 범위를 조정한다.
     * 참고: 역할 코드는 SYSTEM_ADMIN/MANAGER/OPERATOR/VIEWER를 사용한다.
     */
    public String getRole() {
        return role;
    }
}
