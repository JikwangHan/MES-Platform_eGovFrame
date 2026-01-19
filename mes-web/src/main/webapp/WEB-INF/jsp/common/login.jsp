<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 로그인 전용 화면을 제공한다. 기능: 사용자 ID/비밀번호/테넌트 입력을 받는다. 이유: 전용 로그인 페이지 정책을 준수하기 위함이다. 유지보수: 입력 필드 변경 시 이 파일을 수정한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>MES 로그인</title>
</head>
<body class="login-body">
  <div class="login-card">
    <h1>MES 로그인</h1>
    <c:if test="${not empty errorMessage}">
      <div class="error">${errorMessage}</div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/login">
      <label>아이디</label>
      <input type="text" name="userId" placeholder="아이디" required />
      <label>비밀번호</label>
      <input type="password" name="password" placeholder="비밀번호" required />
      <label>기업(테넌트)</label>
      <input type="text" name="tenantId" placeholder="예: companyA" />
      <button type="submit">로그인</button>
    </form>
  </div>
</body>
</html>
