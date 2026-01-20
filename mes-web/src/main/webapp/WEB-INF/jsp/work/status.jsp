<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 작업 현황 화면을 제공한다. 기능: 작업 상태 그리드 영역을 표시한다. 이유: UI/UX 설계 기준을 반영하기 위함이다. 유지보수: 화면 상세 확정 시 이 파일을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>작업 현황</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>작업 현황</h2>
    <p>작업 상태 그리드</p>
    <div class="form-section">
      <form id="workStatusForm">
        <label>작업번호</label>
        <input type="text" name="workNo" placeholder="WORK-0001" />
        <label>상태</label>
        <input type="text" name="status" placeholder="planned" />
      </form>
      <div class="form-actions">
        <button type="button" onclick="MesWeb.post('/api/work/list', 'workStatusForm', 'workStatusResult')">조회</button>
      </div>
      <pre id="workStatusResult" class="result-box"></pre>
    </div>
  <%@ include file="/WEB-INF/jsp/common/grid.jsp" %>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
