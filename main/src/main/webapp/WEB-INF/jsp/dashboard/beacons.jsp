<%@include file="dashboardHeaderBS3.jsp" %>

<table class="table table-hover">
  <thead>
  <th>Label</th>
  <th>Device ID</th>
  <th>Owner</th>
  </thead>
  <tbody>
  <c:forEach var="beacon" items="${devices}" varStatus="rowStatus">
    <tr id="beacon-${rowStatus.index}">
      <td><a href="/admin/beacons/${beacon.key}">${beacon.label}</a></td>
      <td>${beacon.deviceNumber}</td>
    </tr>
  </c:forEach>
  </tbody>
</table>

<%@include file="dashboardFooterBS3.jsp" %>
