<%@ include file="vendorheader.jsp" %>

<script type="text/javascript"
        src="//maps.googleapis.com/maps/api/js?key=${googleApiKey}"></script>

<div class="row">
  <div class="col-md-6">
    <dl>
      <dt>Name</dt>
      <dd>${beacon.label}</dd>
      <dt>Device Number</dt>
      <dd>${beacon.deviceNumber}</dd>
      <c:if test="${!empty(beacon.batteryCharge)}">
        <dt>Battery Charge</dt>
        <dd>${beacon.batteryCharge}</dd>
      </c:if>
      <c:if test="${!empty(beacon.fuelLevel)}">
        <dt>Fuel Level</dt>
        <dd>
          <div class="progress">
            <div
                class="progress-bar <c:choose><c:when test="${beacon.fuelLevelValue < 10}">progress-bar-danger</c:when><c:when test="${beacon.fuelLevelValue < 33}">progress-bar-warning</c:when><c:otherwise>progress-bar-success</c:otherwise></c:choose>"
                role="progressbar" aria-valuenow="${beacon.fuelLevelValue}" aria-valuemin="0" aria-valuemax="100"
                style="width: ${beacon.fuelLevelValue}%">
              <span class="sr-only">${beacon.fuelLevelValue}% full</span>
            </div>
          </div>
        </dd>
      </c:if>
    </dl>


  </div>
  <div class="col-md-6">
    <p>Last broadcast received at <joda:format value="${beacon.lastBroadcast}" style="MM"/>
      at ${beacon.lastLocation.name}</p>
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


<%@ include file="vendorfooter.jsp" %>
