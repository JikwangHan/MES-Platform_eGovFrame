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
