var FoodTruckLocator = function () {
  var _map = null,
      _appKey = null,
      _trucks = null,
      _markers = null,
      _defaultCityRegex = null,
      _defaultCityLength = 0,
      _openInfowindow = null,
      _showFutureStops = false,
      _center = null;

  function refreshViewData() {
    updateDistanceFromCurrentLocation();
    updateMap();
    buildInfoWindows();
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

  function findLocation() {
    return _center;
  }

  var Markers = function () {
    var markers = {};

    this.clear = function () {
      this.bounds = new google.maps.LatLngBounds();
      $.each(markers, function (key, marker) {
        marker.setMap(null);
      });
      markers = {};
    };

    this.add = function (stop) {
      if (markers[stop.location.name] == undefined) {
        stop.marker = new google.maps.Marker({
          map: _map,
          icon: "http://www.google.com/mapfiles/marker.png",
          position: stop.position
        });
        markers[stop.location.name] = stop.marker;
        this.bounds.extend(stop.position);
      } else {
        stop.marker = markers[stop.location.name];
      }
    };
  };

  var Clock = {
    now: function () {
      return new Date().getTime();
    }
  };

  var Trucks = function (model) {
    this.stops = [];
    this.trucks = {};
    var self = this;
    for (var i = 0; i < model["trucks"].length; i++) {
      this.trucks[model["trucks"][i]["id"]] = model["trucks"][i];
    }

    $.each(model["stops"], function (idx, item) {
      self.stops.push(buildStop(item));
    });

    function buildStop(stop) {
      return {
        stop: stop,
        truck: self.trucks[stop["truckId"]],
        location: model["locations"][stop["location"] - 1],
        position: new google.maps.LatLng(model["locations"][stop["location"] - 1].latitude,
            model["locations"][stop["location"] - 1].longitude)
      }
    }

    this.findTruck = function (truckId) {
      return this.trucks[truckId];
    };

    this.findStopsForTruck = function (truckId) {
      var items = [];
      $.each(self.stops, function (idx, item) {
        if (item.stop["truckId"] == truckId) {
          items.push(item);
        }
      });
      return items;
    };

    this.fromNowOn = function() {
      var now = Clock.now(), items = [];
      $.each(self.stops, function (idx, item) {
        if ((item.stop["startMillis"] <= now && item.stop["endMillis"] > now) || item.stop["startMillis"] > now) {
          items.push(item);
        }
      });
      return items;
    };

    this.updateDistanceFrom = function (location) {
      $.each(this.stops, function (idx, stop) {
        var distance = google.maps.geometry.spherical.computeDistanceBetween(location,
            stop.position, 3959);
        stop.distance = Math.round(distance * 100) / 100;
      });
    };

    this.hasActive = function () {
      return self.stops.length > 0;
    };

    this.openNow = function () {
      var now = Clock.now(), items = [];
      $.each(self.stops, function (idx, item) {
        if (item.stop["startMillis"] <= now && item.stop["endMillis"] > now) {
          items.push(item);
        }
      });
      return items;
    };
  };

  function formatLocation(location) {
    if (_defaultCityRegex.test(location)) {
      location = location.substring(0, location.length - _defaultCityLength - 2);
    }
    return location;
  }

  function buildGroupTableRow(stop) {
    return "<div class='media'><a href='/trucks/" + stop.truck.id + "' class='pull-left'><img src='" + stop.truck.iconUrl + "'/></a><div class='media-body' >" +
        "<div><strong>" + stop.truck.name + "</strong></div><div>" + buildTimeRange(stop.stop, Clock.now()) + " </div></div></div>";
  }

  function buildInfoWindow(marker, stops) {
    var contentString = "<div class='infoWindowContent'><h4><a href='/locations/" + stops[0].location.key + "'>" +
        formatLocation(stops[0].location.name) + "</a></h4>";
    if (stops.distance != null) {
      contentString += "<p>" + stops[0].distance + " miles from your location</p>"
    }
    contentString = contentString + "<div class='media-list' style='margin:10px'>"
    $.each(stops, function (idx, stop) {
      contentString += buildGroupTableRow(stop);
    });
    contentString = contentString + "</div></div>";
    var infowindow = new google.maps.InfoWindow({
      content: contentString
    });

    google.maps.event.addListener(marker, 'click', function () {
      if (_openInfowindow) {
        _openInfowindow.close();
      }
      _openInfowindow = infowindow;
      infowindow.open(_map, marker);
    });
  }

  function buildTimeRange(stop, time) {
    if (stop.startMillis < time && stop.endMillis > time) {
      return "Est. departure time: " + stop.endTime;
    } else {
      return stop.startTime + " - " + stop.endTime;
    }
  }

  function groupByStops(stops) {
    var markers = [], lastLocation = null, lastMarkerGroup;
    $.each(stops, function (idx, stop) {
      if (lastLocation != stop.location.name) {
        var foundItems = $.grep(markers, function (item) {
          return item.stops[0].location.name == stop.location.name;
        });
        if (foundItems.length == 0) {
          lastMarkerGroup = {marker: stop.marker, id: stop.markerId, stops: [stop]};
        } else {
          lastMarkerGroup = foundItems[0];
          lastMarkerGroup["stops"].push(stop);
        }
        markers.push(lastMarkerGroup);
      } else {
        lastMarkerGroup["stops"].push(stop);
      }
      lastLocation = stop.location.name;
    });
    return markers;
  }

  function buildInfoWindows() {
    $.each(groupByStops(_showFutureStops ? _trucks.fromNowOn() : _trucks.openNow()), function (idx, markerAndId) {
      buildInfoWindow(markerAndId.marker, markerAndId.stops);
    });
  }

  function updateDistanceFromCurrentLocation() {
    if (_trucks) {
      _trucks.updateDistanceFrom(findLocation());
    }
  }

  function updateMap() {
    _markers.clear();
    var currentLocation = findLocation();
    _markers.bounds.extend(currentLocation);
    $.each(_showFutureStops ? _trucks.fromNowOn() : _trucks.openNow(), function(idx, stop) {
      _markers.add(stop);
    });
  }

  function resize() {
    $("#map_canvas").height($(window).height() - $("#topBar").height() - 20);
  }

  function setupGlobalEventHandlers() {
    $(window).resize(function () {
      resize();
    });
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

  function buildTruckDialog(truck) {
    var $truckDialog = $("#truckDialog");
    $("#truckIcon").attr("src", truck.iconUrl);
    var $truckSocial = $("#truckSocial");
    $truckSocial.empty();
    $("#truck-url").empty();
    $("#truckInfoLink").attr("href", "/trucks/" + truck.id);
    if (truck.twitterHandle) {
      $truckSocial.append("<a target='_blank' href='http://twitter.com/" + truck.twitterHandle +
          "'><img alt='@" +
          truck.twitterHandle + "' src='http://storage.googleapis.com/ftf_static/img/twitter32x32.png'/></a> ");
    }
    if (truck.foursquare) {
      $truckSocial.append("<a target='_blank' href='http://foursquare.com/venue/" +
          truck.foursquare +
          "'><img alt='Checkin on foursquare' src='http://storage.googleapis.com/ftf_static/img/foursquare32x32.png'/></a> ");
    }
    if (truck.instagram) {
      $truckSocial.append("<a target='_blank' href='http://instagram.com/" +
          truck.instagram +
          "'><img alt='View instagram feed' src='http://storage.googleapis.com/ftf_static/img/instagram32x32.png'/></a> ");
    }
    if (truck.facebook) {
      $truckSocial.append("<a target='_blank' href='http://facebook.com" + truck.facebook +
          "'><img alt='" +
          truck.facebook + "' src='http://storage.googleapis.com/ftf_static/img/facebook32x32.png'/></a> ");
    }
    var $truckInfo = $("#truckInfo");
    $truckInfo.empty();
    if (truck.url) {
      $("#truck-url").append("<a class='whitelink' target='_blank' href='" + truck.url + "'>" +
          truck.url + "</a>").removeClass("hidden");
    } else {
      $("#truck-url").addClass("hidden");
    }
    var $truckSchedule = $("#truckSchedule");
    $truckSchedule.empty();
    $.each(_trucks.findStopsForTruck(truck.id), function (idx, stop) {
      var $tr = $("<tr></tr>");
      $tr.append("<td>" + stop.stop.startTime + "</td>");
      $tr.append("<td><a href='/locations/" + stop.location.key + "'>" + stop.location.name + "</a></td>")
      $truckSchedule.append($tr);
    });
    $("#truckTitle").html(truck.name);
    $truckDialog.modal({show: true, keyboard: true, backdrop: true});
  }

  function displayMessageOfTheDay(model) {
    var id = getCookie("motd");
    if (model["message"] && (id != model["message"]["id"])) {
      $("#motd-message").html(model["message"]["message"]);
      $("#motd").removeClass("hidden");
      $('#motd').on('closed.bs.alert', function (e) {
        setCookie("motd", model["message"]["id"])
      });
    }
  }

  function openLaterWidget() {
    var toggleDiv = document.createElement("div");
    toggleDiv.index = 1;

    // Setting padding to 5 px will offset the control
    // from the edge of the map
    toggleDiv.style.padding = '5px';

    // Set CSS for the control border
    var controlUI = document.createElement('div');
    controlUI.style.backgroundColor = 'white';
    controlUI.style.borderStyle = 'none';
    controlUI.style.borderWidth = '0px';
    controlUI.style.cursor = 'pointer';
    controlUI.style.textAlign = 'center';
    controlUI.title = '';
    toggleDiv.appendChild(controlUI);

    // Set CSS for the control interior
    var controlText = document.createElement('div');
    controlText.style.fontFamily = 'Arial,sans-serif';
    controlText.style.fontSize = '12px';
    controlText.style.paddingLeft = '4px';
    controlText.style.paddingRight = '4px';
    controlText.innerHTML = '<b><input type="checkbox" id="futureStopsCB">&nbsp;<label for="futureStopsCB">Show future stops</label></b>';
    controlUI.appendChild(controlText);

    google.maps.event.addDomListener(controlUI, 'click', function () {
      console.log("HELLO");
      _showFutureStops = $("#futureStopsCB").is(":checked");
      refreshViewData();
    });
    return toggleDiv;
  }

  return {
    setModel: function (model) {
      _trucks = new Trucks(model);
      refreshViewData();
    },
    getModel: function () {
      return _trucks;
    },
    openTruck: function (truckId) {
      buildTruckDialog(_trucks.findTruck(truckId));
    },
    reload: function () {
      console.log("Reloading model...")
      var self = this;
      $.ajax({
        url: '/services/daily_schedule?appKey=' + _appKey,
        dataType: 'json',
        cache: false,
        error: function () {
          try {
            console.log("Failed to reload model at " + (new Date()));
          } catch (e) {
          }
        },
        success: function (data) {
          try {
            console.log("Successfully loaded model at " + (new Date()));
          } catch (e) {
          }
          self.setModel(data);
        }
      });
    },
    extend: function () {
      _map.fitBounds(_markers.bounds);
    },
    run: function (mode, center, time, modelPayload, appKey, defaultCity) {
      var self = this;
      var mobile = "mobile" == mode;
      _appKey = appKey;
      _defaultCityRegex = new RegExp(", " + defaultCity + "$");
      _defaultCityLength = defaultCity.length;
      _center = findCenter(center);
      resize();
      displayMessageOfTheDay(modelPayload);
      _markers = new Markers();
      _trucks = new Trucks(modelPayload);
      _map = new google.maps.Map(document.getElementById("map_canvas"), {
        zoom: findZoom(11),
        center: _center,
        maxZoom: 18,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      });
      var loading = true, manuallyMoved = false;
      google.maps.event.addListener(_map, 'center_changed', function () {
        saveCenter(_map.getCenter());
      });

      _map.controls[google.maps.ControlPosition.TOP_RIGHT].push(openLaterWidget());

      google.maps.event.addListener(_map, 'zoom_changed', function () {
        if (loading) {
          manuallyMoved = true;
        }
        saveZoom(_map.getZoom());
      });
      var listener = null;
      // just want to invoke this once, for when the map first loads
      listener = google.maps.event.addListener(_map, 'bounds_changed', function () {
        refreshViewData();
        if (listener) {
          google.maps.event.removeListener(listener);
        }
      });
      setupGlobalEventHandlers();
      // reload the model every 5 minutes
      setInterval(function () {
        self.reload();
      }, 300000)
    }
  };
}();
