<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 생산 현황판 화면을 제공한다. 기능: KPI/차트 요약 영역을 표시한다. 이유: UI/UX 설계 기준을 반영하기 위함이다. 유지보수: 화면 상세 확정 시 이 파일을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>생산 현황판</title>
</head>
<body class="main-body">
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <%-- 목적: 메인 히어로 영역을 제공한다. 기능: 대시보드 핵심 문구를 강조한다. 이유: 로그인 톤과 일관된 첫 화면을 만들기 위함이다. 유지보수: 문구/지표 변경 시 수정한다. --%>
  <section class="main-hero">
    <div class="main-hero-inner">
      <h1>MES</h1>
      <h2>Manufacturing Execution Systems</h2>
      <p>MES는 제조 프로세스의 품질과 효율성을 개선하는 체계적인 제조 실행 소프트웨어 솔루션입니다.</p>
      <div class="hero-badges">
        <span>실시간 모니터링</span>
        <span>공정/품질/재고 통합</span>
        <span>멀티테넌트 지원</span>
      </div>
    </div>
  </section>

  <div class="content main-content">
    <div class="kpi-grid">
      <div class="kpi-card">
        <h3>오늘 생산</h3>
        <strong>1,240</strong>
        <span>전일 대비 +4.2%</span>
      </div>
      <div class="kpi-card">
        <h3>진행 작업</h3>
        <strong>18</strong>
        <span>진행중 12 / 대기 6</span>
      </div>
      <div class="kpi-card">
        <h3>불량률</h3>
        <strong>1.8%</strong>
        <span>목표 2.0% 이하</span>
      </div>
      <div class="kpi-card">
        <h3>설비 가동</h3>
        <strong>92%</strong>
        <span>정상 24 / 점검 2</span>
      </div>
    </div>

    <div class="panel-grid">
      <div class="panel-card">
        <h3>작업 현황</h3>
        <p>작업 지시/상태 변경 흐름을 한눈에 확인합니다.</p>
        <a class="panel-link" href="${pageContext.request.contextPath}/work/status">작업 현황 보기</a>
      </div>
      <div class="panel-card">
        <h3>수주/납품</h3>
        <p>수주 진행, 납품 일정, 반품 현황을 빠르게 탐색합니다.</p>
        <a class="panel-link" href="${pageContext.request.contextPath}/orders/summary">수주 요약 보기</a>
      </div>
      <div class="panel-card">
        <h3>재고/자재</h3>
        <p>재고 수준과 입출고 이력을 통합적으로 관리합니다.</p>
        <a class="panel-link" href="${pageContext.request.contextPath}/inventory/status">재고 현황 보기</a>
      </div>
      <div class="panel-card">
        <h3>품질/설비</h3>
        <p>불량 내역과 설비 상태를 함께 모니터링합니다.</p>
        <a class="panel-link" href="${pageContext.request.contextPath}/quality/defects/status">품질 현황 보기</a>
      </div>
    </div>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
