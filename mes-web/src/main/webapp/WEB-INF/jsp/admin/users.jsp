<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 목적: 사용자 관리 화면을 제공한다. 기능: 사용자 그리드 영역을 표시한다. 이유: UI/UX 설계 기준을 반영하기 위함이다. 유지보수: 화면 상세 확정 시 이 파일을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>사용자 관리</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>사용자 관리</h2>
    <c:if test="${not empty saveMessage}">
      <div class="success">${saveMessage}</div>
    </c:if>
    <p>사용자 그리드</p>
    <div class="form-section">
      <h3>승인 대기 사용자</h3>
      <c:choose>
        <c:when test="${empty pendingUsers}">
          <p>승인 대기 사용자가 없습니다.</p>
        </c:when>
        <c:otherwise>
          <table class="permission-table">
            <thead>
              <tr>
                <th>아이디</th>
                <th>역할</th>
                <th>상태</th>
                <th>요청일</th>
                <th>승인</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="user" items="${pendingUsers}">
                <tr>
                  <td><c:out value="${user.user_id}" /></td>
                  <td><c:out value="${user.role}" /></td>
                  <td><c:out value="${user.status}" /></td>
                  <td><c:out value="${user.created_at}" /></td>
                  <td>
                    <form method="post" action="${pageContext.request.contextPath}/admin/users/approve">
                      <input type="hidden" name="userId" value="${user.user_id}" />
                      <button type="submit" class="btn btn-primary btn-sm">승인</button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </c:otherwise>
      </c:choose>
    </div>
    <div class="form-section">
      <h3>관리 메뉴</h3>
      <div class="form-actions">
        <a href="${pageContext.request.contextPath}/admin/roles">역할 관리</a>
        <a href="${pageContext.request.contextPath}/admin/permissions">권한 매트릭스</a>
      </div>
    </div>
  <%@ include file="/WEB-INF/jsp/common/grid.jsp" %>
  <%@ include file="/WEB-INF/jsp/common/modal.jsp" %>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
