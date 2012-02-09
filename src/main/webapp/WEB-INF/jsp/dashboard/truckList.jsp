<%@ include file="dashboardHeader.jsp" %>

<h3>Active Trucks</h3>
<table>
  <thead>
    <tr>
      <th>Truck</th>
      <th>Current Location</th>
      <th>Next Location</th>
    </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" items="${trucks}">
    <c:if test="${truckStops.active}">
    <tr><td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td><td><c:choose><c:when test="${!empty(truckStops.currentStop)}">${truckStops.currentStop.location.name}</c:when><c:otherwise>Not Active</c:otherwise></c:choose></td><td><c:choose><c:when test="${!empty(truckStops.nextStop)}">${truckStops.nextStop.location.name}</c:when><c:otherwise>None</c:otherwise></c:choose></td></tr>
    </c:if>
  </c:forEach>
  </tbody>
</table>
<h3>Inactive Trucks</h3>
<table>
  <thead>
    <tr>
      <th>Truck</th>
    </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" items="${trucks}">
    <c:if test="${!truckStops.active}">
    <tr><td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td></tr>
    </c:if>
  </c:forEach>
  </tbody>
</table>
<%@ include file="dashboardFooter.jsp" %>