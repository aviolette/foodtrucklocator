var FoodTruckLocator = function() {
  var _map = null,
      _appKey = null,
      _trucks = null,
      _markers = null,
      _center = null;

  function refreshViewData() {
    updateDistanceFromCurrentLocation();
    updateMap();
    updateTruckLists();
    displayWarningIfMarkersNotVisible();
  }

  function setCookie(name, value, days) {
    var expires;
    if (days) {
      var date = new Date();
      date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
      expires = "; expires=" + date.toGMTString();
    }
    else expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
  }

  function findLocation() {
    return _center;
  }

  function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0) == ' ') c = c.substring(1, c.length);
      if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
  }

  var Markers = function(map) {
    var markers = {}, lastLetter = 0, color = "";

    function buildIconURL(letter) {
      if (Modernizr.touch) {
        return "http://maps.google.com/mapfiles/marker.png";
      }
      var code = letter.charCodeAt(0);
      if (code > 90) {
        code = code - 26;
        color = "_orange"
      }
      letter = String.fromCharCode(code);
      return "http://www.google.com/mapfiles/marker" + color + letter + ".png"
    }

     this.clear = function() {
      color = "", lastLetter = 0;
      this.bounds = new google.maps.LatLngBounds();
      $.each(markers, function(key, marker) {
        marker.setMap(null);
      });
      markers = {};
    };

    this.add = function(stop) {
      if (markers[stop.location.name] == undefined) {
        var letterId = String.fromCharCode(65 + lastLetter);
        stop.marker = new google.maps.Marker({
          map: map,
          icon: buildIconURL(letterId),
          position: stop.position
        });
        stop.markerId = "marker" + color + letterId;
        markers[stop.location.name] = stop.marker;
        lastLetter++;
        this.bounds.extend(stop.position);
      } else {
        stop.marker = markers[stop.location.name];
      }
    };
  };

  var Clock = {
    now : function() {
      return new Date().getTime();
      //return 1366625176648;
    }
  };

  var Trucks = function(model) {
    this.stops = [];
    this.trucks = {};
    var self = this;
    for (var i=0; i < model["trucks"].length; i++) {
      this.trucks[model["trucks"][i]["id"]] = model["trucks"][i];
    }

    $.each(model["stops"], function(idx, item) {
      self.stops.push(buildStop(item));
    });

    function buildStop(stop) {
      return {
        stop : stop,
        truck : self.trucks[stop["truckId"]],
        location : model["locations"][stop["location"]-1],
        position : new google.maps.LatLng(model["locations"][stop["location"]-1].latitude,
            model["locations"][stop["location"]-1].longitude)
      }
    }

    this.all = function() {
      return this.stops;
    }

    this.updateDistanceFrom = function(location) {
      $.each(this.stops, function(idx, stop) {
        var distance = google.maps.geometry.spherical.computeDistanceBetween(location,
            stop.position , 3959);
        stop.distance = Math.round(distance * 100) / 100;
      });
    }

    this.allVisible = function() {
      var bounds = _map.getBounds(), visible = true;
      $.each(this.stops, function(idx, stop) {
        if (!bounds.contains(stop.position)) {
          visible = false;
        }
      });
      return visible;
    }

    this.openNow = function() {
      var now = Clock.now(), items = [];
      $.each(self.stops, function(idx, item) {
        if (item.stop["startMillis"] <= now && item.stop["endMillis"] > now && _map.getBounds().contains(item.position)) {
          items.push(item);
        }
      });
      return items;
    };

    this.openLater = function() {
      var now = Clock.now(), items = [];
      $.each(self.stops, function(idx, item) {
        if (item.stop["startMillis"] > now && _map.getBounds().contains(item.position)) {
          items.push(item);
        }
      });
      return items;
    }
  };

  function formatLocation(location) {
    if (/, Chicago, IL$/i.test(location)) {
      location = location.substring(0, location.length - 13);
    }
    return location;
  }

  function buildGroupTableRow(stop) {
    return "<tr><td><img src='" + stop.truck.iconUrl + "'/></td><td style='padding-left:10px'>" +
        stop.stop.startTime + " - <br/>" + stop.stop.endTime + "</td><td style='padding-left:10px'>" +
        "<strong>" + stop.truck.name + "</strong></td></tr>";
  }

  function buildInfoWindow(marker, stops) {
    var contentString = "<div class='infoWindowContent'><address class='locationName'>" +
        stops[0].location.name + "</address>";
    if (stops.distance != null) {
      contentString += "<p>" + stops[0].distance + " miles from your location</p>"
    }
    contentString = contentString + "<table><tbody>"
    $.each(stops, function(idx, stop) {
      contentString += buildGroupTableRow(stop);
    });
    contentString = contentString + "</tbody></table></div>";
    var infowindow = new google.maps.InfoWindow({
      content: contentString
    });

    google.maps.event.addListener(marker, 'click', function() {
      infowindow.open(_map, marker);
    });
  }


  function buildTruckList($truckList, stops) {
    $truckList.empty();
    var markerIds = [];
    var items = "<ul class='unstyled'>", lastIcon = null;
    var lastMarkerGroup;
    $.each(stops, function(idx, stop){
      var distance = stop.distance ? (" (" + stop.distance + " miles away) ") : "";
      var iconColumn = "";
      if (lastIcon != stop.marker.icon) {
        iconColumn = "<img id='" + stop.markerId + "'  src='" + stop.marker.icon + "'/>";
        lastMarkerGroup = {marker : stop.marker, id: stop.markerId, stops: [stop]};
        markerIds.push(lastMarkerGroup);
      } else {
        iconColumn = "<img style='visibility:hidden' src='" + stop.marker.icon + "'/>";
        lastMarkerGroup["stops"].push(stop);
      }
      lastIcon = stop.marker.icon;
      items +=   "<li style='padding-bottom:20px'>" +
          "<table><tr><td style=\"vertical-align: top;width:20px !important\">" + iconColumn + "</td><td style='width: 48px; vertical-align:top;padding-right:5px'>" +
          "<img src='" + stop.truck.iconUrl + "'/></td><td style='vertical-align:top'>" +
          stop.truck.name + "<br/>" +
          formatLocation(stop.location.name) + distance + "<br/>" +
          stop.stop.startTime + " - " + stop.stop.endTime +
          "</td></tr></table>" +
          "</li>";
    });
    $truckList.append(items + "</ul>");
    $.each(markerIds, function(idx, markerAndId) {
      if (markerAndId.marker.getAnimation() != null) {
        return;
      }
      buildInfoWindow(markerAndId.marker, markerAndId.stops);
      $("#" + markerAndId.id).click(function() {
        markerAndId.marker.setAnimation(google.maps.Animation.BOUNCE);
        setTimeout(function() {
          markerAndId.marker.setAnimation(null);
        }, 3000);
      });

    });
  }

  function updateTruckLists() {
    var location = findLocation();
    buildTruckList($("#nowTrucks"), sortByDistanceFromLocation(_trucks.openNow(), location));
    buildTruckList($("#laterTrucks"), sortByDistanceFromLocation(_trucks.openLater(), location));
  }

  function updateDistanceFromCurrentLocation() {
    _trucks.updateDistanceFrom(findLocation());
  }

  function sortByDistanceFromLocation(stops, location) {
    return stops.sort(function(a, b) {
      if (typeof a.distance == "undefined" || a.distance == null) {
        return 0;
      }
      return a.distance > b.distance ? 1 : ((a.distance == b.distance) ? 0 : -1);
    });
  }

  function updateMap() {
    _markers.clear();
    var currentLocation = findLocation();
    _markers.bounds.extend(currentLocation);
    // TODO: we're sorting in two locations...probably shouldn't do that.
    $.each(sortByDistanceFromLocation(_trucks.openNow(), currentLocation), function(idx, stop) {
      _markers.add(stop);
    });
    $.each(sortByDistanceFromLocation(_trucks.openLater(), currentLocation), function(idx, stop) {
      _markers.add(stop);
    });
//    _map.fitBounds(bounds);
  }

  function resize() {
    $("#map_canvas").height($(window).height() - $("#topBar").height());
    $("#sidebar").height($(window).height() - $("#topBar").height());
    $("#listContainer").height($(window).height() - $("#topBar").height());
  }

  function setupGlobalEventHandlers() {
    $("#mobileLink").click(function(e) {
      e.preventDefault();
      $("#mobileDialog").modal({ show: true, keyboard : true, backdrop: true});
    });
    $(window).resize(function() {
      resize();
    });
  }

  function displayWarningIfMarkersNotVisible() {
    if (_trucks.allVisible()) {
      $("#filteredWarning").css("display", "none");
    } else {
      $("#filteredWarning").css("display", "block");
    }
  }

  function findCenter(defaultCenter) {
    var lat = getCookie("map_center_lat"), lng = getCookie("map_center_lng");
    if (lat && lng) {
      return new google.maps.LatLng(lat, lng);
    }
    return defaultCenter;
  }

  function saveCenter(center) {
    setCookie("map_center_lat", center.lat());
    setCookie("map_center_lng", center.lng());
    _center = center;
  }

  function findZoom(defaultZoom) {
    var zoom = getCookie("zoom");
    if (zoom) {
      return parseInt(zoom);
    }
    return defaultZoom;
  }

  function saveZoom(zoom) {
    setCookie("zoom", zoom);
  }

  return {
    setModel : function(model) {
      _trucks = new Trucks(model);
      refreshViewData();
    },
    reload : function() {
      console.log("Reloading model...")
      var self = this;
      $.ajax({
        url : '/services/daily_schedule?appKey=' + _appKey,
        dataType: 'json',
        cache: false,
        error : function() {
          console.log("Failed to reload model");
        },
        success : function(data) {
          console.log("Successfully loaded model")
          self.setModel(data);
        }
      });
    },
    extend : function() {
      _map.fitBounds(_markers.bounds);
    },
    run : function(mobile, center, time, modelPayload, appKey) {
      var self = this;
      _appKey = appKey;
      _center = findCenter(center);
      resize();
      _map = new google.maps.Map(document.getElementById("map_canvas"), {
        zoom: findZoom(13),
        center: _center,
        maxZoom : 18,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      });
      _markers = new Markers(_map);
      google.maps.event.addListener(_map, 'center_changed', function() {
        saveCenter(_map.getCenter());
        displayWarningIfMarkersNotVisible();
      });
      var centerMarker = new google.maps.Marker({
        icon: "http://maps.google.com/mapfiles/arrow.png"
      });

      google.maps.event.addListener(_map, 'drag', function() {
        centerMarker.setMap(_map);
        centerMarker.setPosition(_map.getCenter());
      });

      google.maps.event.addListener(_map, 'dragend', function() {
        centerMarker.setMap(null);
        self.setModel(modelPayload);
      });

      google.maps.event.addListener(_map, 'zoom_changed', function() {
        saveZoom(_map.getZoom());
        self.setModel(modelPayload);
      });

      var listener = null;
      // just want to invoke this once, for when the map first loads
      listener = google.maps.event.addListener(_map, 'bounds_changed', function() {
        self.setModel(modelPayload);
        if (listener) {
          google.maps.event.removeListener(listener);
        }
      });
      setupGlobalEventHandlers();
      // reload the model every 5 minutes
      setInterval(function() {
        self.reload();
      }, 300000)
    }
  };
}();
