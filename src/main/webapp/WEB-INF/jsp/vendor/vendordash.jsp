<%@ include file="vendorheader.jsp" %>
<script type="text/javascript"
        src="//maps.googleapis.com/maps/api/js?key=${googleApiKey}">
</script>
<style type="text/css">
  @media (max-width:990px) {
    .location-related {
      display:none;
    }
  }

  @media (min-width:990px) {
    .location-related {
      display:block;
    }
  }
</style>
<div class="row">
  <div class="col-md-6">
    <h1>${truck.name} <a type="button" class="btn btn-default" aria-label="Edit" href="/vendor/settings/${truck.id}">
      <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
    </a></h1>
    <img src="${truck.previewIcon}" width="180" height="180"/>
    <p class="lead">${truck.description}</p>
  </div>
  <div class="col-md-6">
    <h3 class="location-related">Current Location</h3>
    <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;" class="location-related"></div>
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
              <th>Last Checked</th>
              <th>Status</th>
              <th>&nbsp;</th>
            </tr>
          </thead>
          <tbody>
          <c:forEach var="beacon" items="${beacons}">
            <tr>
              <td>${beacon.label}</td>
              <td>${beacon.deviceNumber}</td>
              <td><c:if test="${!empty(beacon.lastLocation)}"><ftl:location location="${beacon.lastLocation}" admin="false"/> at <joda:format value="${beacon.lastBroadcast}" style="MM"/></c:if></td>
              <td><joda:format value="${beacon.lastModified}" style="MM"/></td>
              <td><c:choose><c:when test="${beacon.parked}">PARKED</c:when><c:otherwise>MOVING</c:otherwise></c:choose></td>
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
<script type="text/javascript" src="/script/truck_edit_widgetv2.js"></script>
<script type="text/javascript" src="/script/vendordash.js"></script>
<script src="/script/lib/typeahead.bundle.js"></script>
<script type="text/javascript">
  (function() {
    TruckMap.init()

    TruckScheduleWidget.init("${truck.id}", ${locations}, ${categories}, {
      addCallback: TruckMap.addStop,
      refreshCallback: function() {
        TruckMap.clear();
        refreshBeacons();
      },
      vendorEndpoints: true,
      hasCalendar: ${not empty(truck.calendarUrl)}});

    $.each(${blacklist}, function(i, location) {
      TruckMap.addBlacklisted(location);
    });

    function refreshBeacons() {
      // TODO: this should actually be done by AJAX so that we can refresh this list without refreshing the page...
      <c:forEach var="beacon" items="${beacons}">
      TruckMap.addBeacon(${beacon.lastLocation.latitude}, ${beacon.lastLocation.longitude}, ${beacon.enabled}, ${beacon.parked});
      </c:forEach>
    }
    refreshBeacons();

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
          TruckScheduleWidget.refresh();
          refreshBeacons();
        }
      });
    });


  })();
</script>
<%@ include file="vendorfooter.jsp" %>
