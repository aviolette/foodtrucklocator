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


  <%@ include file="../../include/core_js.jsp" %>
  <script type="text/javascript" src="/script/vendordash.js"></script>

  <script type="text/javascript">
    (function () {
      TruckMap.init();

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