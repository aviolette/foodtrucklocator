<%@include file="dashboardHeaderBS3.jsp" %>

<div class="row">
  <div class="col-md-2">

    <h3>${truck.name}</h3>
    <ul class="list-unstyled">
      <li><a href="/admin/trucks/${truck.id}">Schedule</a></li>
      <li><a href="/admin/trucks/${truck.id}/configuration">Configuration</a></li>
      <li><a href="/admin/trucks/${truck.id}/stats">Stats</a></li>
      <li><a href="/admin/trucks/${truck.id}/beacons">Beacons</a></li>
    </ul>

    <ul class="list-unstyled">
      <li><a href="/admin/trucks/${truck.id}/menu">Edit Menu</a></li>
      <li><a href="/trucks/${truck.id}">View Public Page</a></li>
    </ul>

  </div>
  <div class="col-md-10">
