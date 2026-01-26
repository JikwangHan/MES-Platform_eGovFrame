<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 목적: 이메일 인증 화면을 구성한다. 기능: 공개 레이아웃과 인증 본문을 연결한다. 이유: 가입 이후 인증 절차를 제공하기 위함이다. 유지보수: 레이아웃 변경 시 경로를 수정한다. --%>
<c:set var="pageTitle" value="이메일 인증" scope="request" />
<c:set var="bodyClass" value="login-body mes-public-bg" scope="request" />
<c:set var="bodyPage" value="/WEB-INF/jsp/page/auth/signup_verify_body.jsp" scope="request" />
<jsp:include page="/WEB-INF/jsp/layout/public.jsp" />
