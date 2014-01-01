<%@ include file="header.jsp" %>

<h2>${foodTruckRequest.eventName}</h2>
<table class="table">
  <tr>
    <td>Start date:</td>
    <td> <joda:format pattern="MMM dd, YYYY" value="${foodTruckRequest.startTime}"/></td>
  </tr>
  <tr>
    <td>End date:</td>
    <td> <joda:format pattern="MMM dd, YYYY" value="${foodTruckRequest.endTime}"/></td>
  </tr>
  <tr>
    <td>Contact name:</td>
    <td> ${foodTruckRequest.requester}</td>
  </tr>
  <tr>
    <td>Email:</td>
    <td>${foodTruckRequest.email}</td>
  </tr>
  <tr>
    <td>Phone:</td>
    <td> ${foodTruckRequest.phone}</td>
  </tr>
  <tr>
    <td>Expected Guests</td><td> ${foodTruckRequest.expectedGuests}</td>
  </tr>
  <tr>
    <td>Prepaid</td>
    <td>${foodTruckRequest.prepaid}</td>
  </tr>
</table>

<p class="lead">${foodTruckRequest.description}</p>


<%@include file="include/core_js.jsp" %>

<%@ include file="footer.jsp" %>
