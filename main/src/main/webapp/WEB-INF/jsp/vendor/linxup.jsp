<%@ include file="vendorheader.jsp" %>

<p class="lead">Enter your username and password for <strong>your Linxup account</strong>. The username is typically an
  email address.</p>

<form role="form" class="form" method="post" action="">
  <div class="form-group" id="usernameGroup">
    <label class="control-label" for="username">Username</label>
    <input class="form-control" name="username" id="username" type="text" value="${username}" autofocus/>
  </div>
  <div class="form-group" id="passwordGroup">
    <label class="control-label" for="password">Password</label>
    <input class="form-control" name="password" id="password" type="password"/>
  </div>
  <div class="btn-group">
    <input type="submit" class="btn btn-primary btn-lg" value="Save"/>
  </div>
</form>

<%@ include file="../include/core_js.jsp" %>
<%@ include file="vendorfooter.jsp" %>
