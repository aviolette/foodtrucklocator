<%@ include file="header.jsp" %>
<dl>
  <c:forEach items="${trucks}" var="truck">
    <dt>${truck.name}</dt>
    <dd><a href="/trucks/${truck.id}">View full profile</a>
    </dd>
  </c:forEach>
</dl>
<%@ include file="footer.jsp" %>
