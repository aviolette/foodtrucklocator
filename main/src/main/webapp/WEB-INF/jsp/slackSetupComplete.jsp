<%@ taglib prefix="o" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>


<div class="jumbotron">
  <h1>Slack Setup Complete!</h1>
  <p class="lead">Your Chicago Food Truck Finder slack webhook is now installed in your workspace. It will send you
    alerts when new trucks are in your area. To change the location that you want the webhook to monitor, re-add the
    Slack integration and change a different location at the post-install step.</p>
  <a class="btn-lg btn-primary" href="/">Go To Chicago Food Truck Finder</a>
</div>


<%@include file="include/core_js.jsp" %>

<%@ include file="footer.jsp" %>