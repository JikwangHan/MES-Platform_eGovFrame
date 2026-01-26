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
        <label>조회 시작일(YYYY-MM-DD)</label>
        <input type="text" name="fromDate" placeholder="2026-01-01" />
        <label>조회 종료일(YYYY-MM-DD)</label>
        <input type="text" name="toDate" placeholder="2026-01-31" />
        <label>계획 수량</label>
        <input type="text" name="planQty" placeholder="10" />
        <label>상태</label>
        <input type="text" name="status" placeholder="planned" />
        <label>담당자 ID</label>
        <input type="text" name="ownerId" placeholder="1" />
        <label>페이지</label>
        <input type="text" name="page" placeholder="1" />
        <label>페이지 크기</label>
        <input type="text" name="pageSize" placeholder="50" />
        <label>요청자 ID(감사로그용)</label>
        <input type="text" name="userId" placeholder="admin" />
      </form>
      <c:set var="crudFormId" value="workForm" />
      <c:set var="crudResultId" value="workResult" />
      <c:set var="crudListUrl" value="/api/work/list" />
      <c:set var="crudCreateUrl" value="/api/work/create" />
      <c:set var="crudUpdateUrl" value="/api/work/update" />
      <c:set var="crudDeleteUrl" value="/api/work/delete" />
      <c:set var="crudExtraUrl" value="/api/work/status" />
      <c:set var="crudExtraLabel" value="상태변경" />
      <c:set var="crudListPerm" value="ACTION_WORK_LIST" />
      <c:set var="crudCreatePerm" value="ACTION_WORK_CREATE" />
      <c:set var="crudUpdatePerm" value="ACTION_WORK_UPDATE" />
      <c:set var="crudDeletePerm" value="ACTION_WORK_DELETE" />
      <c:set var="crudExtraPerm" value="ACTION_WORK_STATUS" />
      <jsp:include page="/WEB-INF/jsp/common/crud_buttons.jsp" />
      <pre id="workResult" class="result-box"></pre>
    </div>
  <%@ include file="/WEB-INF/jsp/common/grid.jsp" %>
  <%@ include file="/WEB-INF/jsp/common/modal.jsp" %>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
