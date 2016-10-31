<%@ include file="vendorheader.jsp" %>

<script type="text/javascript"
        src="//maps.googleapis.com/maps/api/js?key=${googleApiKey}"></script>

<div class="row">
  <div class="col-md-12">
    <h2>${beacon.label}</h2>
  </div>
</div>
<div class="row">
  <div class="col-md-6">
    <dl>
      <dt>Device Number</dt>
      <dd>${beacon.deviceNumber}</dd>
      <dt>Last Location</dt>
      <dd><ftl:location location="${beacon.lastLocation}" admin="false"/></dd>
      <dt>Last Broadcast</dt>
      <dd><joda:format value="${beacon.lastBroadcast}" style="MM"/></dd>
      <dt>State</dt>
      <dd>${beacon.state}</dd>
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
                ${beacon.fuelLevelValue}%
            </div>
          </div>
        </dd>
      </c:if>
    </dl>


  </div>
  <div class="col-md-6">
    <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;"></div>
  </div>
</div>

<div class="row" style="margin-top:20px">
  <div class="col-md-12">
    <h3>Recent trips</h3>
    <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
    </div>
  </div>
</div>
<%@ include file="../include/core_js.jsp" %>

<c:if test="${!empty(beacon.lastLocation)}">
  <script>
    var lat = ${beacon.lastLocation.latitude}, lng = ${beacon.lastLocation.longitude};
    if (!(typeof google == "undefined")) {
      var markerLat = new google.maps.LatLng(lat, lng);
      var myOptions = {
        center: markerLat,
        zoom: 14,
        scrollwheel: false,
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
<%--
<script>
  $.ajax({
    url: "/services/beacons/${beacon.id}/trips",
    type: 'GET',
    dataType: 'json',
    success: function (beacons) {
      $.each(beacons.data.stops.reverse(), function(i, trip) {
        var name = trip.street + " (" + trip.stopType + ")";
        var $panelDiv = $("<div class='panel panel-default'><div class='panel-heading' " +
            "role='tab' id='heading" + i + "'><h4 class='panel-title'><a role='button' " +
            "data-toggle='collapse' data-parent='#accordion' href='#collapse" +
            i + "' aria-expanded='true' aria-controls='collapse" + i + "'>" + name + "</a></h4></div><div id='collapse" + i + "' class='panel-collapse collapse' role='tabpanel' aria-labelledby='heading" + i + "'> <div class='panel-body' id='panel-body-" + i +"'> </div> </div></div>");
        $("#accordion").append($panelDiv);
        var $panelBody = $("#panel-body-" + i);
        $panelBody.append("<dl><dt>Start Time</dt><dd>" + new Date(trip.beginDate) + "</dd></dl>")
      });
    }
  });
</script>
--%>

<%@ include file="vendorfooter.jsp" %>
