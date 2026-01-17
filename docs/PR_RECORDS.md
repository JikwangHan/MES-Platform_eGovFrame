# PR 기록(정책 보완용)

이 문서는 master에 직접 반영된 변경이 있을 때, PR 정책 준수를 위해
기록용 PR을 생성하기 위한 최소 변경을 남깁니다.

## PR-05 기록
- 변경 요약: 정규화 키 별칭 유틸 추가, JSON/CSV/TSV 정규화 보강, 스키마 미등록 사유 상세화, 스모크 실패 케이스 추가
- 변경 이유: 포맷별 키 차이로 인한 검증 실패를 줄이고 테스트 가시성을 높이기 위함
- 검증 방법(명령어):
  - java -Dfile.encoding=UTF-8 -Dai.security.scan.mockClean=true -Dai.http.port=18080 -cp "ai-middleware/target/classes;$(Get-Content ai-middleware/target/classpath.txt)" com.mes.ai.tools.HttpIngressSmokeRunner
- 결과(PASS 근거):
  - 성공 케이스 응답 코드 202
  - 표준 저장 건수 1
  - 실패 케이스 격리 건수 1

## PR-06 기록
- 변경 요약: KCMVP 문서 정합성 보완(v0.2 정리), 모듈 후보/운영 환경 체크리스트 템플릿 추가, 스모크 증빙 템플릿 및 실행 기록 추가, PR 증빙 패키지 문서 추가
- 변경 이유: 문서 기준을 고정하고 운영 검증/증빙을 한 번에 처리하기 위함
- 검증 방법(명령어):
  - mvn -f ai-middleware/pom.xml -DskipTests compile
  - mvn -f ai-middleware/pom.xml -DincludeScope=runtime dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
  - java -Dfile.encoding=UTF-8 -Dai.security.scan.mockClean=true -Dai.http.port=18080 -cp "ai-middleware/target/classes;$(Get-Content ai-middleware/target/classpath.txt)" com.mes.ai.tools.HttpIngressSmokeRunner
- 결과(PASS 근거):
  - 성공/별칭/장비/장비-2/경고 케이스 응답 코드 202
  - 표준 저장 증가: 성공/별칭/장비/장비-2/경고 각 1
  - 격리 증가: 버전 형식 오류 1, 실패 1
  - Unknown Ingest 증가: 경고 1

## PR-07 기록
- 변경 요약: 운영 환경 호환성 정보 반영, 공급사 요청 체크리스트/템플릿 추가, PR 증빙 체크리스트 보강
- 변경 이유: 운영 검증 자료 수집을 한 번에 처리하기 위함
- 검증 방법: 문서 변경(코드 실행 없음)
- 결과: 운영 검증과 공급사 요청 흐름이 단일 문서에서 수행 가능

## PR-08 기록
- 변경 요약: 개발/PoC 단계 무료 오픈소스 사용 확정 및 KCMVP 전환 원칙 문서 반영
- 변경 이유: 비용 발생 없는 개발 원칙을 기준 문서로 고정하기 위함
- 검증 방법: 문서 변경(코드 실행 없음)
- 결과: 후보 모듈/해시/서명은 상용화 전환 시점 수집으로 정리됨

## PR-09 기록
- 변경 요약: FreeCryptoProviderImpl 적용 범위(대상/기능/제한) 문서화
- 변경 이유: 무료 오픈소스 암호 적용 범위를 명확히 하기 위함
- 검증 방법: 문서 변경(코드 실행 없음)
- 결과: CryptoService 경계와 AEAD/nonce/키 래핑 범위가 문서로 고정됨

## PR-10 기록
- 변경 요약: PR-07~09 요약/이유/검증/결과를 PR 기록 문서에 반영
- 변경 이유: PR 정책 준수를 위해 기록 문서를 최신 상태로 유지
- 검증 방법: 문서 변경(코드 실행 없음)
- 결과: PR 기록이 최신 상태로 유지됨

