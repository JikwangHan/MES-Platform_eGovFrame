<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 루트 접근 시 로그인으로 이동한다. 기능: /index.jsp에서 /login으로 리다이렉트한다. 이유: 전용 로그인 페이지 정책을 유지하기 위함이다. 유지보수: 기본 랜딩 정책 변경 시 수정한다. --%>
<% response.sendRedirect(request.getContextPath() + "/login"); %>
