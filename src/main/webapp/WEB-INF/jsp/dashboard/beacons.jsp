<%@include file="dashboardHeaderBS3.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a href="/admin/messages/new" class="btn btn-primary"><span class="glyphicon glyphicon-plus"></span> New Tracking Device</a>
  </div>
</div>


<table class="table">
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
      <td><ftl:truck truckId="${beacon.truckOwnerId}" admin="true"/></td>
    </tr>
  </c:forEach>
  </tbody>
</table>

<script type="text/javascript">
</script>


<%@include file="dashboardFooterBS3.jsp" %>
