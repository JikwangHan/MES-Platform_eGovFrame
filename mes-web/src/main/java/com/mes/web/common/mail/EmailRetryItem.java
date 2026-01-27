package com.mes.web.common.mail;

/**
 * 목적: 이메일 재시도 항목을 표현한다.
 * 기능: 발송 대상/제목/본문/시도 횟수를 보관한다.
 * 이유: 발송 실패 시 큐에 적재하여 재처리하기 위함이다.
 * 유지보수: 필요 필드 추가 시 확장한다.
 */
public class EmailRetryItem {

    private final String toEmail;
    private final String subject;
    private final String textBody;
    private final String htmlBody;
    private int attempts;
    private final int maxAttempts;

    /**
     * 목적: 재시도 항목을 생성한다.
     * 기능: 이메일 정보를 초기화한다.
     * 이유: 재시도 큐에서 동일한 값을 사용하기 위함이다.
     * 유지보수: 필드 확장 시 생성자를 보완한다.
     */
    public EmailRetryItem(String toEmail, String subject, String textBody, String htmlBody, int maxAttempts) {
        this.toEmail = toEmail;
        this.subject = subject;
        this.textBody = textBody;
        this.htmlBody = htmlBody;
        this.maxAttempts = maxAttempts;
    }

    /**
     * 목적: 수신자 이메일을 조회한다.
     * 기능: 재시도 대상 주소를 반환한다.
     * 이유: 재발송 대상 정보를 제공하기 위함이다.
     * 유지보수: 주소 정책 변경 시 반환 형식을 확인한다.
     */
    public String getToEmail() {
        return toEmail;
    }

    /**
     * 목적: 메일 제목을 조회한다.
     * 기능: 재발송에 사용할 제목을 반환한다.
     * 이유: 동일한 제목으로 재발송하기 위함이다.
     * 유지보수: 제목 정책 변경 시 호출부를 점검한다.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * 목적: 텍스트 본문을 조회한다.
     * 기능: 재발송 텍스트 본문을 반환한다.
     * 이유: HTML 미지원 환경을 대비하기 위함이다.
     * 유지보수: 본문 정책 변경 시 반환 값을 확인한다.
     */
    public String getTextBody() {
        return textBody;
    }

    /**
     * 목적: HTML 본문을 조회한다.
     * 기능: HTML 형식의 본문을 반환한다.
     * 이유: 템플릿 기반 발송에 사용하기 위함이다.
     * 유지보수: HTML 포맷 변경 시 호출부를 점검한다.
     */
    public String getHtmlBody() {
        return htmlBody;
    }

    /**
     * 목적: 현재 시도 횟수를 조회한다.
     * 기능: 재시도 횟수를 반환한다.
     * 이유: 재시도 제한 판단에 사용하기 위함이다.
     * 유지보수: 카운트 정책 변경 시 확인한다.
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * 목적: 최대 시도 횟수를 조회한다.
     * 기능: 재시도 제한 값을 반환한다.
     * 이유: 무한 재시도를 방지하기 위함이다.
     * 유지보수: 정책 변경 시 반환 값을 확인한다.
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * 목적: 재시도 횟수를 증가시킨다.
     * 기능: 시도 횟수를 1 증가한다.
     * 이유: 재시도 제한을 관리하기 위함이다.
     * 유지보수: 정책 변경 시 로직을 수정한다.
     */
    public void incrementAttempts() {
        attempts += 1;
    }

    /**
     * 목적: 추가 재시도가 가능한지 판단한다.
     * 기능: 최대 시도 횟수와 비교한다.
     * 이유: 무한 재시도를 방지하기 위함이다.
     * 유지보수: 정책 변경 시 로직을 수정한다.
     */
    public boolean canRetry() {
        return attempts < maxAttempts;
    }
}
