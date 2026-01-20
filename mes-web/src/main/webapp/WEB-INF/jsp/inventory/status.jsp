<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 재고 현황 화면을 제공한다. 기능: 재고 상태 그리드 영역을 표시한다. 이유: UI/UX 설계 기준을 반영하기 위함이다. 유지보수: 화면 상세 확정 시 이 파일을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>재고 현황</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>재고 현황</h2>
    <p>재고 상태 그리드</p>
    <div class="form-section">
      <form id="inventoryForm">
        <label>재고 ID(수정/삭제용)</label>
        <input type="text" name="id" placeholder="1" />
        <label>품목 ID</label>
        <input type="text" name="itemId" placeholder="1" />
        <label>창고 ID</label>
        <input type="text" name="warehouseId" placeholder="1" />
        <label>재고 수량</label>
        <input type="text" name="stockQty" placeholder="100" />
        <label>재고 유형</label>
        <input type="text" name="stockType" placeholder="normal" />
        <label>입고일자(YYYY-MM-DD)</label>
        <input type="text" name="lastInDate" placeholder="2026-01-01" />
        <label>출고일자(YYYY-MM-DD)</label>
        <input type="text" name="lastOutDate" placeholder="2026-01-02" />
        <label>품목 코드(조회용)</label>
        <input type="text" name="itemCode" placeholder="ITEM-0001" />
        <label>요청자 ID(감사로그용)</label>
        <input type="text" name="userId" placeholder="admin" />
      </form>
      <div class="form-actions">
        <button type="button" onclick="MesWeb.post('/api/inventory/list', 'inventoryForm', 'inventoryResult')">조회</button>
        <button type="button" onclick="MesWeb.post('/api/inventory/create', 'inventoryForm', 'inventoryResult')">등록</button>
        <button type="button" onclick="MesWeb.post('/api/inventory/update', 'inventoryForm', 'inventoryResult')">수정</button>
        <button type="button" onclick="MesWeb.post('/api/inventory/delete', 'inventoryForm', 'inventoryResult')">삭제</button>
      </div>
      <pre id="inventoryResult" class="result-box"></pre>
    </div>
  <%@ include file="/WEB-INF/jsp/common/grid.jsp" %>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
