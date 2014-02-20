<%@ include file="header.jsp" %>
<div class="row">
    <c:if test="${!empty(filteredBy)}">
      <div class="alert alert-message">
        Filtering list by: <strong>${filteredBy}</strong> (<a
          href="/trucks<c:if test="${!empty(truck)}">/${truck.id}</c:if>">reset truck list</a>).
      </div>

    </c:if>
    <h4>TRUCKS AND VENDORS</h4>
    <ul class="media-list">
      <c:forEach var="tr" items="${trucks}">
        <c:if test="${!tr.inactive}">
          <li class="media<c:if test="${tr.id == truck.id}"> active</c:if>"><a class="pull-left"
                 href="/trucks/${tr.id}<c:if test="${!empty(filteredBy)}">?tag=${filteredBy}</c:if>"><img src="${tr.iconUrl}"/></a><div class='media-body'><a href="/trucks/${tr.id}">${tr.name}</a></div></li>
        </c:if>
      </c:forEach>
      <%--
      <li class="nav-header">INACTIVE TRUCKS</li>
      <c:forEach var="tr" items="${trucks}">
        <c:if test="${tr.inactive}">
          <li><a class="<c:if test="${tr.id == truck.id}">active</c:if>" href="/trucks/${tr.id}">${tr.name}</a></li>
        </c:if>
      </c:forEach>
      --%>
    </ul>
</div>
<%@include file="include/core_js.jsp" %>
<%@ include file="footer.jsp" %>
