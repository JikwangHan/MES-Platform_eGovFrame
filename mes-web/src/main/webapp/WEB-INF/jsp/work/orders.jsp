<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 작업 관리 화면을 제공한다. 기능: 작업 관리 CRUD 영역을 표시한다. 이유: UI/UX 설계 기준을 반영하기 위함이다. 유지보수: 화면 상세 확정 시 이 파일을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>작업 관리</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>작업 관리</h2>
    <p>작업 관리 CRUD</p>
    <div class="form-section">
      <form id="workForm">
        <label>작업번호</label>
        <input type="text" name="workNo" placeholder="WORK-0001" />
        <label>수주 ID</label>
        <input type="text" name="orderId" placeholder="1" />
        <label>계획 시작일(YYYY-MM-DD)</label>
        <input type="text" name="planStartDate" placeholder="2026-01-01" />
        <label>계획 종료일(YYYY-MM-DD)</label>
        <input type="text" name="planEndDate" placeholder="2026-01-05" />
        <label>계획 수량</label>
        <input type="text" name="planQty" placeholder="10" />
        <label>상태</label>
        <input type="text" name="status" placeholder="planned" />
        <label>담당자 ID</label>
        <input type="text" name="ownerId" placeholder="1" />
        <label>요청자 ID(감사로그용)</label>
        <input type="text" name="userId" placeholder="admin" />
      </form>
      <div class="form-actions">
        <button type="button" onclick="MesWeb.post('/api/work/list', 'workForm', 'workResult')">조회</button>
        <button type="button" onclick="MesWeb.post('/api/work/create', 'workForm', 'workResult')">등록</button>
        <button type="button" onclick="MesWeb.post('/api/work/update', 'workForm', 'workResult')">수정</button>
        <button type="button" onclick="MesWeb.post('/api/work/status', 'workForm', 'workResult')">상태변경</button>
        <button type="button" onclick="MesWeb.post('/api/work/delete', 'workForm', 'workResult')">삭제</button>
      </div>
      <pre id="workResult" class="result-box"></pre>
    </div>
  <%@ include file="/WEB-INF/jsp/common/grid.jsp" %>
  <%@ include file="/WEB-INF/jsp/common/modal.jsp" %>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
