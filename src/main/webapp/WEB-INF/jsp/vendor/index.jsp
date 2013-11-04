<%@ include file="vendorheader.jsp" %>

<c:if test="${!empty(errorMessage)}">
  <div class="alert alert-block alert-error">
    ${errorMessage}
  </div>
</c:if>

<div class="hero-unit">
  <h1>Vendor Dashboard</h1>
  <p>The Vendor Dashboard has some tools to manage your truck's information on The Chicago Food Truck Finder.</p>
  <a class="btn btn-primary" href="${loginUrl}">Login &raquo;</a>
</div>

<div>
  <p>
    What you will need to get started:
    <ul>
    <li>A <strong>gmail</strong> or <strong>google-apps</strong> account email address (this will be used for third-party sign-on)</li>
    <li>A food truck</li>
    </ul>
  </p>
  <p>DM or email your google email and I will set you up so you can use the vendor dashboard.  Once I instrument you,
    you just need to hit this URL, <a href="${loginUrl}">login here</a>, accept the stuff about this app knowing your
  email and your location, and click the button when you want to turn on your beacon.</p>
</div>

<%@ include file="vendorfooter.jsp" %>
