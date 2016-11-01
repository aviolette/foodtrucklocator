var TruckMap = function() {
  var MILES_TO_METERS = 1609.34;
  var map, markers = [], bounds = new google.maps.LatLngBounds(),openInfoWindow;
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
      var markerLat = new google.maps.LatLng(location.latitude, location.longitude);
      circle = new google.maps.Circle({
        radius: location.radius * MILES_TO_METERS,
        center: markerLat,
        map: map
      });
    },
    addBeacon: function (lat, lng, enabled, parked, blacklisted, direction) {
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
    clear: function() {
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

