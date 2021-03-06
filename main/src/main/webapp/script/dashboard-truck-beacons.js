(function () {
  TruckMap.init();
  TruckMap.clear();
  var beaconData = JSON.parse($("#beaconData").text());
  var blacklist = JSON.parse($("#blacklist").text());

  function refreshBeacons() {

    $.ajax({
      url: "/services/trucks/" + beaconData.truckId + "/beacons",
      type: 'GET',
      dataType: 'json',
      success: function (beacons) {
        $("#beacons").empty();
        $.each(beacons, function (i, item) {
          TruckMap.addAnnotatedBeacon(item);

          var $tr = $("<tr></tr>");
          $tr.append("<td><a href='/admin/beacons/" + item.id + "'>" + item.label + "</a></td>");
          $tr.append("<td class='large-screen-only'>" + item.deviceNumber + "</td>");
          var locationBody = "";
          if (item.lastLocation) {
            locationBody = "<a href=\"/admin/locations/" + item.lastLocation.key + "\">" + item.lastLocation.shortenedName + "</a> at " + item.lastBroadcast;
          }
          if (item.hasWarning) {
            locationBody += "<div class='alert alert-danger mt-2'><span class='icon icon-warning'></span> " + item.warning +"</div>";
          }
          $tr.append("<td>" + locationBody + "</td>");
          $tr.append("<td class='large-screen-only'>" + item.lastModified + "</td>");
          $tr.append("<td>" + (item.parked ? "PARKED" : "MOVING") + "</td>");

          var $cb = $("<label class='switch'><input type='checkbox' class='beacon-button' id='beacon-button-" + item.id + "' " + (item.enabled ? "checked='checked'" : "") + "><span class='slider'></span></label>");
          var $td = $("<td></td>");
          $td.append($cb);
          $tr.append($td)
          $("#beacons").append($tr);
        });
        $(".beacon-button").click(function (e) {
          var $self = $(e.target);
          var item = $self.attr("id").substr(14);
          var action = $self.prop("checked") ? "enable" : "disable";
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
  }

  $.each(blacklist, function (i, location) {
    TruckMap.addBlacklisted(location);
  });

  refreshBeacons();

})();
