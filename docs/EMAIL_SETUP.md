# MES Web Service 이메일 발송 설정 안내

## 목적
- 회원가입 이메일 인증 발송을 위해 SMTP 정보를 환경 변수 또는 시스템 속성으로 설정합니다.

## 설정 값(환경 변수 또는 시스템 속성)
- `MES_MAIL_HOST` 또는 `-Dmes.mail.host`
- `MES_MAIL_PORT` 또는 `-Dmes.mail.port` (기본 587)
- `MES_MAIL_USER` 또는 `-Dmes.mail.user` (선택)
- `MES_MAIL_PASS` 또는 `-Dmes.mail.pass` (선택)
- `MES_MAIL_FROM` 또는 `-Dmes.mail.from` (필수)
- `MES_MAIL_TLS` 또는 `-Dmes.mail.tls` (기본 true)
- `MES_MAIL_SSL` 또는 `-Dmes.mail.ssl` (기본 false)

## 승인/인증 흐름 제어
- 관리자 승인 사용: `-Dmes.signup.requireApproval=true`
- 이메일 입력 시 자동으로 인증 대기 상태로 전환됨

## 주의
- 비밀번호/토큰은 절대 소스코드, 문서, 로그에 기록하지 않습니다.
- 운영 환경에서는 반드시 안전한 보관소(환경 변수/비밀 관리 도구)를 사용합니다.
