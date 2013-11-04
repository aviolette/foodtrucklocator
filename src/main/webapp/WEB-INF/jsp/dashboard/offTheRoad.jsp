<%@include file="dashboardHeader.jsp" %>

Would you like to take <a href="/admin/trucks/${truck.id}">${truck.name}</a> off the road for today?

<form action="" method="POST">
  <input type="submit" value="Do it!"/>
</form>

<%@include file="dashboardFooter.jsp" %>
