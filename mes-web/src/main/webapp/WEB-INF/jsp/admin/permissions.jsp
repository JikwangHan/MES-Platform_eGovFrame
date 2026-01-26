<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 목적: 사용자 권한 화면을 제공한다. 기능: 권한 매트릭스 영역을 표시한다. 이유: UI/UX 설계 기준을 반영하기 위함이다. 유지보수: 화면 상세 확정 시 이 파일을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>사용자 권한</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>사용자 권한</h2>
    <p>권한 매트릭스(서버 기준)</p>
    <c:if test="${not empty saveMessage}">
      <p class="notice">${saveMessage}</p>
    </c:if>
    <div class="permission-matrix">
      <table>
        <thead>
          <tr>
            <th>구분</th>
            <th>권한</th>
            <c:forEach var="role" items="${roles}">
              <th>${role}</th>
            </c:forEach>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="group" items="${permissionGroups}">
            <c:forEach var="item" items="${group.items}" varStatus="rowStatus">
              <tr>
                <c:if test="${rowStatus.first}">
                  <td class="group" rowspan="${fn:length(group.items)}">${group.name}</td>
                </c:if>
                <td>${item.label}</td>
                <c:forEach var="role" items="${roles}">
                  <td class="center">
                    <input type="checkbox" class="perm-checkbox" data-role="${role}" data-key="${item.key}"
                           ${rolePermissions[role][item.key] ? 'checked' : ''} />
                  </td>
                </c:forEach>
              </tr>
            </c:forEach>
          </c:forEach>
        </tbody>
      </table>
    </div>
    <div class="permission-actions">
      <form id="permissionSaveForm" method="post" action="${pageContext.request.contextPath}/admin/permissions/save">
        <label>저장 대상 역할</label>
        <select id="roleSelect" name="roleCode">
          <c:forEach var="role" items="${roles}">
            <option value="${role}">${role}</option>
          </c:forEach>
        </select>
        <button type="button" onclick="savePermissions()">권한 저장</button>
      </form>
      <p class="notice">권한 저장 시 선택된 역할의 권한이 즉시 덮어쓰여집니다.</p>
    </div>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
  <script>
    // 목적: 권한 저장 폼에 선택된 권한 키를 주입한다.
    // 기능: 선택된 역할의 체크박스를 읽어 hidden input으로 추가한다.
    // 이유: 서버가 permKeys 배열을 받을 수 있도록 하기 위함이다.
    // 유지보수: 역할 목록이 바뀌면 선택 UI를 수정한다.
    function savePermissions() {
      var form = document.getElementById("permissionSaveForm");
      var role = document.getElementById("roleSelect").value;
      var existing = form.querySelectorAll("input[name='permKeys']");
      for (var i = 0; i < existing.length; i++) {
        existing[i].remove();
      }
      var boxes = document.querySelectorAll(".perm-checkbox[data-role='" + role + "']");
      for (var j = 0; j < boxes.length; j++) {
        if (boxes[j].checked) {
          var input = document.createElement("input");
          input.type = "hidden";
          input.name = "permKeys";
          input.value = boxes[j].getAttribute("data-key");
          form.appendChild(input);
        }
      }
      form.submit();
    }
  </script>
</body>
</html>
