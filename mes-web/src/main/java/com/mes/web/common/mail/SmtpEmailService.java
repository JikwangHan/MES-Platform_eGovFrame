package com.mes.web.common.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 목적: SMTP 기반 이메일 발송을 제공한다.
 * 기능: 환경 변수/시스템 속성으로 SMTP 정보를 읽어 메일을 전송한다.
 * 이유: 외부 발송 시스템이 없을 때도 기본 이메일을 보낼 수 있게 하기 위함이다.
 * 유지보수: SMTP 설정 항목 변경 시 이 클래스만 수정한다.
 */
@Service
public class SmtpEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpEmailService.class);
    private final EmailRetryQueue retryQueue;

    /**
     * 목적: 재시도 큐를 주입받는다.
     * 기능: 발송 실패 시 큐에 적재할 수 있게 한다.
     * 이유: 실패 메일을 누락 없이 복구하기 위함이다.
     * 유지보수: 큐 구현 변경 시 주입만 수정한다.
     */
    @Autowired
    public SmtpEmailService(EmailRetryQueue retryQueue) {
        this.retryQueue = retryQueue;
    }

    /**
     * 목적: 이메일 발송 가능 여부를 확인한다.
     * 기능: SMTP 호스트/발신자 주소가 있는지 검사한다.
     * 이유: 필수 값 미설정 시 실패를 방지하기 위함이다.
     * 유지보수: 필수 값 기준이 바뀌면 로직을 보완한다.
     */
    @Override
    public boolean isEnabled() {
        String host = read("mes.mail.host", "MES_MAIL_HOST");
        String from = read("mes.mail.from", "MES_MAIL_FROM");
        return host != null && from != null;
    }

    /**
     * 목적: 회원가입 이메일 인증 메일을 발송한다.
     * 기능: 인증 코드와 안내 문구를 이메일로 전송한다.
     * 이유: 이메일 인증 흐름을 완료하기 위함이다.
     * 유지보수: 템플릿 변경 시 본문 생성 로직을 수정한다.
     */
    @Override
    public void sendSignupVerification(String toEmail, String userId, String code) {
        if (!isEnabled()) {
            throw new IllegalStateException("이메일 설정이 없어 발송할 수 없습니다.");
        }
        String subject = "[MES] 이메일 인증 코드 안내";
        String textBody = buildBody(userId, code);
        String htmlBody = buildHtmlBody(userId, code);
        sendRaw(toEmail, subject, textBody, htmlBody);
    }

    /**
     * 목적: 승인 결과 안내 메일을 발송한다.
     * 기능: 승인 여부를 안내 문구로 전달한다.
     * 이유: 관리자 승인 결과를 사용자에게 전달하기 위함이다.
     * 유지보수: 템플릿 변경 시 본문 생성 로직을 수정한다.
     */
    @Override
    public void sendApprovalResult(String toEmail, String userId, boolean approved) {
        if (!isEnabled()) {
            throw new IllegalStateException("이메일 설정이 없어 발송할 수 없습니다.");
        }
        String subject = approved ? "[MES] 회원가입 승인 완료" : "[MES] 회원가입 승인 보류";
        String textBody = buildApprovalBody(userId, approved);
        String htmlBody = buildApprovalHtmlBody(userId, approved);
        sendRaw(toEmail, subject, textBody, htmlBody);
    }

    /**
     * 목적: 이메일 원문을 발송한다.
     * 기능: 텍스트/HTML 본문을 전송한다.
     * 이유: 템플릿 기반 메일과 재시도 큐를 통합하기 위함이다.
     * 유지보수: 본문 포맷 변경 시 로직을 수정한다.
     */
    @Override
    public void sendRaw(String toEmail, String subject, String textBody, String htmlBody) {
        if (!isEnabled()) {
            throw new IllegalStateException("이메일 설정이 없어 발송할 수 없습니다.");
        }
        sendWithRetry(toEmail, subject, textBody, htmlBody, true);
    }

    /**
     * 목적: 재시도 큐 크기를 조회한다.
     * 기능: 큐에 적재된 실패 건수를 반환한다.
     * 이유: 관리자 화면 표시용으로 사용하기 위함이다.
     * 유지보수: 큐 구현 변경 시 로직을 수정한다.
     */
    @Override
    public int getRetryQueueSize() {
        if (retryQueue == null) {
            return 0;
        }
        return retryQueue.size();
    }

    /**
     * 목적: 재시도 큐를 즉시 재처리한다.
     * 기능: 큐에 적재된 메일을 재발송한다.
     * 이유: 운영자가 수동 복구를 수행할 수 있게 하기 위함이다.
     * 유지보수: 재시도 정책 변경 시 로직을 수정한다.
     */
    @Override
    public int retryFailedEmails() {
        if (retryQueue == null || !retryQueue.isEnabled()) {
            return 0;
        }
        java.util.List<EmailRetryItem> items = retryQueue.drainAll();
        int successCount = 0;
        for (EmailRetryItem item : items) {
            if (item == null) {
                continue;
            }
            item.incrementAttempts();
            try {
                sendWithRetry(item.getToEmail(), item.getSubject(), item.getTextBody(), item.getHtmlBody(), false);
                successCount += 1;
            } catch (IllegalStateException ex) {
                if (item.canRetry()) {
                    retryQueue.enqueue(item);
                }
            }
        }
        return successCount;
    }

    /**
     * 목적: SMTP 세션을 생성한다.
     * 기능: 인증 정보 유무에 따라 세션을 구성한다.
     * 이유: 인증 설정 여부에 따라 적절한 세션을 만들기 위함이다.
     * 유지보수: 인증 방식 변경 시 로직을 보완한다.
     */
    private Session buildSession(Properties props, String username, String password) {
        if (username == null || password == null) {
            return Session.getInstance(props);
        }
        return Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * 목적: 회원가입 인증용 텍스트 본문을 생성한다.
     * 기능: 아이디/인증 코드를 포함한 본문을 만든다.
     * 이유: 텍스트 메일 수신자를 지원하기 위함이다.
     * 유지보수: 문구 정책 변경 시 내용을 수정한다.
     */
    private String buildBody(String userId, String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("안녕하세요. MES 회원가입 이메일 인증 안내입니다.\n\n");
        sb.append("아이디: ").append(userId).append("\n");
        sb.append("인증 코드: ").append(code).append("\n\n");
        sb.append("해당 코드를 인증 화면에 입력해 주세요.\n");
        sb.append("본 메일은 자동 발송되며, 회신하지 마세요.\n");
        return sb.toString();
    }

    /**
     * 목적: 회원가입 인증용 HTML 본문을 생성한다.
     * 기능: 카드형 안내 UI로 내용을 구성한다.
     * 이유: HTML 메일 가독성을 높이기 위함이다.
     * 유지보수: 디자인 변경 시 HTML을 수정한다.
     */
    private String buildHtmlBody(String userId, String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html lang=\"ko\"><head><meta charset=\"UTF-8\">");
        sb.append("<style>");
        sb.append("body{font-family:'Noto Sans KR','Malgun Gothic',sans-serif;background:#f2f6fb;padding:24px;}");
        sb.append(".card{max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;");
        sb.append("padding:24px;border:1px solid #e6eef7;box-shadow:0 10px 30px rgba(7,24,56,0.08);}");
        sb.append(".title{font-size:18px;font-weight:700;color:#0b2c69;margin-bottom:8px;}");
        sb.append(".badge{display:inline-block;padding:6px 12px;border-radius:999px;background:#eaf4ff;color:#0f63e5;font-size:12px;}");
        sb.append(".code{font-size:22px;font-weight:700;letter-spacing:2px;color:#0b2c69;margin:16px 0;}");
        sb.append(".desc{font-size:13px;color:#5b6b7c;line-height:1.6;}");
        sb.append("</style></head><body>");
        sb.append("<div class=\"card\">");
        sb.append("<div class=\"badge\">MES 이메일 인증</div>");
        sb.append("<div class=\"title\">이메일 인증 코드 안내</div>");
        sb.append("<div class=\"desc\">아이디: ").append(escapeHtml(userId)).append("</div>");
        sb.append("<div class=\"code\">").append(escapeHtml(code)).append("</div>");
        sb.append("<div class=\"desc\">인증 화면에 코드를 입력해 주세요. 본 메일은 자동 발송되며 회신하지 마세요.</div>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    /**
     * 목적: 승인 결과 텍스트 본문을 생성한다.
     * 기능: 승인 여부에 맞는 안내 문구를 구성한다.
     * 이유: 텍스트 메일 수신자를 지원하기 위함이다.
     * 유지보수: 문구 변경 시 내용을 수정한다.
     */
    private String buildApprovalBody(String userId, boolean approved) {
        StringBuilder sb = new StringBuilder();
        sb.append("안녕하세요. MES 회원가입 승인 결과 안내입니다.\n\n");
        sb.append("아이디: ").append(userId).append("\n");
        if (approved) {
            sb.append("승인 결과: 승인 완료\n\n");
            sb.append("이제 로그인할 수 있습니다.\n");
        } else {
            sb.append("승인 결과: 보류\n\n");
            sb.append("승인 보류 사유는 관리자에게 문의해 주세요.\n");
        }
        sb.append("본 메일은 자동 발송되며, 회신하지 마세요.\n");
        return sb.toString();
    }

    /**
     * 목적: 승인 결과 HTML 본문을 생성한다.
     * 기능: 상태 배지와 안내 문구를 카드 형태로 구성한다.
     * 이유: HTML 메일 가독성을 높이기 위함이다.
     * 유지보수: 디자인 변경 시 HTML을 수정한다.
     */
    private String buildApprovalHtmlBody(String userId, boolean approved) {
        String statusText = approved ? "승인 완료" : "승인 보류";
        String guide = approved ? "이제 로그인할 수 있습니다." : "승인 보류 사유는 관리자에게 문의해 주세요.";
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html lang=\"ko\"><head><meta charset=\"UTF-8\">");
        sb.append("<style>");
        sb.append("body{font-family:'Noto Sans KR','Malgun Gothic',sans-serif;background:#f2f6fb;padding:24px;}");
        sb.append(".card{max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;");
        sb.append("padding:24px;border:1px solid #e6eef7;box-shadow:0 10px 30px rgba(7,24,56,0.08);}");
        sb.append(".title{font-size:18px;font-weight:700;color:#0b2c69;margin-bottom:8px;}");
        sb.append(".status{display:inline-block;padding:6px 12px;border-radius:999px;background:#eaf4ff;color:#0f63e5;font-size:12px;}");
        sb.append(".desc{font-size:13px;color:#5b6b7c;line-height:1.6;margin-top:12px;}");
        sb.append("</style></head><body>");
        sb.append("<div class=\"card\">");
        sb.append("<div class=\"status\">").append(escapeHtml(statusText)).append("</div>");
        sb.append("<div class=\"title\">회원가입 승인 결과</div>");
        sb.append("<div class=\"desc\">아이디: ").append(escapeHtml(userId)).append("</div>");
        sb.append("<div class=\"desc\">").append(escapeHtml(guide)).append("</div>");
        sb.append("<div class=\"desc\">본 메일은 자동 발송되며 회신하지 마세요.</div>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    /**
     * 목적: SMTP 메일을 재시도 포함해 발송한다.
     * 기능: 지정 횟수만큼 재시도 후 실패 시 큐에 적재한다.
     * 이유: 발송 실패를 복구하기 위함이다.
     * 유지보수: 재시도 정책 변경 시 로직을 수정한다.
     */
    private void sendWithRetry(String toEmail, String subject, String textBody, String htmlBody, boolean allowQueue) {
        String host = read("mes.mail.host", "MES_MAIL_HOST");
        String port = readOrDefault("mes.mail.port", "MES_MAIL_PORT", "587");
        String username = read("mes.mail.user", "MES_MAIL_USER");
        String password = read("mes.mail.pass", "MES_MAIL_PASS");
        String from = read("mes.mail.from", "MES_MAIL_FROM");
        boolean useTls = Boolean.parseBoolean(readOrDefault("mes.mail.tls", "MES_MAIL_TLS", "true"));
        boolean useSsl = Boolean.parseBoolean(readOrDefault("mes.mail.ssl", "MES_MAIL_SSL", "false"));
        boolean useHtml = Boolean.parseBoolean(readOrDefault("mes.mail.html.enabled", "MES_MAIL_HTML_ENABLED", "true"));
        int retryCount = parseInt(readOrDefault("mes.mail.retry.count", "MES_MAIL_RETRY_COUNT", "1"));
        int retryDelay = parseInt(readOrDefault("mes.mail.retry.delay.ms", "MES_MAIL_RETRY_DELAY_MS", "500"));
        int queueMaxAttempts = parseInt(readOrDefault("mes.mail.retry.queue.attempts", "MES_MAIL_RETRY_QUEUE_ATTEMPTS", "3"));

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", username != null ? "true" : "false");
        props.put("mail.smtp.starttls.enable", String.valueOf(useTls));
        props.put("mail.smtp.ssl.enable", String.valueOf(useSsl));

        Session session = buildSession(props, username, password);
        int attempts = Math.max(1, retryCount);
        for (int i = 1; i <= attempts; i++) {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                if (useHtml && htmlBody != null && !htmlBody.trim().isEmpty()) {
                    MimeBodyPart textPart = new MimeBodyPart();
                    textPart.setText(textBody == null ? "" : textBody, "UTF-8");
                    MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
                    MimeMultipart multipart = new MimeMultipart("alternative");
                    multipart.addBodyPart(textPart);
                    multipart.addBodyPart(htmlPart);
                    message.setContent(multipart);
                } else {
                    message.setText(textBody == null ? "" : textBody);
                }
                Transport.send(message);
                LOGGER.info("이메일 발송 완료: {}", maskEmail(toEmail));
                return;
            } catch (Exception ex) {
                LOGGER.warn("이메일 발송 실패({}/{}): {}", i, attempts, ex.getMessage());
                if (i == attempts) {
                    if (allowQueue && retryQueue != null && retryQueue.isEnabled()) {
                        retryQueue.enqueue(new EmailRetryItem(toEmail, subject, textBody, htmlBody, queueMaxAttempts));
                    }
                    throw new IllegalStateException("이메일 발송 실패", ex);
                }
                sleep(retryDelay);
            }
        }
    }

    /**
     * 목적: 설정 값을 읽는다.
     * 기능: 시스템 속성/환경 변수 순으로 조회한다.
     * 이유: 다양한 배포 환경을 지원하기 위함이다.
     * 유지보수: 설정 키 변경 시 수정한다.
     */
    private String read(String sysKey, String envKey) {
        String value = System.getProperty(sysKey);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(envKey);
        }
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    /**
     * 목적: 기본값 포함 설정을 조회한다.
     * 기능: 값이 없으면 기본값을 반환한다.
     * 이유: 설정 누락 시 기본 동작을 보장하기 위함이다.
     * 유지보수: 기본값 정책 변경 시 수정한다.
     */
    private String readOrDefault(String sysKey, String envKey, String fallback) {
        String value = read(sysKey, envKey);
        return value == null ? fallback : value;
    }

    /**
     * 목적: 문자열을 정수로 변환한다.
     * 기능: 변환 실패 시 1을 반환한다.
     * 이유: 잘못된 설정으로 인한 오류를 방지하기 위함이다.
     * 유지보수: 기본값 정책 변경 시 수정한다.
     */
    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    /**
     * 목적: HTML 특수문자를 이스케이프한다.
     * 기능: 텍스트를 안전한 HTML 문자열로 변환한다.
     * 이유: HTML 본문에서 스크립트 삽입을 방지하기 위함이다.
     * 유지보수: 이스케이프 규칙 변경 시 수정한다.
     */
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * 목적: 재시도 대기 시간을 적용한다.
     * 기능: 지정된 밀리초만큼 대기한다.
     * 이유: SMTP 서버 부하를 완화하기 위함이다.
     * 유지보수: 대기 정책 변경 시 수정한다.
     */
    private void sleep(int delayMs) {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 목적: 이메일 주소를 마스킹한다.
     * 기능: 로그에 개인정보가 노출되지 않도록 가공한다.
     * 이유: 개인정보 보호를 위해서다.
     * 유지보수: 마스킹 규칙 변경 시 수정한다.
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "unknown";
        }
        String[] parts = email.split("@");
        String local = parts[0];
        if (local.length() <= 2) {
            return "**@" + parts[1];
        }
        return local.substring(0, 2) + "***@" + parts[1];
    }
}
