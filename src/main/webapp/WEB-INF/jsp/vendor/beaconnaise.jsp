<%@ include file="vendorheader.jsp" %>

<div>
  Setting up beacon for truck: <strong>${truck.name}</strong>
</div>

<label class="checkbox"><input type="checkbox" id="beaconButton"> Beacon enabled</label>

<%@ include file="../include/core_js.jsp" %>

<script type="text/javascript">
  var $beaconButton = $("#beaconButton");

  function runBeacon() {
    if ($beaconButton.is(":checked")) {
      $("#flash").css("display","block");
      $("#flash").html("Detecting location...");
      navigator.geolocation.getCurrentPosition(function(position) {
        var beaconData = {
          truckId : '${truck.id}',
          location : { latitude : position.coords.latitude, longitude : position.coords.longitude }
        };
        $("#flash").html("Success");
        $.ajax({
          url: "/services/beacon",
          type: 'POST',
          contentType: 'application/json',
          data: JSON.stringify(beaconData),
          complete: function (e) {
            setTimeout(runBeacon, 60000)
          },
          error: function (e) {
          },
          success: function (e) {
          }
        });
      }, function(error) {
        $("#flash").html("Failed");
        setTimeout(runBeacon, 10000);
      }, { enableHighAccuracy : false, timeout : 50000, maximumAge: 0});
    } else {
      // disable the beacon
    }
  }

  $beaconButton.click(function(e) {
    runBeacon();
  });

</script>

<%@ include file="vendorfooter.jsp" %>
