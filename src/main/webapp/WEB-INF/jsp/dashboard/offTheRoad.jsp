<%@include file="dashboardHeaderBS3.jsp" %>

<p class="lead">
  Would you like to take <a href="/admin/trucks/${truck.id}">${truck.name}</a> off the road for today?
</p>

<form action="" method="POST">

  <div class="btn-group">
    <input type="submit" class="btn btn-primary" value="Do it!"/>
  </div>
</form>

<%@include file="dashboardFooterBS3.jsp" %>
