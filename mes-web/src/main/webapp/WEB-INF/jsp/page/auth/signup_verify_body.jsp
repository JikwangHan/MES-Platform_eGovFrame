<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 목적: 이메일 인증 본문을 제공한다. 기능: 인증 코드 입력 UI를 표시한다. 이유: 가입 후 인증 절차를 완료하기 위함이다. 유지보수: 안내 문구/필드 변경 시 수정한다. --%>
<div class="login-topbar">
  <div class="login-brand">
    <span class="brand-text-only">MES</span>
  </div>
  <div class="login-links">
    <a href="${pageContext.request.contextPath}/login" class="login-button">Login</a>
  </div>
</div>

<div class="login-hero">
  <h1>MES</h1>
  <h2>Manufacturing Execution Systems</h2>
  <p>가입한 이메일로 발송된 인증 코드를 입력해 주세요.</p>
</div>

<div class="login-modal show">
  <div class="login-panel signup-panel">
    <div class="login-form-box">
      <h3>이메일 인증</h3>
      <p class="login-sub">인증이 완료되면 로그인할 수 있습니다.</p>
      <c:if test="${not empty errorMessage}">
        <div class="error">${errorMessage}</div>
      </c:if>
      <c:if test="${not empty verifyError}">
        <div class="error">${verifyError}</div>
      </c:if>
      <c:if test="${verifySent eq true}">
        <div class="success">인증 메일이 발송되었습니다. 받은 코드를 입력해 주세요.</div>
      </c:if>
      <c:if test="${verifySent ne true}">
        <div class="notice">이메일 발송이 비활성화되어 화면에 표시된 인증 코드를 사용하세요.</div>
      </c:if>
      <c:if test="${not empty devCode}">
        <div class="success">개발용 인증 코드: ${devCode}</div>
      </c:if>
      <div class="verify-summary">
        <span>아이디: <strong><c:out value="${verifyUserId}" /></strong></span>
        <span>이메일: <strong><c:out value="${verifyEmail}" /></strong></span>
      </div>
      <form method="post" action="${pageContext.request.contextPath}/signup/verify">
        <label>인증 코드</label>
        <div class="input-group no-icon">
          <input type="text" name="code" required />
        </div>
        <button type="submit" class="login-btn">인증 완료</button>
      </form>
    </div>
    <div class="login-info-box">
      <h3>Security</h3>
      <p>
        인증 코드는 가입한 이메일로 전송됩니다.<br />
        입력한 정보는 암호화되어 안전하게 보호됩니다.<br />
        코드가 오지 않으면 관리자에게 문의해 주세요.
      </p>
    </div>
  </div>
</div>

<div class="login-footer">
  <span>© 2025 MES (업체 로고/정보 교체 예정)</span>
  <div class="login-footer-links">
    <a href="#">About Us</a>
    <a href="#">MES License</a>
    <a href="#">MES V0.5</a>
  </div>
</div>
