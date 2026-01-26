<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 역할 관리 화면을 제공한다. 기능: 역할 목록과 추가/삭제를 표시한다. 이유: 권한 관리 기반을 마련하기 위함이다. 유지보수: 역할 정책 확정 시 화면을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>역할 관리</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>역할 관리</h2>
    <c:if test="${not empty saveMessage}">
      <p class="notice">${saveMessage}</p>
    </c:if>
    <div class="form-section">
      <h3>역할 추가</h3>
      <form method="post" action="${pageContext.request.contextPath}/admin/roles/create">
        <label>역할 코드(영문 대문자/숫자/언더바)</label>
        <input type="text" name="roleCode" placeholder="AUDITOR" />
        <label>역할명</label>
        <input type="text" name="roleName" placeholder="감사자" />
        <label>설명</label>
        <input type="text" name="roleDesc" placeholder="조회/감사 전용 역할" />
        <div class="form-actions">
          <button type="submit">등록</button>
        </div>
      </form>
    </div>
    <div class="form-section">
      <h3>역할 목록</h3>
      <table class="permission-table">
        <thead>
          <tr>
            <th>역할 코드</th>
            <th>역할명</th>
            <th>설명</th>
            <th>상태</th>
            <th>삭제</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="role" items="${roles}">
            <tr>
              <td>${role.roleCode}</td>
              <td>${role.roleName}</td>
              <td>${role.roleDesc}</td>
              <td>${role.status}</td>
              <td>
                <form method="post" action="${pageContext.request.contextPath}/admin/roles/delete">
                  <input type="hidden" name="roleCode" value="${role.roleCode}" />
                  <button type="submit">삭제</button>
                </form>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
