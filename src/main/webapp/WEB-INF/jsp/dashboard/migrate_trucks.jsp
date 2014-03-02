<%@include file="dashboardHeader.jsp" %>

<form method="POST" action="">
  <input type="hidden" name="delete" value="true"/>
  <input type="submit" value="Delete"/>
</form>

<form method="POST" action="">
  <input type="hidden" name="compress" value="true"/>
  <input type="submit" value="Compress"/>
</form>

<a href="/cron/migrateWeeklyStats?truckId=5411empanadas">Get the ball rolling</a>


<c:if test="${!empty(nextTruckId)}">
Next truck: ${nextTruckId}
<script>
  setTimeout(function() {
    location.href = "/cron/migrateWeeklyStats?truckId=${nextTruckId}"
  }, 5000);
</script>
</c:if>

<%@include file="dashboardFooter.jsp" %>
