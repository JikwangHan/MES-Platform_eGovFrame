# AI Middleware PR 증빙 패키지 체크리스트

## 목적
- PR 제출 시 필수 3종 세트(코드+스모크/테스트+문서)를 빠르게 준비합니다.
- 초보자도 빠뜨리지 않도록 최소 체크리스트로 제공합니다.

## 1) 코드 변경 증빙
- 변경 파일 목록 정리
- 변경 요약(무엇/왜/영향 범위)
- 위험 요인/회귀 가능성 메모

## 2) 스모크/테스트 증빙
- 스모크 테스트 실행 기록: `docs/AI_MIDDLEWARE_SMOKE.md` 템플릿 사용
- 결과 요약(PASS/FAIL 근거 수치)
- 실패 시 재현 절차/원인 요약

## 3) 문서 증빙
- 정책/설계/운영 문서 갱신 여부 확인
- 변경된 문서 목록 정리
- 핵심 변경점 3줄 요약

## 제출 전 최종 확인
- 민감정보(로컬 경로/토큰/계정) 기록 없음
- PR 정책 준수(코드+스모크/테스트+문서)
- 변경 범위에 맞는 파일만 add
- 공급사 자료 요청 여부 확인(배포물/해시/서명/인증서)
- 운영 적용 점검 패키지 기록 확인: `docs/AI_MIDDLEWARE_OPERATIONAL_VERIFICATION_PACK.md`
- 운영 적용 점검은 현재 미실행 상태로 기록됨

## PR-06 증빙 요약(2026-01-17)
### 코드 변경 증빙
- 변경 파일:
  - docs/AI_MIDDLEWARE_KCMVP_SCOPE.md
  - docs/AI_MIDDLEWARE_KCMVP_KEY_POLICY.md
  - docs/AI_MIDDLEWARE_KCMVP_ENCRYPTION_POLICY.md
  - docs/AI_MIDDLEWARE_KCMVP_MODULE_CHECKLIST.md
  - docs/AI_MIDDLEWARE_SMOKE.md
  - docs/AI_MIDDLEWARE_EVIDENCE_PACK.md
  - docs/PR_RECORDS.md
- 변경 요약: KCMVP 문서 정합성 기준 확정, 모듈 후보 체크리스트 템플릿/스모크 증빙 템플릿/PR 증빙 문서 보강
- 영향 범위: 문서/정책/증빙 가이드(코드 로직 변경 없음)

### 스모크/테스트 증빙
- 스모크 기록 위치: `docs/AI_MIDDLEWARE_SMOKE.md`
- 결과 요약: 성공/별칭/장비/장비-2/경고 케이스 202, 버전 형식 오류/실패 격리 1, Unknown Ingest 1

### 문서 증빙
- 변경 문서 목록: 위 “변경 파일”과 동일
- 핵심 변경점 3줄 요약:
  - KCMVP 관련 문서 간 기준/용어를 상호 참조로 고정
  - 운영 검증을 위한 체크리스트/템플릿을 추가
  - 스모크 실행 결과를 문서 증빙으로 남길 수 있게 템플릿 보강
