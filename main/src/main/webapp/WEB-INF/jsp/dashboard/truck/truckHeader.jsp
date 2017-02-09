<%@include file="../dashboardHeaderBS3.jsp" %>

<div class="row">
  <div class="col-md-2">

    <c:choose>
      <c:when test="${empty(truck.previewIcon)}">
        <h3>${truck.name}</h3>
      </c:when>
      <c:otherwise>
        <img src="${truck.previewIcon}" class="img-rounded img-responsive" alt="${truck.name}" width="150"
             height="150"/>
      </c:otherwise>
    </c:choose>
    <nav>
      <ul class="nav list-unstyled">
        <li><a href="/admin/trucks/${truck.id}">Schedule</a></li>
        <li><a href="/admin/trucks/${truck.id}/configuration">Configuration</a></li>
        <li><a href="/admin/trucks/${truck.id}/stats">Stats</a></li>
        <li><a href="/admin/trucks/${truck.id}/beacons">Beacons</a></li>
        <li><a href="/admin/trucks/${truck.id}/danger">Danger Zone</a></li>
      </ul>

      <ul class="nav list-unstyled">
        <li><a href="/admin/trucks/${truck.id}/menu">Edit Menu</a></li>
        <li><a href="/trucks/${truck.id}">View Public Page</a></li>
      </ul>
    </nav>
  </div>
  <div class="col-md-10">
