<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 목적: 사용자 관리 화면을 제공한다. 기능: 사용자 그리드/승인 흐름/이력 조회를 표시한다. 이유: 관리자 운영 기능을 통합하기 위함이다. 유지보수: 승인/이력 정책 확정 시 항목을 보완한다. --%>
<%@ include file="/WEB-INF/jsp/common/include.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>사용자 관리</title>
</head>
<body>
  <%@ include file="/WEB-INF/jsp/common/header.jsp" %>
  <div class="content">
    <h2>사용자 관리</h2>
    <c:if test="${not empty saveMessage}">
      <div class="success">${saveMessage}</div>
    </c:if>
    <c:if test="${not empty errorMessage}">
      <div class="error">${errorMessage}</div>
    </c:if>

    <div class="form-section">
      <div class="section-header">
        <h3>승인 대기 사용자</h3>
        <div class="queue-actions">
          <span>이메일 재시도 대기: <strong>${emailRetryQueueSize}</strong>건</span>
          <form method="post" action="${pageContext.request.contextPath}/admin/users/email-retry">
            <button type="submit" class="btn btn-sm btn-outline-primary">재시도 실행</button>
          </form>
        </div>
      </div>

      <form method="get" action="${pageContext.request.contextPath}/admin/users" class="filter-form">
        <div class="filter-grid">
          <label class="filter-item">아이디 키워드
            <input type="text" name="pendingKeyword" value="${pendingKeyword}" />
          </label>
          <label class="filter-item">역할
            <select name="pendingRole">
              <option value="" <c:if test="${empty pendingRole}">selected</c:if>>전체</option>
              <option value="ADMIN" <c:if test="${pendingRole eq 'ADMIN'}">selected</c:if>>ADMIN</option>
              <option value="MANAGER" <c:if test="${pendingRole eq 'MANAGER'}">selected</c:if>>MANAGER</option>
              <option value="OPERATOR" <c:if test="${pendingRole eq 'OPERATOR'}">selected</c:if>>OPERATOR</option>
              <option value="VIEWER" <c:if test="${pendingRole eq 'VIEWER'}">selected</c:if>>VIEWER</option>
            </select>
          </label>
          <label class="filter-item">요청일(시작)
            <input type="date" name="pendingFromDate" value="${pendingFromDate}" />
          </label>
          <label class="filter-item">요청일(종료)
            <input type="date" name="pendingToDate" value="${pendingToDate}" />
          </label>
          <label class="filter-item">페이지 크기
            <select name="pendingSize">
              <option value="5" <c:if test="${pendingSize == 5}">selected</c:if>>5</option>
              <option value="10" <c:if test="${pendingSize == 10}">selected</c:if>>10</option>
              <option value="20" <c:if test="${pendingSize == 20}">selected</c:if>>20</option>
              <option value="50" <c:if test="${pendingSize == 50}">selected</c:if>>50</option>
            </select>
          </label>
        </div>
        <div class="filter-actions">
          <button type="submit" class="btn btn-primary btn-sm">검색</button>
          <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/admin/users">초기화</a>
        </div>
      </form>

      <c:choose>
        <c:when test="${empty pendingUsers}">
          <p>승인 대기 사용자가 없습니다.</p>
        </c:when>
        <c:otherwise>
          <table class="permission-table">
            <thead>
              <tr>
                <th>아이디</th>
                <th>역할</th>
                <th>상태</th>
                <th>요청일</th>
                <th>처리</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="user" items="${pendingUsers}">
                <tr>
                  <td><c:out value="${user.user_id}" /></td>
                  <td><c:out value="${user.role}" /></td>
                  <td><c:out value="${user.status}" /></td>
                  <td><c:out value="${user.created_at}" /></td>
                  <td>
                    <div class="action-stack">
                      <form method="post" action="${pageContext.request.contextPath}/admin/users/approve">
                        <input type="hidden" name="userId" value="${user.user_id}" />
                        <select name="reasonCode" class="inline-select" required>
                          <c:forEach var="reason" items="${approvalReasons}">
                            <option value="${reason.code}" <c:if test="${reason.code eq 'APPROVE_GENERAL'}">selected</c:if>>${reason.label}</option>
                          </c:forEach>
                        </select>
                        <input type="text" name="reason" class="inline-input" placeholder="승인 사유(선택)" />
                        <button type="submit" class="btn btn-primary btn-sm">승인</button>
                      </form>
                      <form method="post" action="${pageContext.request.contextPath}/admin/users/reject">
                        <input type="hidden" name="userId" value="${user.user_id}" />
                        <select name="reasonCode" class="inline-select" required>
                          <c:forEach var="reason" items="${approvalReasons}">
                            <option value="${reason.code}" <c:if test="${reason.code eq 'REJECT_OTHER'}">selected</c:if>>${reason.label}</option>
                          </c:forEach>
                        </select>
                        <input type="text" name="reason" class="inline-input" placeholder="반려 사유(필수)" required />
                        <button type="submit" class="btn btn-outline-danger btn-sm">반려</button>
                      </form>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
          <c:if test="${pendingTotalPages > 1}">
            <div class="pagination">
              <c:forEach var="page" begin="1" end="${pendingTotalPages}">
                <c:url var="pendingPageUrl" value="/admin/users">
                  <c:param name="pendingPage" value="${page}" />
                  <c:param name="pendingSize" value="${pendingSize}" />
                  <c:param name="pendingKeyword" value="${pendingKeyword}" />
                  <c:param name="pendingRole" value="${pendingRole}" />
                  <c:param name="pendingFromDate" value="${pendingFromDate}" />
                  <c:param name="pendingToDate" value="${pendingToDate}" />
                  <c:param name="historyPage" value="${historyPage}" />
                  <c:param name="historySize" value="${historySize}" />
                  <c:param name="historyKeyword" value="${historyKeyword}" />
                  <c:param name="historyAction" value="${historyAction}" />
                  <c:param name="historyFromDate" value="${historyFromDate}" />
                  <c:param name="historyToDate" value="${historyToDate}" />
                </c:url>
                <a class="page-link ${page == pendingPage ? 'active' : ''}" href="${pendingPageUrl}">${page}</a>
              </c:forEach>
            </div>
          </c:if>
        </c:otherwise>
      </c:choose>
    </div>

    <div class="form-section">
      <h3>승인 처리 이력</h3>
      <c:if test="${not approvalHistoryEnabled}">
        <p>승인 이력 기능이 비활성화 상태입니다. (MES_APPROVAL_HISTORY_ENABLED 설정 필요)</p>
      </c:if>
      <c:if test="${approvalHistoryEnabled}">
        <form method="get" action="${pageContext.request.contextPath}/admin/users" class="filter-form">
          <div class="filter-grid">
            <label class="filter-item">아이디 키워드
              <input type="text" name="historyKeyword" value="${historyKeyword}" />
            </label>
            <label class="filter-item">처리 구분
              <select name="historyAction">
                <option value="" <c:if test="${empty historyAction}">selected</c:if>>전체</option>
                <option value="approve" <c:if test="${historyAction eq 'approve'}">selected</c:if>>승인</option>
                <option value="reject" <c:if test="${historyAction eq 'reject'}">selected</c:if>>반려</option>
              </select>
            </label>
            <label class="filter-item">처리일(시작)
              <input type="date" name="historyFromDate" value="${historyFromDate}" />
            </label>
            <label class="filter-item">처리일(종료)
              <input type="date" name="historyToDate" value="${historyToDate}" />
            </label>
            <label class="filter-item">페이지 크기
              <select name="historySize">
                <option value="5" <c:if test="${historySize == 5}">selected</c:if>>5</option>
                <option value="10" <c:if test="${historySize == 10}">selected</c:if>>10</option>
                <option value="20" <c:if test="${historySize == 20}">selected</c:if>>20</option>
                <option value="50" <c:if test="${historySize == 50}">selected</c:if>>50</option>
              </select>
            </label>
          </div>
          <div class="filter-actions">
            <button type="submit" class="btn btn-primary btn-sm">검색</button>
            <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/admin/users">초기화</a>
          </div>
        </form>

        <c:choose>
          <c:when test="${empty historyRows}">
            <p>승인 이력이 없습니다.</p>
          </c:when>
          <c:otherwise>
            <table class="permission-table">
              <thead>
                <tr>
                  <th>아이디</th>
                  <th>처리</th>
                  <th>사유 코드</th>
                  <th>사유 내용</th>
                  <th>처리자</th>
                  <th>처리일</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="row" items="${historyRows}">
                  <tr>
                    <td><c:out value="${row.user_id}" /></td>
                    <td><c:out value="${row.action_type}" /></td>
                    <td><c:out value="${row.reason_code}" /></td>
                    <td><c:out value="${row.reason_text}" /></td>
                    <td><c:out value="${row.actor_user_id}" /></td>
                    <td><c:out value="${row.created_at}" /></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
            <c:if test="${historyTotalPages > 1}">
              <div class="pagination">
                <c:forEach var="page" begin="1" end="${historyTotalPages}">
                  <c:url var="historyPageUrl" value="/admin/users">
                    <c:param name="pendingPage" value="${pendingPage}" />
                    <c:param name="pendingSize" value="${pendingSize}" />
                    <c:param name="pendingKeyword" value="${pendingKeyword}" />
                    <c:param name="pendingRole" value="${pendingRole}" />
                    <c:param name="pendingFromDate" value="${pendingFromDate}" />
                    <c:param name="pendingToDate" value="${pendingToDate}" />
                    <c:param name="historyPage" value="${page}" />
                    <c:param name="historySize" value="${historySize}" />
                    <c:param name="historyKeyword" value="${historyKeyword}" />
                    <c:param name="historyAction" value="${historyAction}" />
                    <c:param name="historyFromDate" value="${historyFromDate}" />
                    <c:param name="historyToDate" value="${historyToDate}" />
                  </c:url>
                  <a class="page-link ${page == historyPage ? 'active' : ''}" href="${historyPageUrl}">${page}</a>
                </c:forEach>
              </div>
            </c:if>
          </c:otherwise>
        </c:choose>
      </c:if>
    </div>

    <div class="form-section">
      <h3>관리 메뉴</h3>
      <div class="form-actions">
        <a href="${pageContext.request.contextPath}/admin/roles">역할 관리</a>
        <a href="${pageContext.request.contextPath}/admin/permissions">권한 매트릭스</a>
      </div>
    </div>
    <%@ include file="/WEB-INF/jsp/common/grid.jsp" %>
    <%@ include file="/WEB-INF/jsp/common/modal.jsp" %>
  </div>
  <%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
</body>
</html>
