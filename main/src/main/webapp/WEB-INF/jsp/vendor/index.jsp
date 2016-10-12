<%@ include file="vendorheader.jsp" %>

<c:if test="${!empty(errorMessage)}">
  <div class="alert alert-danger" role="alert">
    <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
    <span class="sr-only">Error:</span>
    ${errorMessage}
  </div>
</c:if>

<div class="hero-unit">
  <h1>Vendor Dashboard</h1>
  <p class="lead">The Vendor Dashboard has some tools to manage your truck's information on The Chicago Food Truck Finder.  You can
  sign in using your twitter credentials or your google credentials (to use Google, you must first tell me what email
  address(es) are associated with your account).</p>
  <div class="row">
    <div class="col-md-4 col-md-offset-4">
      <a class="btn btn-default btn-block btn-lg" href="${loginUrl}">Sign in with Google</a>
      <a href="/vendor/twitter" class="btn btn-default btn-block btn-lg">Sign in with Twitter</a>
    </div>
  </div>
</div>


<%@ include file="vendorfooter.jsp" %>
