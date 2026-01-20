<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 불량 내역 화면을 제공한다. 기능: 불량 내역 그리드 영역을 표시한다. 이유: UI/UX 설계 기준을 반영하기 위함이다. 유지보수: 화면 상세 확정 시 이 파일을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>불량 내역</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>불량 내역</h2>
    <p>불량 내역 그리드</p>
    <div class="form-section">
      <form id="defectForm">
        <label>불량 ID(삭제용)</label>
        <input type="text" name="id" placeholder="1" />
        <label>불량일자(YYYY-MM-DD)</label>
        <input type="text" name="defectDate" placeholder="2026-01-01" />
        <label>품목 ID</label>
        <input type="text" name="itemId" placeholder="1" />
        <label>공정 ID</label>
        <input type="text" name="processId" placeholder="1" />
        <label>설비 ID</label>
        <input type="text" name="equipmentId" placeholder="1" />
        <label>불량 유형 ID</label>
        <input type="text" name="defectTypeId" placeholder="1" />
        <label>불량 수량</label>
        <input type="text" name="defectQty" placeholder="2" />
        <label>요청자 ID(감사로그용)</label>
        <input type="text" name="userId" placeholder="admin" />
      </form>
      <div class="form-actions">
        <button type="button" onclick="MesWeb.post('/api/quality/defects/list', 'defectForm', 'defectResult')">조회</button>
        <button type="button" onclick="MesWeb.post('/api/quality/defects/create', 'defectForm', 'defectResult')">등록</button>
        <button type="button" onclick="MesWeb.post('/api/quality/defects/delete', 'defectForm', 'defectResult')">삭제</button>
      </div>
      <pre id="defectResult" class="result-box"></pre>
    </div>
  <%@ include file="/WEB-INF/jsp/common/grid.jsp" %>
  <%@ include file="/WEB-INF/jsp/common/modal.jsp" %>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
