<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 로그인 전용 화면을 제공한다. 기능: 사용자 ID/비밀번호/테넌트 입력을 받는다. 이유: 전용 로그인 페이지 정책을 준수하기 위함이다. 유지보수: 입력 필드 변경 시 이 파일을 수정한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>MES 로그인</title>
</head>
<body class="login-body">
  <%-- 목적: 상단 헤더 영역을 표시한다. 기능: 브랜드/바로가기 링크를 제공한다. 이유: 참고 UI 톤을 맞추기 위함이다. 유지보수: 링크 정책 변경 시 수정한다. --%>
  <div class="login-topbar">
    <div class="login-brand">
      <span class="brand-mark">H</span>
      <div class="brand-text">
        <strong>H:D</strong>
        <small>Technology</small>
      </div>
    </div>
    <div class="login-links">
      <a href="${pageContext.request.contextPath}/dashboard/production">Dashboard</a>
      <a href="${pageContext.request.contextPath}/account/change-password">FirstLogin</a>
      <a href="${pageContext.request.contextPath}/login" class="active">Login</a>
    </div>
  </div>

  <%-- 목적: 중앙 히어로 영역을 구성한다. 기능: 메인 타이틀/설명 문구를 노출한다. 이유: 이미지 톤과 구조를 맞추기 위함이다. 유지보수: 문구 변경 시 수정한다. --%>
  <div class="login-hero">
    <h1>MES</h1>
    <h2>Manufacturing Execution Systems</h2>
    <p>제조 프로세스의 품질과 효율성을 개선하는 체계적인 소프트웨어 솔루션입니다.</p>
  </div>

  <%-- 목적: 로그인 팝업을 제공한다. 기능: 입력 폼과 안내 영역을 모달로 표시한다. 이유: 메인 배경과 분리된 로그인 팝업을 구현하기 위함이다. 유지보수: 필드 추가 시 이 영역을 수정한다. --%>
  <div class="login-modal" id="loginModal">
    <div class="login-modal-backdrop"></div>
    <div class="login-panel">
      <button type="button" class="login-close" id="closeLoginModal">×</button>
      <div class="login-form-box">
        <h3>Login</h3>
        <p class="login-sub">Sign in to your account</p>
        <c:if test="${not empty errorMessage}">
          <div class="error">${errorMessage}</div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/login">
          <label>아이디</label>
          <div class="input-group">
            <span class="input-icon">ID</span>
            <input type="text" name="userId" placeholder="아이디" required />
          </div>
          <label>비밀번호</label>
          <div class="input-group">
            <span class="input-icon">PW</span>
            <input type="password" name="password" placeholder="비밀번호" required />
          </div>
          <label>기업(테넌트)</label>
          <div class="input-group">
            <span class="input-icon">CO</span>
            <input type="text" name="tenantId" placeholder="예: companyA" />
          </div>
          <button type="submit" class="login-btn">Login</button>
          <div class="remember-line">
            <label class="remember">
              <input type="checkbox" name="remember" />
              Remember me?
            </label>
          </div>
        </form>
      </div>
      <div class="login-info-box">
        <h3>Login</h3>
        <p>
          사전에 등록한 사용자만 로그인할 수 있습니다.<br />
          처음 접속하는 경우에는 ID와 동일한 PASSWORD를 입력하고<br />
          이후 새로운 PASSWORD로 변경합니다.
        </p>
      </div>
    </div>
  </div>

  <%-- 목적: 하단 안내 영역을 제공한다. 기능: 저작권 및 링크를 표시한다. 이유: 레퍼런스 하단 구조를 맞추기 위함이다. 유지보수: 문구 변경 시 수정한다. --%>
  <div class="login-footer">
    <span>© 2019 HID Technology, Ltd.</span>
    <div class="login-footer-links">
      <a href="#">About Us</a>
      <a href="#">MIT License</a>
      <a href="#">Oummuava V1.2</a>
    </div>
  </div>

  <%-- 목적: 로그인 모달 제어 스크립트를 제공한다. 기능: 로드 시 자동 표시 및 닫기 동작을 처리한다. 이유: 팝업 로그인 UX를 간단히 제공하기 위함이다. 유지보수: 동작 정책 변경 시 수정한다. --%>
  <script>
    (function () {
      var modal = document.getElementById("loginModal");
      var closeBtn = document.getElementById("closeLoginModal");
      var backdrop = modal ? modal.querySelector(".login-modal-backdrop") : null;
      var open = function () { if (modal) { modal.classList.add("show"); } };
      var close = function () { if (modal) { modal.classList.remove("show"); } };
      if (closeBtn) { closeBtn.addEventListener("click", close); }
      if (backdrop) { backdrop.addEventListener("click", close); }
      open();
    })();
  </script>
</body>
</html>