## PR-11 기록
- 변경 요약: KEY_MGMT_SOP/MIGRATION_PLAN에 무료 오픈소스 적용 기준 명시
- 변경 이유: 외부 입력 없이 무료 오픈소스 적용 원칙을 문서 기준으로 고정
- 검증 방법: 문서 변경(코드 실행 없음)
- 결과: 개발 단계 암호 구현 기준이 문서에 고정됨

## PR-12 기록
- 변경 요약: 문서 작업 최종 마무리 보고 문서 추가
- 변경 이유: 완료 항목/현재 상태/다음 단계 전환을 한 번에 확인하기 위함
- 검증 방법: 문서 변경(코드 실행 없음)
- 결과: 완료 항목/현재 상태/다음 단계가 문서로 고정됨

## PR-13 기록
- 변경 요약: 운영 적용 점검 패키지 문서 추가 및 증빙 체크리스트 연결
- 변경 이유: 운영 전 점검과 증빙을 한 번에 마무리하기 위함
- 검증 방법: 문서 변경(코드 실행 없음)
- 결과: 적용→검증→증빙 흐름이 단일 문서로 정리됨

## PR-14 기록
- 변경 요약: 운영 점검 미실행 상태 및 예정 환경 반영
- 변경 이유: 테스트 미수행 상태를 문서로 명확히 고정하기 위함
- 검증 방법: 문서 변경(코드 실행 없음)
- 결과: 운영 점검은 미실행으로 기록되고 예정 환경 기준이 반영됨

## PR-15 기록
- 변경 요약: CryptoService/FreeCryptoProviderImpl/NoopCryptoService 추가 및 저장 구간 암호화 적용
- 변경 이유: 무료 오픈소스 기반 암호화를 저장 구간에 적용하고 경계를 고정하기 위함
- 검증 방법: 문서/코드 변경(코드 실행 없음)
- 결과: 저장 구간 암호화 경계와 컨테이너 포맷이 코드로 적용됨

## PR-16 기록
- 변경 요약: CryptoService 스모크 실행기 추가 및 운영 점검/스모크 증빙 반영
- 변경 이유: 암호화 적용 상태를 실제 실행 결과로 증빙하기 위함
- 검증 방법(명령어):
  - mvn -f ai-middleware/pom.xml -DskipTests compile
  - mvn -f ai-middleware/pom.xml -DincludeScope=runtime dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
  - java -Dfile.encoding=UTF-8 -Dai.crypto.enabled=true -Dai.crypto.key.base64=<임시 키> -Dai.crypto.key.id=dev-key -Dai.crypto.key.version=v1 -cp "ai-middleware/target/classes;$(Get-Content ai-middleware/target/classpath.txt)" com.mes.ai.tools.CryptoServiceSmokeRunner
  - java -Dfile.encoding=UTF-8 -Dai.security.scan.mockClean=true -Dai.crypto.enabled=true -Dai.crypto.key.base64=<임시 키> -Dai.crypto.key.id=dev-key -Dai.crypto.key.version=v1 -Dai.http.port=18080 -cp "ai-middleware/target/classes;$(Get-Content ai-middleware/target/classpath.txt)" com.mes.ai.tools.HttpIngressSmokeRunner
- 결과(PASS 근거):
  - CryptoService 스모크 PASS(Nonce 차이/암호문/태그 존재)
  - HTTP Ingress 스모크 PASS(정상/경고/실패 케이스 결과 확인)

## PR-17 기록
- 변경 요약: PR-10~16 기록 및 PR-16 증빙 요약 반영
- 변경 이유: 최신 작업 증빙을 문서에 일괄 반영해 마무리 상태를 고정하기 위함
- 검증 방법: 문서 변경(코드 실행 없음)
- 결과: PR 기록과 증빙 요약이 최신 상태로 정리됨

## PR-18 기록
- 변경 요약: CryptoService 설정 가이드 및 암호화 스모크 스크립트 추가
- 변경 이유: 운영 전 점검을 한 번에 실행할 수 있도록 안내와 자동 실행 제공
- 검증 방법: 문서/스크립트 변경(코드 실행 없음)
- 결과: 암호화 스모크 실행 절차가 문서/스크립트로 고정됨
