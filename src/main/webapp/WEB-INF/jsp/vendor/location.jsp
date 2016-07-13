<%@ include file="vendorheader.jsp" %>

<p class="lead">This is the location dashboard for <strong>${location.name}</strong>.  You can add, edit, or delete truck events
  at this location</p>

<table class="table">
  <thead>
    <tr>
      <th>Date</th>
      <th>Start</th>
      <th>End</th>
      <th>Truck</th>
      <th>&nbsp;</th>
    </tr>
  </thead>
  <tbody>
<c:forEach items="${stops}" var="stop">
    <tr>
      <td><joda:format value="${stop.startTime}" pattern="MM/dd/YYYY"/></td>
      <td><joda:format value="${stop.startTime}" pattern="hh:mm"/></td>
      <td><joda:format value="${stop.endTime}" pattern="hh:mm"/></td>
      <td>${stop.truck.name}</td>
    </tr>
</c:forEach>
  </tbody>
</table>

<a href="/vendor/locations/${locationId}/stops/new" class="btn btn-default"><span class="glyphicon glyphicon-plus"></span> New Event</a>

<%@ include file="vendorfooter.jsp" %>
