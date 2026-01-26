<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 목적: 공통 CRUD 버튼 영역을 제공한다. 기능: 권한에 따라 조회/등록/수정/삭제/추가 버튼을 출력한다. 이유: 버튼 UI를 재사용하고 권한 제어를 일관화하기 위함이다. 유지보수: 버튼 유형 추가 시 이 파일만 확장한다. --%>
<div class="form-actions">
  <c:if test="${not empty crudListUrl && sessionScope.PERMISSIONS[crudListPerm]}">
    <button type="button" onclick="MesWeb.post('${crudListUrl}', '${crudFormId}', '${crudResultId}')">조회</button>
  </c:if>
  <c:if test="${not empty crudCreateUrl && sessionScope.PERMISSIONS[crudCreatePerm]}">
    <button type="button" onclick="MesWeb.post('${crudCreateUrl}', '${crudFormId}', '${crudResultId}')">등록</button>
  </c:if>
  <c:if test="${not empty crudUpdateUrl && sessionScope.PERMISSIONS[crudUpdatePerm]}">
    <button type="button" onclick="MesWeb.post('${crudUpdateUrl}', '${crudFormId}', '${crudResultId}')">수정</button>
  </c:if>
  <c:if test="${not empty crudExtraUrl && sessionScope.PERMISSIONS[crudExtraPerm]}">
    <button type="button" onclick="MesWeb.post('${crudExtraUrl}', '${crudFormId}', '${crudResultId}')">${crudExtraLabel}</button>
  </c:if>
  <c:if test="${not empty crudDeleteUrl && sessionScope.PERMISSIONS[crudDeletePerm]}">
    <button type="button" onclick="MesWeb.post('${crudDeleteUrl}', '${crudFormId}', '${crudResultId}')">삭제</button>
  </c:if>
</div>
