# 테스트 리포트 (2026-01-27) - 로컬 가상 SMTP 발송 테스트

## 1) 로컬 가상 SMTP 서버 실행
- 도구: smtp4dev (Desktop)
- 실행 파일: tools/smtp4dev/Rnwood.Smtp4dev.Desktop.exe
- 확인: TCP 127.0.0.1:25 포트 접속 성공

## 2) 텍스트 메일 발송 테스트
- 명령: Send-MailMessage (localhost:25)
- 제목: [MES] SMTP 가상 테스트
- 결과: 발송 성공 (연결 확인 완료)

## 3) HTML 메일 발송 테스트
- 명령: Send-MailMessage -BodyAsHtml (localhost:25)
- 제목: [MES] SMTP HTML 테스트
- 결과: 발송 성공 (연결 확인 완료)

## 비고
- Send-MailMessage는 PowerShell에서 폐기 예정 경고가 있으나 로컬 테스트 용도로 사용함.
- 실제 운영 SMTP 테스트는 운영 계정/서버 정보가 필요함.
