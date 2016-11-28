<%@ include file="../truckHeader.jsp" %>

<script type="text/javascript"
        src="//maps.googleapis.com/maps/api/js?key=${googleApiKey}">
</script>
<script type="text/javascript" src="/script/lib/spin.min.js"></script>
<style type="text/css">
  @media (max-width: 990px) {
    .location-related {
      display: none;
    }
  }

  @media (min-width: 990px) {
    .location-related {
      display: block;
    }
  }
</style>

<div class="btn-toolbar">
  <div class="btn-group">
    <a class="btn btn-primary" href="/admin/trucks/${truck.id}/linxup_config">Setup</a>
  </div>
</div>

<c:if test="${!empty(linxupAccount)}">
  <div class="row">
    <div class="col-md-12">
      <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;" class="location-related"></div>
    </div>
  </div>

  <div class="row">
    <div class="col-md-12">
      <h2>Beacons </h2>
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

  <%@ include file="../../include/core_js.jsp" %>
  <script type="text/javascript" src="/script/vendordash.js"></script>

  <script type="text/javascript">
    (function () {
      TruckMap.init();
      TruckMap.clear();

      function refreshBeacons() {
        <c:if test="${!empty(linxupAccount)}">

        $.ajax({
          url: "/services/trucks/${truck.id}/beacons",
          type: 'GET',
          dataType: 'json',
          success: function (beacons) {
            $("#beacons").empty();
            $.each(beacons, function (i, item) {
              TruckMap.addBeacon(item.lastLocation.latitude, item.lastLocation.longitude,
                  item.enabled, item.parked, item.blacklisted, item.direction);

              var $tr = $("<tr></tr>");
              $tr.append("<td><a href='/admin/trucks/${truck.id}/beacons/" + item.id + "'>" + item.label + "</a></td>");
              $tr.append("<td class='large-screen-only'>" + item.deviceNumber + "</td>");
              if (item.lastLocation) {
                $tr.append("<td><a href=\"/admin/locations/" + item.lastLocation.key + "\">" + item.lastLocation.shortenedName + "</a> at " + item.lastBroadcast + "</td>");
              }
              $tr.append("<td class='large-screen-only'>" + item.lastModified + "</td>");
              $tr.append("<td>" + (item.parked ? "PARKED" : "MOVING") + "</td>");

              var $button = $("<button class='beacon-button btn' id='beacon-button-" + item.id + "'>" + (item.enabled ? "Disable" : "Enable") + "</button>");
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
            $(".beacon-button").click(function (e) {
              var $self = $(e.target);
              var item = $self.attr("id").substr(14);
              var action = $self.text().toLowerCase();
              $.ajax({
                url: "/services/beacons/" + item + "/" + action,
                type: 'POST',
                contentType: 'application/json',
                complete: function () {
                },
                success: function (e) {
                  if (action == "disable") {
                    $self.text("Enable");
                    $self.removeClass("btn-danger");
                    $self.addClass("btn-success");
                  } else {
                    $self.text("Disable");
                    $self.addClass("btn-danger");
                    $self.removeClass("btn-success");
                  }
                  refreshBeacons();
                }
              });
            });

          }
        });
        </c:if>
      }

      $.each(${blacklist}, function (i, location) {
        TruckMap.addBlacklisted(location);
      });

      refreshBeacons();

    })();
  </script>


</c:if>


<%@ include file="../truckFooter.jsp" %>