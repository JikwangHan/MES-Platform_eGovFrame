<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 사용자 권한 화면을 제공한다. 기능: 권한 매트릭스 영역을 표시한다. 이유: UI/UX 설계 기준을 반영하기 위함이다. 유지보수: 화면 상세 확정 시 이 파일을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>사용자 권한</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>사용자 권한</h2>
    <p>권한 매트릭스(서버 기준)</p>
    <div class="permission-matrix">
      <table>
        <thead>
          <tr>
            <th>구분</th>
            <th>권한</th>
            <th>SYSTEM_ADMIN</th>
            <th>MANAGER</th>
            <th>OPERATOR</th>
            <th>VIEWER</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="group" items="${permissionGroups}">
            <c:forEach var="item" items="${group.items}" varStatus="rowStatus">
              <tr>
                <c:if test="${rowStatus.first}">
                  <td class="group" rowspan="${fn:length(group.items)}">${group.name}</td>
                </c:if>
                <td>${item.label}</td>
                <td class="center">${rolePermissions['SYSTEM_ADMIN'][item.key] ? 'Y' : 'N'}</td>
                <td class="center">${rolePermissions['MANAGER'][item.key] ? 'Y' : 'N'}</td>
                <td class="center">${rolePermissions['OPERATOR'][item.key] ? 'Y' : 'N'}</td>
                <td class="center">${rolePermissions['VIEWER'][item.key] ? 'Y' : 'N'}</td>
              </tr>
            </c:forEach>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
