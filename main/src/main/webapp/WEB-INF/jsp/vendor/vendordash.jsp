<%@ include file="vendorheader.jsp" %>
<script type="text/javascript"
        src="//maps.googleapis.com/maps/api/js?key=${googleApiKey}">
</script>
<script type="text/javascript" src="/script/lib/spin.min.js"></script>
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
<h1>${truck.name}</h1>
<div class="row">
  <div class="col-md-12">
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
    <h2>Beacons </h2>
    <a href="/vendor/linxup/${truck.id}">(Edit Configuration)</a>
    <table class="table">
          <thead>
            <tr>
              <th>Name</th>
              <th class="large-screen-only">Device Id</th>
              <th>Last Broadcast</th>
              <th class="large-screen-only">Last Checked</th>
              <th>Status</th>
              <th>&nbsp;</th>
            </tr>
          </thead>
          <tbody id="beacons">
          </tbody>
        </table>
  </div>
</div>
<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript" src="/script/truck_edit_widgetv2.js"></script>
<script type="text/javascript" src="/script/vendordash.js"></script>
<script src="/script/lib/typeahead.bundle.js"></script>
<script type="text/javascript">
  (function() {
    TruckMap.init()

    function refreshBeacons() {
      $.ajax({
        url: "/services/trucks/${truck.id}/beacons",
        type: 'GET',
        dataType: 'json',
        success: function (beacons) {
          $("#beacons").empty();
          $.each(beacons, function(i, item) {
            TruckMap.addBeacon(item.lastLocation.latitude, item.lastLocation.longitude,
                item.enabled, item.parked, item.blacklisted);
            var $tr = $("<tr></tr>");
            $tr.append("<td>" + item.label + "</td>");
            $tr.append("<td class='large-screen-only'>" + item.deviceNumber + "</td>");
            if (item.lastLocation) {
              $tr.append("<td><a href=\"/locations/" + item.lastLocation.key + "\">" + item.lastLocation.name + "</a> at " + item.lastBroadcast + "</td>");
            }
            $tr.append("<td class='large-screen-only'>" + item.lastModified + "</td>");
            $tr.append("<td>" + (item.parked ? "PARKED" : "MOVING") + "</td>");

            var $button = $("<button class='beacon-button btn' id='beacon-button-" + item.id +"'>" + (item.enabled ? "Disable" : "Enable") + "</button>");
            if (item.enabled) {
              $button.addClass("btn-danger");
            } else {
              $button.addClass("btn-success");
            }
            var $td = $("<td></td>");
            $td.append($button);
            $tr.append($td)
            $("#beacons").append($tr);
          });
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
        }
      })
    }

    TruckScheduleWidget.init("${truck.id}", ${locations}, ${categories}, {
      addCallback: TruckMap.addStop,
      refreshCallback: function() {
        TruckMap.clear();
        refreshBeacons();
      },
      vendorEndpoints: true,
      hasCalendar: ${not empty(truck.calendarUrl)}
    });

    $.each(${blacklist}, function(i, location) {
      TruckMap.addBlacklisted(location);
    });

    refreshBeacons();

    setInterval(function() {
      console.log("Refreshing...");
      TruckScheduleWidget.refresh();
    }, 60000);


  })();
</script>
<%@ include file="vendorfooter.jsp" %>
