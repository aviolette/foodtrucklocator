<%@ include file="vendorheader.jsp" %>
<script type="text/javascript"
        src="//maps.googleapis.com/maps/api/js?key=${googleApiKey}"></script>

<div class="row">
  <div class="col-md-12">
    <h2>${beacon.label}</h2>
  </div>
</div>
<c:if test="${beacon.hasWarning}">

  <div class="alert alert-danger" role="alert">
    <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> ${beacon.warning}
  </div>
</c:if>
<div class="row">
  <div class="col-md-3">

    <div class="panel panel-info">
      <div class="panel-heading">
        <h3 class="panel-title">Last Broadcast</h3>
      </div>
      <div class="panel-body">
        <joda:format value="${beacon.lastBroadcast}" style="MM"/>
      </div>
    </div>
  </div>
  <div class="col-md-3">

    <div class="panel panel-info">
      <div class="panel-heading">
        <h3 class="panel-title">Last Location</h3>
      </div>
      <div class="panel-body">
        <ftl:location location="${beacon.lastLocation}"/>
      </div>
    </div>

  </div>
  <div class="col-md-3">

    <div class="panel panel-info">
      <div class="panel-heading">
        <h3 class="panel-title">Battery</h3>
      </div>
      <div class="panel-body">
        ${beacon.batteryCharge} V
      </div>
    </div>
  </div>
  <div class="col-md-3">

    <div class="panel panel-info">
      <div class="panel-heading">
        <h3 class="panel-title">Fuel</h3>
      </div>
      <div class="panel-body">
        ${beacon.fuelLevelValue}%
      </div>
    </div>

  </div>

</div>
<div class="row">
  <div class="col-md-12">
    <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;"></div>
  </div>
</div>

<div style="position:relative" style="right: 100px" id="accordion-spinner"></div>

<div class="row" style="margin-top:20px">
  <div class="col-md-12">
    <h3>Recent trips</h3>
    <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
    </div>
  </div>
</div>
<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript" src="/script/lib/spin.min.js"></script>

<c:if test="${!empty(beacon.lastLocation)}">
  <script type="text/javascript" src="/script/vendordash.js"></script>
  <script type="text/javascript">
    TruckMap.init();
    TruckMap.clear();
    TruckMap.addMarker({lat: ${beacon.lastLocation.latitude}, lng: ${beacon.lastLocation.longitude}});
  </script>
</c:if>
<script>
  (function () {
    var openInfoWindow = null, spinner = new Spinner();

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

    spinner.spin($("#accordion-spinner").get(0));

    $.ajax({
      url: "/services/beacons/${beacon.id}/trips",
      type: 'GET',
      dataType: 'json',
      complete: function () {
        spinner.stop();
      },
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
