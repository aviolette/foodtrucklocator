var FoodTruckLocator = function() {
  var _map = null,
      _trucks = null,
      _markers = null,
      _center = null;

  function setCookie(name, value, days) {
    if (days) {
      var date = new Date();
      date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
      var expires = "; expires=" + date.toGMTString();
    }
    else var expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
  }

  function findLocation() {
    var lat = getCookie("latitude"), lng = getCookie("longitude");
    if (lat && lng) {
      return new google.maps.LatLng(lat, lng);
    } else {
      return _center;
    }
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
    var markers = {}, lastLetter = 0;

    function buildIconURL(letter) {
      if (Modernizr.touch) {
        return "http://maps.google.com/mapfiles/marker.png";
      }
      var code = letter.charCodeAt(0)
      var color = "";
      if (code > 90) {
        code = code - 26;
        color = "_orange"
      }
      letter = String.fromCharCode(code);
      return "http://www.google.com/mapfiles/marker" + color + letter + ".png"
    }

    this.clear = function() {
      $.each(markers, function(key, marker) {
        marker.setMap(null);
      });
      markers = {};
    };

    this.add = function(stop) {
      if (markers[stop.location.name] == undefined) {
        stop.marker = new google.maps.Marker({
          map: map,
          icon: buildIconURL(String.fromCharCode(65 + lastLetter)),
          position: stop.position
        });
        markers[stop.location.name] = stop.marker;
        lastLetter++;
      } else {
        stop.marker = markers[stop.location.name];
      }
    }
  };

  var Clock = {
    now : function() {
      return new Date().getTime();
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

    this.nowOrLater = function() {
      var now = Clock.now();
      return $.grep(this.stops, function(item) {
        return item.stop["endMillis"] > now;
      });
    }

    this.updateDistanceFrom = function(location) {
      $.each(this.stops, function(idx, stop) {
        var distance = google.maps.geometry.spherical.computeDistanceBetween(location,
            stop.position , 3959);
        stop.distance = Math.round(distance * 100) / 100;
      });
    }

    this.openNow = function() {
      var now = Clock.now(), items = [];
      $.each(self.stops, function(idx, item) {
        if (item.stop["startMillis"] <= now && item.stop["endMillis"] > now) {
          items.push(item);
        }
      });
      return items;
    };

    this.openLater = function() {
      var now = Clock.now(), items = [];
      $.each(self.stops, function(idx, item) {
        if (item.stop["startMillis"] > now) {
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

  function buildTruckList($truckList, stops) {
    $truckList.empty();
    var items = "<ul class='unstyled'>", lastIcon = null;
    $.each(stops, function(idx, stop){
      var distance = stop.distance ? (" (" + stop.distance + " miles away) ") : "";
      var iconColumn = "";
      if (lastIcon != stop.marker.icon) {
        iconColumn = "<img src='" + stop.marker.icon + "'/>";
      } else {
        iconColumn = "<img style='visibility:hidden' src='" + stop.marker.icon + "'/>";
      }
      lastIcon = stop.marker.icon;
      items +=   "<li style='padding-bottom:20px'>" +
          "<table><tr><td>" + iconColumn + "</td><td style='vertical-align:top;padding-right:5px'>" +
          "<img src='" + stop.truck.iconUrl + "'/></td><td style='vertical-align:top'>" +
          stop.truck.name + "<br/>" +
          formatLocation(stop.location.name) + distance + "<br/>" +
          stop.stop.startTime + " - " + stop.stop.endTime +
          "</td></tr></table>" +
          "</li>";
    });
    $truckList.append(items + "</ul>");
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
    // TODO: we're sorting in two locations...probably shouldn't do that.
    $.each(sortByDistanceFromLocation(_trucks.openNow(), findLocation()), function(idx, stop) {
      _markers.add(stop);
//      bounds.extend(objectOnMap.position.latLng);
    });
    $.each(sortByDistanceFromLocation(_trucks.openLater(), findLocation()), function(idx, stop) {
      _markers.add(stop);
//      bounds.extend(objectOnMap.position.latLng);
    });
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
    $("#settingsLink").click(function(e) {
      e.preventDefault();
      var locationName = getCookie("locationName");
      if (Modernizr.geolocation) {
        $("#useGPS").prop("checked", "true" == getCookie("useGPS"));
      } else {
        $("#useGPSLabel").css("display:none");
        $("#useGPS").prop("checked", false);
      }
      $("#locationName").attr("value", locationName);
      $("#settingsDialog").modal({show: true, keyboard : true, backdrop: true});
    });
    $("#useGPS").click(function(e) {

    });
    $("#saveSettingsButton").click(function(e) {
      e.preventDefault();
      var existingGPS = getCookie("useGPS"),
          existingLocationName = getCookie("locationName"),
          locationName = $("#locationName").attr("value"),
          useGPS = $("#useGPS").checked;

      if (existingGPS != useGPS && useGPS) {
        navigator.geolocation.getCurrentPosition(function(position) {
          setCookie("latitude", position.coords.latitude)
          setCookie("longitude", position.coords.longitude);
          $("#settingsDialog").modal("hide");
        });
      } else if (existingLocationName != locationName && !useGPS) {

      } else {
        $("#settingsDialog").modal("hide");
      }
    });
  }

  return {
    setModel : function(model) {
      _trucks = new Trucks(model);
      updateDistanceFromCurrentLocation();
      updateMap();
      updateTruckLists();
    },
    reload : function() {
      // TODO: implement logic to reload model
    },
    run : function(mobile, center, time, modelPayload) {
      var self = this;
      _center = center;
      resize();
      _map = new google.maps.Map(document.getElementById("map_canvas"), {
        zoom: 13,
        center: center,
        maxZoom : 18,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      });
      _markers = new Markers(_map);
      setupGlobalEventHandlers();
      self.setModel(modelPayload);
    }
  };
}();
