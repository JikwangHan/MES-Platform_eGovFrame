package com.mes.web.common.mail;

/**
 * 목적: 이메일 발송 기능을 표준화한다.
 * 기능: 회원가입 인증 메일 등 주요 알림을 전송한다.
 * 이유: 발송 로직을 한 곳에 모아 유지보수를 쉽게 하기 위함이다.
 * 유지보수: SMTP/외부 API 교체 시 구현체만 변경한다.
 */
public interface EmailService {

    /**
     * 목적: 이메일 발송 가능 여부를 확인한다.
     * 기능: 설정 값이 충분한지 판단해 true/false를 반환한다.
     * 이유: 미설정 상태에서 실패를 최소화하기 위함이다.
     * 유지보수: 필수 설정 항목이 바뀌면 여기서 수정한다.
     */
    boolean isEnabled();

    /**
     * 목적: 회원가입 이메일 인증 메일을 발송한다.
     * 기능: 인증 코드와 안내 문구를 이메일로 전송한다.
     * 이유: 이메일 인증 흐름을 완료하기 위함이다.
     * 유지보수: 템플릿 변경 시 본문 생성 로직을 수정한다.
     */
    void sendSignupVerification(String toEmail, String userId, String code);

    /**
     * 목적: 사용자 승인 결과 알림을 발송한다.
     * 기능: 승인/보류 결과를 이메일로 안내한다.
     * 이유: 관리자 승인 결과를 사용자에게 즉시 전달하기 위함이다.
     * 유지보수: 템플릿 변경 시 구현체를 수정한다.
     */
    void sendApprovalResult(String toEmail, String userId, boolean approved);
}
