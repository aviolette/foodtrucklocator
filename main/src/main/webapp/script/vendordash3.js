var TruckMap = function() {
  var MILES_TO_METERS = 1609.34;
  var map, markers = [], bounds, openInfoWindow;

  function nearest(direction) {
    var pos = 45 * parseInt(direction / 45);
    var upper = pos + 45;
    if (upper - direction < direction - pos) {
      if (upper === 360) {
        return 0;
      }
      return upper;
    }
    return pos;
  }

  function buildBeaconWindow(marker, map, beacon) {
    var $content = $("<div>"), $topDiv = $("<div>");
    var $header = $("<div><h4>" + beacon.label + "</h4></div>");
    var $body = $("<div>");
    $body.append($("<table style='width:100%'>" +
        "<tbody><tr>" +
        "<td class='align-bottom'><span class='glyphicons glyphicons-gas-station'></span>&nbsp;" + beacon.fuelLevel + "</td>" +
        "<td><span class='glyphicons glyphicons-battery-75'></span>&nbsp;" + beacon.battery +"V</td>" +
        "<td><span class='glyphicons glyphicons-dashboard'></span>&nbsp" + beacon.lastSpeedInMPH +" MPH</td>" +
        "</tr></tbody></table>"));
    $body.append($("<p class='mt-0'><span class='glyphicons glyphicons-map-marker'></span>&nbsp;" +
        "<a target='_blank' href='/locations/" + beacon.lastLocation.key + "'>" + beacon.lastLocation.name + "</a></p>"));
    $body.append($("<p class='mt-0 mb-0'>Last broadcast " + beacon.sinceLastUpdate + " ago at " + beacon.lastBroadcast + " </p>"));
    $topDiv.append($header);
    $topDiv.append($body);
    $content.append($topDiv);
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

  function buildInfoWindow(marker, map, stop) {
    var $content = $("<div>"),
        $masterDiv = $("<div>");
    $masterDiv.append($("<h4>" + stop.location.name + "<h4>"));
    $masterDiv.append($("<div>Estimated departure: " + stop.endTime + "</div>"));
    if (stop.notes.length > 0) {
      $masterDiv.append("<h5>Notes</h5>");
      var $notesList = $("<ul></ul>");
      $.each(stop.notes, function(i, note) {
        $notesList.append($("<li>" + note + "</li>"));
      });
      $masterDiv.append($notesList);
    }
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

  return {
    init : function() {
      if (typeof google == "undefined") {
        return;
      }
      bounds = new google.maps.LatLngBounds();
      var markerLat = new google.maps.LatLng(41.8807438, -87.6293867);
      var myOptions = {
        center: markerLat,
        zoom: 14,
        scrollwheel: false,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      };
      map = new google.maps.Map(document.getElementById("map_canvas"),
          myOptions);
    },
    addBlacklisted: function(location) {
      if (typeof google == "undefined") {
        return;
      }
      var markerLat = new google.maps.LatLng(location.latitude, location.longitude);
      circle = new google.maps.Circle({
        radius: location.radius * MILES_TO_METERS,
        center: markerLat,
        map: map
      });
    },
    addAnnotatedBeacon: function(beacon) {
      if (typeof google == "undefined") {
        return;
      }
      var latLng = new google.maps.LatLng(beacon.lastLocation.latitude, beacon.lastLocation.longitude);
      var marker, parked = beacon.parked, blacklisted = beacon.blacklisted, enabled = beacon.enabled;
      var icon = "//maps.google.com/mapfiles/marker_green.png";
      if (beacon.truckOwnerId === "beaversdonuts") {
        icon = "https://storage.googleapis.com/truckicons/map_markers/" + beacon.truckOwnerId + "-" + nearest(beacon.direction) + ".png";
      }
      if (parked && enabled && !blacklisted) {
        if (beacon.truckOwnerId !== "beaversdonuts") {
          icon = "//maps.google.com/mapfiles/marker_green.png"
        }
        marker = new google.maps.Marker({
          draggable: false,
          position: latLng,
          icon: icon,
          map: map
        });
      } else if(parked) {
        if (beacon.truckOwnerId !== "beaversdonuts") {
          icon = "//maps.google.com/mapfiles/marker_grey.png";
        }
        marker = new google.maps.Marker({
          draggable: false,
          position: latLng,
          icon: icon,
          map: map
        });
      } else {
        if (beacon.truckOwnerId !== "beaversdonuts") {
          icon = {
            path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW,
            rotation: beacon.direction,
            strokeColor: "red",
            scale: 3
          };
        }
        marker = new google.maps.Marker({
          position: latLng,
          icon: icon,
          draggable: false,
          map: map
        });
      }
      markers.push(marker);
      buildBeaconWindow(marker, map, beacon);
      bounds.extend(marker.getPosition());
      map.fitBounds(bounds);
    },
    addBeacon: function (lat, lng, enabled, parked, blacklisted, direction) {
      if (typeof google == "undefined") {
        return;
      }
      var latLng = new google.maps.LatLng(lat, lng);
      var marker;
      if (parked && enabled && !blacklisted) {
        return;
      } else if(parked) {
        marker = new google.maps.Marker({
          draggable: false,
          position: latLng,
          icon: "//maps.google.com/mapfiles/marker_grey.png",
          map: map
        });
      } else {
        marker = new google.maps.Marker({
          position: latLng,
          icon: {
            path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW,
            rotation: direction,
            strokeColor: "red",
            scale: 3
          },
          draggable: false,
          map: map
        });
      }
      markers.push(marker);
      bounds.extend(marker.getPosition());
      map.fitBounds(bounds);
    },
    addStop: function(stop) {
      if (typeof google == "undefined") {
        return;
      }
      var now = new Date().getTime();
      if (stop.startMillis <= now && stop.endMillis > now) {
        var pos = new google.maps.LatLng(stop.location.latitude, stop.location.longitude);
        var marker = new google.maps.Marker({
          draggable: true,
          position: pos,
          icon: "//maps.google.com/mapfiles/marker_green.png",
          map: map
        });
        markers.push(marker);
        bounds.extend(marker.getPosition());
        buildInfoWindow(marker, map, stop);
        map.fitBounds(bounds);
      }
    },
    addMarker: function (pos) {
      if (typeof google == "undefined") {
        return;
      }
      var marker = new google.maps.Marker({
        draggable: true,
        position: pos,
        icon: "//maps.google.com/mapfiles/marker_green.png",
        map: map
      });
      markers.push(marker);
      bounds.extend(marker.getPosition());
      map.fitBounds(bounds);
    },
    clear: function() {
      if (typeof google == "undefined") {
        return;
      }
      $.each(markers, function(i, marker) {
        marker.setMap(null);
      });
      markers = [];
      // TODO: extend around beacons
      bounds = new google.maps.LatLngBounds();
      bounds.extend(new google.maps.LatLng(41.8807438, -87.6293867));
    }
  };
}();

