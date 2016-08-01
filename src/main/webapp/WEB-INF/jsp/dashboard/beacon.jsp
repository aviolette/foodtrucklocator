<%@include file="dashboardHeaderBS3.jsp" %>
<script type="text/javascript"
        src="//maps.googleapis.com/maps/api/js?key=${googleApiKey}"></script>

<div class="row">
  <div class="col-md-6">
    <dl>
      <dt>Name</dt>
      <dd>${beacon.label}</dd>
      <dt>Device Number</dt>
      <dd>${beacon.deviceNumber}</dd>
    </dl>

    <form action="" method="post">
      <div class="form-group">
        <label for="associated-truck">Associated Food Truck</label>
        <%--suppress CheckTagEmptyBody --%>
        <select id="associated-truck" name="associatedTruck">
          <c:forEach var="truck" items="${trucks}">
            <option value="${truck.id}" <c:if test="${beacon.truckOwnerId == truck.id}">selected</c:if>>${truck.name}</option>
          </c:forEach>
        </select>

      </div>
      <div class="form-group">
        <label>
          <input name="enabled" type="checkbox" <c:if test="${beacon.enabled}">checked</c:if>> Enabled
        </label>
      </div>
      <button type="submit" class="btn btn-primary">Update</button>
    </form>


  </div>
  <div class="col-md-6">
    <p>Last broadcast received at <joda:format value="${beacon.lastBroadcast}" style="MM"/> at ${beacon.lastLocation.name}</p>
    <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;"></div>
  </div>
</div>



<c:if test="${!empty(beacon.lastLocation)}">
  <script>
  var lat = ${beacon.lastLocation.latitude}, lng = ${beacon.lastLocation.longitude};
  if (!(typeof google == "undefined")) {
    var markerLat = new google.maps.LatLng(lat, lng);
    var myOptions = {
      center: markerLat,
      zoom: 14,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var map = new google.maps.Map(document.getElementById("map_canvas"),
        myOptions);

    var marker = new google.maps.Marker({
      draggable: true,
      position: markerLat,
      map: map
    });
  }
  </script>
</c:if>

${beacon.lastModified}

<%@include file="dashboardFooterBS3.jsp" %>