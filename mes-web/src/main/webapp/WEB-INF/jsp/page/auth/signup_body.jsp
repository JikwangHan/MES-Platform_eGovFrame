<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 목적: 회원가입 본문을 제공한다. 기능: 로그인 톤을 유지한 중앙 카드 폼을 표시한다. 이유: 사용자 등록 절차를 제공하기 위함이다. 유지보수: 입력 항목 확정 시 수정한다. --%>
<div class="login-topbar">
  <div class="login-brand">
    <span class="brand-text-only">MES</span>
  </div>
  <div class="login-links">
    <a href="${pageContext.request.contextPath}/dashboard/production">Dashboard</a>
    <a href="${pageContext.request.contextPath}/signup" class="login-button">회원가입</a>
    <a href="${pageContext.request.contextPath}/login">Login</a>
  </div>
</div>

<div class="login-hero">
  <h1>MES</h1>
  <h2>Manufacturing Execution Systems</h2>
  <p>MES는 제조 프로세스의 품질과 효율성을 개선하는 체계적인 제조 실행 소프트웨어 솔루션입니다.</p>
</div>

<div class="login-modal show">
  <div class="login-panel signup-panel">
    <div class="login-form-box">
      <h3>Sign Up</h3>
      <p class="login-sub">필수 항목을 입력하면 가입이 완료됩니다.</p>
      <c:if test="${not empty errorMessage}">
        <div class="error">${errorMessage}</div>
      </c:if>
      <form method="post" action="${pageContext.request.contextPath}/signup">
        <label>아이디</label>
        <div class="input-group no-icon">
          <input type="text" name="userId" value="${form.userId}" required />
        </div>
        <label>이름</label>
        <div class="input-group no-icon">
          <input type="text" name="userName" value="${form.userName}" required />
        </div>
        <label>비밀번호</label>
        <div class="input-group no-icon">
          <input type="password" name="password" required />
        </div>
        <label>비밀번호 확인</label>
        <div class="input-group no-icon">
          <input type="password" name="passwordConfirm" required />
        </div>
        <label>역할</label>
        <div class="input-group no-icon">
          <select name="role" class="input-select">
            <option value="OPERATOR" <c:if test="${form.role eq 'OPERATOR'}">selected</c:if>>작업자</option>
            <option value="VIEWER" <c:if test="${form.role eq 'VIEWER'}">selected</c:if>>조회자</option>
          </select>
        </div>
        <label>기업 코드(테넌트)</label>
        <div class="input-group no-icon">
          <input type="text" name="tenantId" value="${form.tenantId}" />
        </div>
        <label>연락처(선택)</label>
        <div class="input-group no-icon">
          <input type="text" name="phone" value="${form.phone}" />
        </div>
        <label>이메일(선택)</label>
        <div class="input-group no-icon">
          <input type="text" name="email" value="${form.email}" />
        </div>
        <div class="signup-agree">
          <label class="agree-line">
            <input type="checkbox" name="agreeTerms" />
            <span>이용약관에 동의합니다. (필수)</span>
            <button type="button" class="link-button" data-bs-toggle="modal" data-bs-target="#termsModal">전문보기</button>
          </label>
          <label class="agree-line">
            <input type="checkbox" name="agreePrivacy" />
            <span>개인정보 처리방침에 동의합니다. (필수)</span>
            <button type="button" class="link-button" data-bs-toggle="modal" data-bs-target="#privacyModal">전문보기</button>
          </label>
          <label class="agree-line">
            <input type="checkbox" name="agreeMarketing" />
            <span>이벤트/마케팅 정보 수신에 동의합니다. (선택)</span>
          </label>
          <label class="agree-line">
            <input type="checkbox" name="autoLogin" value="Y" />
            <span>회원가입 후 자동 로그인</span>
          </label>
        </div>
        <button type="submit" class="login-btn">회원가입</button>
        <div class="remember-line">
          <span class="help-text">가입 후 로그인 화면으로 이동합니다.</span>
          <a class="signup-link" href="${pageContext.request.contextPath}/login">로그인</a>
        </div>
      </form>
    </div>
    <div class="login-info-box">
      <h3>Welcome</h3>
      <p>
        가입 시 입력한 정보는 안전하게 암호화되어 저장됩니다.<br />
        관리자 승인 후 권한을 부여받을 수 있습니다.<br />
        비밀번호는 복호화가 불가능한 해시로만 저장됩니다.
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

<div class="modal fade" id="termsModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">이용약관</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <p>본 약관은 MES 서비스 이용에 필요한 기본 사항을 안내합니다.</p>
        <ul>
          <li>서비스 이용 목적에 맞게 계정을 생성하고 사용합니다.</li>
          <li>타인의 계정을 도용하거나 불법 행위를 하지 않습니다.</li>
          <li>서비스 정책은 공지 후 변경될 수 있습니다.</li>
        </ul>
        <p>자세한 내용은 운영 정책에 따라 안내됩니다.</p>
      </div>
    </div>
  </div>
</div>

<div class="modal fade" id="privacyModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">개인정보 처리방침</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>
      </div>
      <div class="modal-body">
        <p>개인정보는 서비스 제공을 위해 최소한으로 수집됩니다.</p>
        <ul>
          <li>수집 항목: 아이디, 이름, 연락처, 이메일</li>
          <li>보관 기간: 서비스 이용 기간 및 관련 법령에 따름</li>
          <li>보안: 암호화 저장 및 접근 통제</li>
        </ul>
        <p>자세한 내용은 운영 정책에 따라 안내됩니다.</p>
      </div>
    </div>
  </div>
</div>
