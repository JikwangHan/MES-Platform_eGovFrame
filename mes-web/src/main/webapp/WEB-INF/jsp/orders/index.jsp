<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 수주 내역 화면을 제공한다. 기능: 수주 CRUD 그리드 영역을 표시한다. 이유: UI/UX 설계 기준을 반영하기 위함이다. 유지보수: 화면 상세 확정 시 이 파일을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>수주 내역</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>수주 내역</h2>
    <p>수주 CRUD 그리드</p>
    <div class="form-section">
      <form id="orderForm">
        <label>수주번호</label>
        <input type="text" name="orderNo" placeholder="ORDER-0001" />
        <label>수주일자(YYYY-MM-DD)</label>
        <input type="text" name="orderDate" placeholder="2026-01-01" />
        <label>조회 시작일(YYYY-MM-DD)</label>
        <input type="text" name="fromDate" placeholder="2026-01-01" />
        <label>조회 종료일(YYYY-MM-DD)</label>
        <input type="text" name="toDate" placeholder="2026-01-31" />
        <label>납기일자(YYYY-MM-DD)</label>
        <input type="text" name="dueDate" placeholder="2026-01-07" />
        <label>품목 ID</label>
        <input type="text" name="itemId" placeholder="1" />
        <label>수주수량</label>
        <input type="text" name="orderQty" placeholder="10" />
        <label>상태</label>
        <input type="text" name="status" placeholder="planned" />
        <label>거래처 ID</label>
        <input type="text" name="partnerId" placeholder="1" />
        <label>담당자 ID</label>
        <input type="text" name="ownerId" placeholder="1" />
        <label>페이지</label>
        <input type="text" name="page" placeholder="1" />
        <label>페이지 크기</label>
        <input type="text" name="pageSize" placeholder="50" />
        <label>요청자 ID(감사로그용)</label>
        <input type="text" name="userId" placeholder="admin" />
      </form>
      <c:set var="crudFormId" value="orderForm" />
      <c:set var="crudResultId" value="orderResult" />
      <c:set var="crudListUrl" value="/api/orders/list" />
      <c:set var="crudCreateUrl" value="/api/orders/create" />
      <c:set var="crudUpdateUrl" value="/api/orders/update" />
      <c:set var="crudDeleteUrl" value="/api/orders/delete" />
      <c:set var="crudListPerm" value="ACTION_ORDER_LIST" />
      <c:set var="crudCreatePerm" value="ACTION_ORDER_CREATE" />
      <c:set var="crudUpdatePerm" value="ACTION_ORDER_UPDATE" />
      <c:set var="crudDeletePerm" value="ACTION_ORDER_DELETE" />
      <jsp:include page="/WEB-INF/jsp/common/crud_buttons.jsp" />
      <pre id="orderResult" class="result-box"></pre>
    </div>
  <%@ include file="/WEB-INF/jsp/common/grid.jsp" %>
  <%@ include file="/WEB-INF/jsp/common/modal.jsp" %>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
