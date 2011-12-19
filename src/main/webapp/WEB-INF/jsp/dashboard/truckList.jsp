<%@ include file="dashboardHeader.jsp" %>
<ul>
  <c:forEach var="truck" items="${trucks}">
    <li><a href="/admin/trucks/${truck.id}">${truck.name}</a></li>
  </c:forEach>
</ul>
<%@ include file="dashboardFooter.jsp" %>