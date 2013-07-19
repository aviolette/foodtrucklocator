<%@ include file="vendorheader.jsp" %>

<c:if test="${!empty(errorMessage)}">
  <div class="alert alert-block alert-error">
    ${errorMessage}
  </div>
</c:if>

<div>
  <h1>Beaconnaise</h1>
  <p>Project your location to the Chicago Food Truck Finder using this simple web app.  Once
  your location is projected, a special indicator will appear on the Food Truck Finder web page indicating that the beacon
  is turned on.  This will give people an extra level of confidence that you are where the marker is.</p>
</div>

<div>
  <p>
    I am currently looking for some early-adopters of this functionality to help me test it out.  Here's what you need to
    get started:
    <ul>
    <li>A gmail or google-apps account email address</li>
    <li>A food truck</li>
    <li>A modern smart phone with GPS/javascript-enabled web-browser</li>
    </ul>
  </p>
  <p>DM or email your google email and I will set you up so you can use beaconnaise.  Once I instrument you,
    you just need to hit this URL, <a href="${loginUrl}">login here</a>, accept the stuff about this app knowing your
  email and your location, and click the button when you want to turn on your beacon.</p>
  <p>I am eventually planning on having an Android and iPhone app if this functionality actually gets used.</p>
</div>

<%@ include file="vendorfooter.jsp" %>
