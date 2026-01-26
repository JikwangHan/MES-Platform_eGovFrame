<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 설비 등록 화면을 제공한다. 기능: 설비 등록 CRUD 영역을 표시한다. 이유: UI/UX 설계 기준을 반영하기 위함이다. 유지보수: 화면 상세 확정 시 이 파일을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>설비 등록</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>설비 등록</h2>
    <p>설비 등록 CRUD</p>
    <div class="form-section">
      <form id="equipmentForm">
        <label>설비 코드</label>
        <input type="text" name="equipmentCode" placeholder="EQ-0001" />
        <label>설비명</label>
        <input type="text" name="equipmentName" placeholder="샘플 설비" />
        <label>설비 유형</label>
        <input type="text" name="equipmentType" placeholder="PRESS" />
        <label>상태</label>
        <input type="text" name="status" placeholder="idle" />
        <label>IP 주소</label>
        <input type="text" name="ipAddress" placeholder="192.168.0.10" />
        <label>포트</label>
        <input type="text" name="portNumber" placeholder="502" />
        <label>COM 포트</label>
        <input type="text" name="comPort" placeholder="COM3" />
        <label>통신 속도</label>
        <input type="text" name="baudRate" placeholder="9600" />
        <label>폴링 주기(ms)</label>
        <input type="text" name="pollingInterval" placeholder="1000" />
        <label>페이지</label>
        <input type="text" name="page" placeholder="1" />
        <label>페이지 크기</label>
        <input type="text" name="pageSize" placeholder="50" />
        <label>요청자 ID(감사로그용)</label>
        <input type="text" name="userId" placeholder="admin" />
      </form>
      <c:set var="crudFormId" value="equipmentForm" />
      <c:set var="crudResultId" value="equipmentResult" />
      <c:set var="crudListUrl" value="/api/equipment/list" />
      <c:set var="crudCreateUrl" value="/api/equipment/create" />
      <c:set var="crudUpdateUrl" value="/api/equipment/update" />
      <c:set var="crudDeleteUrl" value="/api/equipment/delete" />
      <c:set var="crudListPerm" value="ACTION_EQUIPMENT_LIST" />
      <c:set var="crudCreatePerm" value="ACTION_EQUIPMENT_CREATE" />
      <c:set var="crudUpdatePerm" value="ACTION_EQUIPMENT_UPDATE" />
      <c:set var="crudDeletePerm" value="ACTION_EQUIPMENT_DELETE" />
      <jsp:include page="/WEB-INF/jsp/common/crud_buttons.jsp" />
      <pre id="equipmentResult" class="result-box"></pre>
    </div>
  <%@ include file="/WEB-INF/jsp/common/grid.jsp" %>
  <%@ include file="/WEB-INF/jsp/common/modal.jsp" %>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
