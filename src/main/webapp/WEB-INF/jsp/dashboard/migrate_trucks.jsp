<%@include file="dashboardHeader.jsp" %>

<form method="POST" action="">
  <input type="hidden" name="delete" value="true"/>
  <input type="submit"/>
</form>

<ul>
  <c:forEach var="item" items="${trucks}">
    <li><a href="/cron/migrateWeeklyStats?truckId=${item.id}">${item.name}</a></li>
  </c:forEach>
</ul>

<%@include file="dashboardFooter.jsp" %>
