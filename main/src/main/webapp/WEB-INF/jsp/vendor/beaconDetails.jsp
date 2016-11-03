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
<script>
  (function () {
    var openInfoWindow = null;

    function directionValue(direction) {
      if ((direction >= 0 && direction <= 22) || (direction >= 338 && direction <= 360)) {
        return "N";
      } else if (direction >= 23 && direction <= 67) {
        return "NE";
      } else if (direction >= 68 && direction <= 112) {
        return "E";
      } else if (direction >= 113 && direction <= 157) {
        return "SE";
      } else if (direction >= 168 && direction <= 202) {
        return "S";
      } else if (direction >= 203 && direction <= 247) {
        return "SW";
      } else if (direction >= 248 && direction <= 292) {
        return "W";
      } else {
        return "NW";
      }
    }

    function buildInfoWindow(marker, map, contentValue) {
      var $content = $("<div>"),
          $masterDiv = $("<div>");
      $masterDiv.append($("<div>" + contentValue + "</div>"));
      $content.append($masterDiv);
      var infowindow = new google.maps.InfoWindow({
        content: $content.html()
      });
      google.maps.event.addListener(marker, 'click', function () {
        if (openInfoWindow) {
          openInfoWindow.close();
        }
        openInfoWindow = infowindow;
        infowindow.open(map, marker);
      });
    }

    $.ajax({
      url: "/services/beacons/${beacon.id}/trips",
      type: 'GET',
      dataType: 'json',
      success: function (trips) {
        $.each(trips.reverse(), function (i, trip) {
          var name = trip.name;
          var $panelDiv = $("<div class='panel panel-default'><div class='panel-heading' " +
              "role='tab' id='heading" + i + "'><h4 class='panel-title'><a role='button' " +
              "data-toggle='collapse' data-parent='#accordion' href='#collapse" +
              i + "' aria-expanded='true' aria-controls='collapse" + i + "'>" + name + "</a></h4></div><div id='collapse" + i + "' class='panel-collapse collapse' role='tabpanel' aria-labelledby='heading" + i + "'> <div class='panel-body' id='panel-body-" + i + "'> </div> </div></div>");
          $("#accordion").append($panelDiv);
          var $panelBody = $("#panel-body-" + i);
          $panelBody.append("<div id='map_canvas-" + i + "' style='width:100%; height:300px; padding-bottom:20px;'></div>");
          $("#collapse" + i).on("shown.bs.collapse", function () {
            var startPosition = new google.maps.LatLng(trip.start.latitude, trip.start.longitude);
            var endPosition = new google.maps.LatLng(trip.end.latitude, trip.end.longitude);
            var myOptions = {
              center: startPosition,
              zoom: 14,
              scrollwheel: false,
              mapTypeId: google.maps.MapTypeId.ROADMAP
            };
            var map = new google.maps.Map(document.getElementById("map_canvas-" + i),
                myOptions);
            bounds = new google.maps.LatLngBounds();

            var marker = new google.maps.Marker({
              draggable: false,
              position: startPosition,
              map: map

            });
            bounds.extend(startPosition);
            bounds.extend(endPosition);
            buildInfoWindow(marker, map, "<h4>" + trip.start.name + "</h4><br/><strong>" + trip.startTimeValue + "</strong>");

            marker = new google.maps.Marker({
              draggable: false,
              position: endPosition,
              map: map

            });
            buildInfoWindow(marker, map, "<h4>" + trip.end.name + "</h4><br/><strong>" + trip.endTimeValue + "</strong>");

            var flightPath = new google.maps.Polyline({
              path: trip.positions,
              geodesic: true,
              strokeColor: '#FF0000',
              strokeOpacity: 1.0,
              strokeWeight: 4
            });
            flightPath.setMap(map);

            var firstPos = true;
            $.each(trip.positions, function (j, pos) {
              if (firstPos) {
                firstPos = false;
                return;
              }
              if (pos.speed === 0) {
                return;
              }
              bounds.extend(pos);
              var arrow = new google.maps.Marker({
                position: pos,
                icon: {
                  path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW,
                  rotation: pos.direction,
                  strokeColor: "red",
                  scale: 3
                },
                draggable: false,
                map: map
              });
              buildInfoWindow(arrow, map, "Speed: " + pos.speed + "mph<br/>Direction: " + directionValue(pos.direction) + "<br/>Time: " + pos.time);
            });
            map.fitBounds(bounds);

          });
        });
      }
    });
  })();
</script>

<%@ include file="vendorfooter.jsp" %>
