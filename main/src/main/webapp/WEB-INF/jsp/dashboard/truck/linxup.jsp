<%@include file="../dashboardHeader1.jsp" %>
<%@include file="truckNav.jsp" %>

<p class="lead">To link Chicago Food Truck Finder with Linxup, enter your username and password for <strong>your Linxup
  account</strong>. It is recommended that you use a separate readonly account for this purpose.</p>

<form role="form" class="form" method="post" action="">
  <div class="form-group" id="usernameGroup">
    <label class="control-label" for="username">Username</label>
    <input class="form-control" name="username" id="username" type="text" value="${username}" autofocus/>
  </div>
  <div class="form-group" id="passwordGroup">
    <label class="control-label" for="password">Password</label>
    <input class="form-control" name="password" id="password" type="password"/>
  </div>
  <c:if test="${!empty(username)}">
    <div class="btn-group">
      <input type="submit" name="action" class="btn btn-danger" id="unlinkButton" value="Unlink Account"/>
    </div>
  </c:if>
  <div class="btn-group">
    <input type="submit" class="btn btn-outline-primary" value="Save"/>
  </div>
</form>

<%@include file="../dashboardFooter1.jsp" %>
