package com.mes.web.common.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        String host = read("mes.mail.host", "MES_MAIL_HOST");
        String port = readOrDefault("mes.mail.port", "MES_MAIL_PORT", "587");
        String username = read("mes.mail.user", "MES_MAIL_USER");
        String password = read("mes.mail.pass", "MES_MAIL_PASS");
        String from = read("mes.mail.from", "MES_MAIL_FROM");
        boolean useTls = Boolean.parseBoolean(readOrDefault("mes.mail.tls", "MES_MAIL_TLS", "true"));
        boolean useSsl = Boolean.parseBoolean(readOrDefault("mes.mail.ssl", "MES_MAIL_SSL", "false"));

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", username != null ? "true" : "false");
        props.put("mail.smtp.starttls.enable", String.valueOf(useTls));
        props.put("mail.smtp.ssl.enable", String.valueOf(useSsl));

        Session session = buildSession(props, username, password);
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[MES] 이메일 인증 코드 안내");
            message.setText(buildBody(userId, code));
            Transport.send(message);
            LOGGER.info("이메일 인증 발송 완료: {}", maskEmail(toEmail));
        } catch (Exception ex) {
            LOGGER.warn("이메일 발송 실패: {}", ex.getMessage());
            throw new IllegalStateException("이메일 발송 실패", ex);
        }
    }

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

    private String buildBody(String userId, String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("안녕하세요. MES 회원가입 이메일 인증 안내입니다.\n\n");
        sb.append("아이디: ").append(userId).append("\n");
        sb.append("인증 코드: ").append(code).append("\n\n");
        sb.append("해당 코드를 인증 화면에 입력해 주세요.\n");
        sb.append("본 메일은 자동 발송되며, 회신하지 마세요.\n");
        return sb.toString();
    }

    private String read(String sysKey, String envKey) {
        String value = System.getProperty(sysKey);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(envKey);
        }
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String readOrDefault(String sysKey, String envKey, String fallback) {
        String value = read(sysKey, envKey);
        return value == null ? fallback : value;
    }

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
