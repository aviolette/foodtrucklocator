<%@ include file="vendorheader.jsp" %>

<form method="POST" action="/vendor/locations/${locationId}/stops/${stopId}/delete">
<p class="lead">
  Are you sure you want to delete this truck Stop?
</p>
  <div>
    <a href="/vendor/locations/${locationId}"/>Cancel</a>
    <input type="submit" class="btn btn-danger" value="Delete"/>
  </div>
</form>

<%@ include file="vendorfooter.jsp" %>
