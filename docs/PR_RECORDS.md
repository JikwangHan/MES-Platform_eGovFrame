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
