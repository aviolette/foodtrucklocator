<%@ include file="vendorheader.jsp" %>
<script type="text/javascript"
        src="//maps.googleapis.com/maps/api/js?key=${googleApiKey}">
</script>

<div class="row">
  <div class="col-md-6">
    <h1>${truck.name} <a type="button" class="btn btn-default" aria-label="Edit" href="/vendor/settings/${truck.id}">
      <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
    </a></h1>
    <img src="${truck.previewIcon}" width="180" height="180"/>
    <p class="lead">${truck.description}</p>
  </div>
  <div class="col-md-6">
    <h3>Current Location</h3>
    <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;"></div>
  </div>
</div>
<div class="row">
  <div class="col-md-12">
    <%@ include file="../include/truck_schedule_widget.jsp" %>
  </div>
</div>
<div class="row">
  <div class="col-md-12">
    <h2>Beacons</h2>
    <c:choose>
      <c:when test="${empty(beacons)}">
        <p>Coming Soon!</p>
      </c:when>
      <c:otherwise>
        <table class="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Device Id</th>
              <th>Last Broadcast</th>
              <th>&nbsp;</th>
            </tr>
          </thead>
          <tbody>
          <c:forEach var="beacon" items="${beacons}">
            <tr>
              <td><a href="/vendor/beacons/${beacon.key}">${beacon.label}</a></td>
              <td>${beacon.deviceNumber}</td>
              <td><c:if test="${!empty(beacon.lastLocation)}"><ftl:location location="${beacon.lastLocation}" admin="false"/> at <joda:format value="${beacon.lastBroadcast}" style="MM"/></c:if></td>
              <td><button class="beacon-button btn <c:choose><c:when test="${beacon.enabled}">btn-danger</c:when><c:otherwise>btn-success</c:otherwise></c:choose>" id="beacon-button-${beacon.key}"><c:choose><c:when test="${beacon.enabled}">Disable</c:when><c:otherwise>Enable</c:otherwise></c:choose></button></td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>
</div>
<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript" src="/script/truck_edit_widget.js"></script>
<script src="/script/lib/typeahead.bundle.js"></script>
<script type="text/javascript">
  (function() {
    $(".beacon-button").click(function(e) {
      var $self = $(e.target);
      var item = $self.attr("id").substr(14);
      var action = $self.text().toLowerCase();
      $.ajax({
        url: "/services/beacons/" + item + "/" + action,
        type: 'POST',
        contentType: 'application/json',
        complete : function() {
        },
        success: function(e) {
          if (action == "disable") {
            $self.text("Enable");
            $self.removeClass("btn-danger");
            $self.addClass("btn-success");
          } else {
            $self.text("Disable");
            $self.addClass("btn-danger");
            $self.removeClass("btn-success");
          }
        }
      });
    });
    var map, markers = [], bounds = new google.maps.LatLngBounds();
    if (!(typeof google == "undefined")) {
      var markerLat = new google.maps.LatLng(41.8807438, -87.6293867);
      var myOptions = {
        center: markerLat,
        zoom: 14,
        maxZoom: 14,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      };
      map = new google.maps.Map(document.getElementById("map_canvas"),
          myOptions);
    }
    runEditWidget("${truck.id}", ${locations}, ${categories}, {
      addCallback: function(stop) {
      if (!map) {
        return;
      }
      var now = new Date().getTime();
      if (stop.startMillis <= now && stop.endMillis > now) {
        var marker = new google.maps.Marker({
          draggable: true,
          position: new google.maps.LatLng(stop.location.latitude, stop.location.longitude),
          map: map
        });
        markers.push(marker);
        bounds.extend(marker.getPosition());
        map.fitBounds(bounds);
      }
    }, refreshCallback: function() {
        $.each(markers, function(i, marker) {
          marker.setMap(null);
        });
        markers = [];
        bounds = new google.maps.LatLngBounds()
      },
      vendorEndpoints: true, hasCalendar: ${not empty(truck.calendarUrl)}});
  })();
</script>
<%@ include file="vendorfooter.jsp" %>
