<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 목적: 공통 리소스와 태그 라이브러리를 제공한다. 기능: 모든 JSP에서 공통 CSS/JS를 로드한다. 이유: 중복을 줄이고 일관된 UI를 유지하기 위함이다. 유지보수: 리소스 경로 변경 시 이 파일만 수정한다. --%>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/main.css" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/mes-theme.css" />
<script src="${pageContext.request.contextPath}/resources/js/main.js"></script>
