<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 공통 모달 템플릿을 제공한다. 기능: 기본 모달 레이아웃을 제공한다. 이유: CRUD 모달 재사용성을 높이기 위함이다. 유지보수: 모달 UI 변경 시 이 파일만 수정한다. --%>
<div class="modal" style="display:none;">
  <div class="modal-content">
    <div class="modal-header">공통 모달</div>
    <div class="modal-body">입력 폼 영역</div>
    <div class="modal-footer">
      <button type="button">저장</button>
      <button type="button">닫기</button>
    </div>
  </div>
</div>
