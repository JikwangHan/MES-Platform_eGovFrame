<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 공통 상단 영역을 제공한다. 기능: 로고/메뉴/로그아웃 링크를 표시한다. 이유: 모든 화면의 일관된 네비게이션을 제공하기 위함이다. 유지보수: 메뉴 구조 변경 시 여기만 수정한다. --%>
<div class="header">
  <div class="logo">MES Platform</div>
  <div class="menu">
    <c:if test="${sessionScope.PERMISSIONS['MENU_DASHBOARD']}">
      <a href="${pageContext.request.contextPath}/dashboard/production">대시보드</a>
    </c:if>
    <c:if test="${sessionScope.PERMISSIONS['MENU_ORDERS']}">
      <a href="${pageContext.request.contextPath}/orders">수주</a>
    </c:if>
    <c:if test="${sessionScope.PERMISSIONS['MENU_WORK']}">
      <a href="${pageContext.request.contextPath}/work/orders">작업</a>
    </c:if>
    <c:if test="${sessionScope.PERMISSIONS['MENU_INVENTORY']}">
      <a href="${pageContext.request.contextPath}/inventory/status">재고</a>
    </c:if>
    <c:if test="${sessionScope.PERMISSIONS['MENU_QUALITY']}">
      <a href="${pageContext.request.contextPath}/quality/defects/status">품질</a>
    </c:if>
    <c:if test="${sessionScope.PERMISSIONS['MENU_EQUIPMENT']}">
      <a href="${pageContext.request.contextPath}/equipment/status">설비</a>
    </c:if>
    <c:if test="${sessionScope.PERMISSIONS['MENU_ADMIN']}">
      <a href="${pageContext.request.contextPath}/admin/users">관리</a>
    </c:if>
  </div>
  <div class="user-actions">
    <a href="${pageContext.request.contextPath}/account/change-password">암호변경</a>
    <a href="${pageContext.request.contextPath}/logout">로그아웃</a>
  </div>
</div>
