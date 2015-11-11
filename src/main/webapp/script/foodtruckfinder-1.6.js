var FoodTruckLocator = function () {
  var _map = null,
      _appKey = null,
      _trucks = null,
      _mobile = false,
      _mode = null,
      _markers = null,
      _defaultCityRegex = null,
      _defaultCityLength = 0,
      _openInfoWindow = null,
      _center = null;

  function isMobile() {
    return _map == null || _mobile;
  }

  function hideFlash() {
    $("#flashMsg").css("display", "none");
  }

  function flash(msg) {
    $("#flashMsg").css("display", "block").html(msg);
  }

  function refreshViewData() {
    updateDistanceFromCurrentLocation();
    if (!isMobile()) {
      updateMap();
    }
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

  var Markers = function () {
    var markers = {}, lastLetter = 0, color = "", locationMarkers = {};

    function buildIconURL(letter) {
      var code = letter.charCodeAt(0);
      var color = "";
      if (code > 90) {
        code = code - 26;
        color = "_orange";
      }
      letter = String.fromCharCode(code);
      return "http://www.google.com/mapfiles/marker" + color + letter + ".png";
    }

    this.clear = function () {
      color = "", lastLetter = 0;
      this.bounds = new google.maps.LatLngBounds();
      $.each(markers, function (key, marker) {
        marker.setMap(null);
      });
      markers = {};
      $.each(locationMarkers, function (key, marker) {
        marker.setMap(null);
      });
      locationMarkers = {};
    };

    this.add = function (stop) {
      if (markers[stop.location.name] == undefined) {
        var letterId = String.fromCharCode(65 + lastLetter), color = "";
        stop.marker = new google.maps.Marker({
          map: _map,
          icon: buildIconURL(letterId),
          position: stop.position
        });
        if (lastLetter > 25) {
          color = "Orange";
          letterId = String.fromCharCode(lastLetter - 26 + 65);
        }
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

    this.all = function () {
      return this.stops;
    };

    this.updateDistanceFrom = function (location) {
      $.each(this.stops, function (idx, stop) {
        var distance = google.maps.geometry.spherical.computeDistanceBetween(location,
            stop.position, 3959);
        stop.distance = Math.round(distance * 100) / 100;
      });
    };

    this.allVisible = function () {
      if (isMobile()) {
        return true;
      }
      var bounds = isMobile() ? null : _map.getBounds(), visible = true;
      if (!bounds) {
        return false;
      }
      // TODO: not efficient (looping)
      $.each(this.stops, function (idx, stop) {
        if (!bounds.contains(stop.position)) {
          visible = false;
        }
      });
      return visible;
    };

    this.hasActive = function () {
      return self.openNowAndLater().length > 0;
    };

    this.openNowAndLater = function() {
      var now = Clock.now(), items = [];
      $.each(self.stops, function (idx, item) {
        if (item.stop["endMillis"] > now) {
          items.push(item);
        }
      });
      return items;
    };

    this.openNow = function () {
      var now = Clock.now(), items = [], bounds = isMobile() ? null : _map.getBounds();
      $.each(self.stops, function (idx, item) {
        if (item.stop["startMillis"] <= now && item.stop["endMillis"] > now && (isMobile() || (bounds && bounds.contains(item.position)))) {
          items.push(item);
        }
      });
      return items;
    };

    this.openLater = function () {
      var now = Clock.now(), items = [], bounds = isMobile() ? null : _map.getBounds();
      $.each(self.stops, function (idx, item) {
        if (item.stop["startMillis"] > now && (isMobile() || (bounds && bounds.contains(item.position)))) {
          items.push(item);
        }
      });
      return items;
    }
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
    contentString = contentString + "<div class='media-list' style='margin:10px'>";
    $.each(stops, function (idx, stop) {
      contentString += buildGroupTableRow(stop);
    });
    contentString = contentString + "</div></div>";
    var infowindow = new google.maps.InfoWindow({
      content: contentString
    });

    google.maps.event.addListener(marker, 'click', function () {
      if (_openInfoWindow) {
        _openInfoWindow.close();
      }
      _openInfoWindow = infowindow;
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

  function buildTruckList($truckList, stops, markers) {
    $truckList.empty();
    var $items = $("<ul class='media-list'></ul>"), now = Clock.now(),
        $location, $div, lastMarkerGroup, lastLocation = null, centerNote = _mobile ? "current location" : "map center";
    $items.appendTo($truckList);

    $.each(stops, function (idx, stop) {
      var $locationDescription = $("<div></div>");
      if (stop.location.url) $locationDescription.append("<div><a href='" + stop.location.url + "'>" + stop.location.url + "</a></div>");
      if (stop.location.description) $locationDescription.append("<div>" + stop.location.description + " </div>");
      if (stop.location.twitterHandle) $locationDescription.append("<div><small>Follow <a href='http://twitter.com/" + stop.location.twitterHandle + "'>@" + stop.location.twitterHandle + "</a> on twitter.</small></div>");
      if (stop.distance) $locationDescription.append("<div>(" + stop.distance + " miles from " + centerNote + ")</div>");
      if (lastLocation != stop.location.name) {
        $div = $("<div class='media-body'><h4><a href='/locations/" + stop.location.key + "'>" + formatLocation(stop.location.name) + "</a></h4></div>");
        $div.append($locationDescription);
        var linkBody = isMobile() ? "" : "<a class='pull-left' href='#'><img id='" + stop.markerId + "' class='media-object' src='"
        + stop.marker.icon + "'/></a>";
        $location = $("<li class='media'>" + linkBody + "</li>");
        $location.append($div);
        $items.append($location);
        var foundItems = $.grep(markers, function (item) {
          if (typeof(item.marker) == 'object') {
            return item.marker.icon == stop.marker.icon;
          } else {
            return item.stops[0].location.name == stop.location.name;
          }
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
      $div.append($("<div class='media'><a class='pull-left truckLink' truck-id='" + stop.truck.id
      + "' href='#'><img class='media-object' src='"
      + stop.truck.iconUrl + "'/></a><div class='media-body'><a class='truckLink' href='#' truck-id='" + stop.truck.id
      + "'><strong>" + stop.truck.name + "</strong><div>"
      + buildTimeRange(stop.stop, now)
      + "</div>"
      + "</a></div></div>"));
    });
    $("a.truckLink").each(function (idx, item) {
      var $item = $(item), truckId = $item.attr("truck-id");
      if (isMobile()) {
        $item.attr("href", "/trucks/" + truckId);
      } else {
        $item.off('click');
        $item.click(function (e) {
          e.preventDefault();
          buildTruckDialog(_trucks.findTruck(truckId));
          return false;
        })
      }
    });
  }

  function updateTruckLists() {
    var nowTrucks = sortByDistanceFromLocation(_trucks.openNow()),
        laterTrucks = sortByDistanceFromLocation(_trucks.openLater());
    if (nowTrucks.length == 0 && laterTrucks.length == 0) {
      $(".trucksListHeader").css("display", "none");
      $("#navTabs").css("display", "none");
      $(".truckDL").empty();
    } else {
      $(".trucksListHeader").css("display", "block");
      $("#navTabs").css("display", "block");
      var markers = [];
      buildTruckList($("#nowTrucks"), nowTrucks, markers);
      buildTruckList($("#laterTrucks"), laterTrucks, markers);
      if (!isMobile()) {
        $.each(markers, function (idx, markerAndId) {
          if (markerAndId.marker.getAnimation() != null) {
            return;
          }
          buildInfoWindow(markerAndId.marker, markerAndId.stops);
          $("#" + markerAndId.id).click(function (e) {
            e.preventDefault();
            markerAndId.marker.setAnimation(google.maps.Animation.BOUNCE);
            setTimeout(function () {
              markerAndId.marker.setAnimation(null);
            }, 3000);
          });

        });
      }
      if (nowTrucks.length == 0) {
        $('a[href="#laterTrucks"]').tab('show');
      }
    }
  }

  function updateDistanceFromCurrentLocation() {
    if (_trucks) {
      _trucks.updateDistanceFrom(findLocation());
    }
  }

  function sortByDistanceFromLocation(stops) {
    return stops.sort(function (a, b) {
      if (typeof a.distance == "undefined" || a.distance == null) {
        return 0;
      }
      if (a.distance > b.distance) {
        return 1;
      } else if (a.distance < b.distance) {
        return -1;
      } else {
        return a.location.name.localeCompare(b.location.name);
      }
    });
  }

  function updateMap() {
    _markers.clear();
    var currentLocation = findLocation();
    _markers.bounds.extend(currentLocation);
    // TODO: we're sorting in two locations...probably shouldn't do that.
    $.each(sortByDistanceFromLocation(_trucks.openNow()), function (idx, stop) {
      _markers.add(stop);
    });
    $.each(sortByDistanceFromLocation(_trucks.openLater()), function (idx, stop) {
      _markers.add(stop);
    });
  }

  function resize() {
    var $topBar = $("#topBar");
    $("#map_canvas").height($(window).height() - $topBar.height() - 60);
    $("#sidebar").height($(window).height() - $topBar.height() - 60);
    $("#listContainer").height($(window).height() - $topBar.height() - 60);
  }

  function setupGlobalEventHandlers() {
    $(window).resize(function () {
      resize();
    });
    $('a[href="#nowTrucks"]').click(function (e) {
      e.preventDefault();
      $(this).tab('show')
    });
    $('a[href="#laterTrucks"]').click(function (e) {
      e.preventDefault();
      $(this).tab('show')
    });
  }

  function displayWarningIfMarkersNotVisible() {
    if (_trucks == null || !_trucks.hasActive()) {
      flash("There are no food trucks on the road right now.");
    } else if (_trucks.allVisible()) {
      hideFlash();
    } else {
      flash("The result list is currently being filtered based on the zoom-level and center of the map.  To see all trucks, zoom out.", "warning")
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

  function savePinMap(pinMap) {
    setCookie("pin_map", pinMap);
  }

  function findPinMap() {
    var pinMap = getCookie("pin_map");
    if (pinMap) {
      return "true" == pinMap;
    }
    return false;
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
    var $truckSocial = $("#truckSocial"), $truckUrl = $("#truck-url");
    $truckSocial.empty();
    $truckUrl.empty();
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
      $truckUrl.append("<a class='whitelink' target='_blank' href='" + truck.url + "'>" +
      truck.url + "</a>").removeClass("hidden");
    } else {
      $truckUrl.addClass("hidden");
    }
    var $truckSchedule = $("#truckSchedule");
    $truckSchedule.empty();
    $.each(_trucks.findStopsForTruck(truck.id), function (idx, stop) {
      var $tr = $("<tr></tr>");
      $tr.append("<td>" + stop.stop.startTime + "</td>");
      $tr.append("<td><a href='/locations/" + stop.location.key + "'>" + stop.location.name + "</a></td>");
      $truckSchedule.append($tr);
    });
    $("#truckTitle").html(truck.name);
    $truckDialog.modal({show: true, keyboard: true, backdrop: true});
  }

  function displayListOnly() {
    return (_mode != "desktop" && Modernizr.touch && window.screen.width < 1024);
  }

  function displayMessageOfTheDay(model) {
    var id = getCookie("motd");
    if (model["message"] && (id != model["message"]["id"])) {
      $("#motd-message").html(model["message"]["message"]);
      var $motd = $("#motd");
      $motd.removeClass("hidden");
      $motd.on('closed.bs.alert', function () {
        setCookie("motd", model["message"]["id"])
      });
    }
  }

  function badgeWidget() {
    var badgeDiv = document.createElement("div");
    badgeDiv.index = 1;

    // Setting padding to 5 px will offset the control
    // from the edge of the map
    badgeDiv.style.padding = '17px';

    // Set CSS for the control border
    var controlUI = document.createElement('div');
    controlUI.style.backgroundColor = 'white';
    controlUI.style.borderStyle = 'none';
    controlUI.style.borderWidth = '0px';
    controlUI.style.cursor = 'pointer';
    controlUI.style.textAlign = 'center';
    controlUI.title = '';
    badgeDiv.appendChild(controlUI);

    // Set CSS for the control interior
    var controlText = document.createElement('div');
    var androidBadge = '<a href="https://play.google.com/store/apps/details?id=net.andrewviolette.truckz"><img src="/img/en_generic_rgb_wo_45.png" title="Google Play Button"/></a>';
    var iphoneBadge = '<a href="https://itunes.apple.com/us/app/chicago-food-truck-finder/id1002801516"><img height="45px" src="/img/Download_on_the_App_Store_Badge_US-UK_135x40.svg"/></a>';
    if (Modernizr.touch) {
      if (navigator.userAgent.match(/iPhone/i) || navigator.userAgent.match(/iPod/i) || navigator.userAgent.match(/iPad/i) ) {
        controlText.innerHTML  = iphoneBadge;
      } else {
        controlText.innerHTML  = androidBadge;
      }
    } else {
      controlText.innerHTML  = androidBadge + " " + iphoneBadge;
    }
    controlUI.appendChild(controlText);
    return badgeDiv;
  }

  function createOptionsDiv() {
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
    controlText.innerHTML = '<b><input ' + (findPinMap() ? "checked" : "")  + ' type="checkbox" id="pinMapCB">&nbsp;<label for="pinMapCB">Pin map at current location</label></b>';
    controlUI.appendChild(controlText);

    google.maps.event.addDomListener(controlUI, 'click', function () {
      savePinMap($("#pinMapCB").is(":checked"));
    });
    return toggleDiv;
  }

  //noinspection JSUnusedGlobalSymbols
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
      console.log("Reloading model...");
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
    hideFlash: function () {
      hideFlash();
    },
    flash: function (msg, type) {
      flash(msg, type);
    },
     clear: function() {
       _markers.clear();
    },
    extend: function () {
      var bounds = new google.maps.LatLngBounds();
      $.each(_trucks.openNowAndLater(), function (idx, stop) {
        bounds.extend(stop.position);
      });
      _map.fitBounds(bounds);
    },
    run: function (mode, center, time, modelPayload, appKey, defaultCity) {
      var self = this;
      var mobile = "mobile" == mode;
      _appKey = appKey;
      _mode = mode;
      _defaultCityRegex = new RegExp(", " + defaultCity + "$");
      _defaultCityLength = defaultCity.length;
      _center = findCenter(center);
      resize();
      displayMessageOfTheDay(modelPayload);
      if (displayListOnly() || mobile) {
        _mobile = true;
        $("body").css("background", "white");
        $("#map_wrapper").css("display", "none");

        if (navigator.userAgent.match(/iPhone/i) || navigator.userAgent.match(/iPod/i) || navigator.userAgent.match(/iPad/i) ) {
          $("#iphoneBadge").removeClass("hidden");
        } else {
          $("#androidBadge").removeClass("hidden");
        }
        $("#appbadges").removeClass("hidden");


        if (Modernizr.geolocation) {
          self.setModel(modelPayload);
          flash("Looking up location...");
          navigator.geolocation.getCurrentPosition(function (position) {
            _center = new google.maps.LatLng(position.coords.latitude,
                position.coords.longitude);
            hideFlash();
            self.setModel(modelPayload);
          }, function () {
            hideFlash();
            self.setModel(modelPayload);
          }, {timeout: 5000});
        } else {
          self.setModel(modelPayload);
        }
      } else {
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
          displayWarningIfMarkersNotVisible();
        });

        var centerMarker = new google.maps.Marker({
          icon: "http://maps.google.com/mapfiles/arrow.png"
        });

        _map.controls[google.maps.ControlPosition.BOTTOM_CENTER].push(badgeWidget());
        _map.controls[google.maps.ControlPosition.TOP_RIGHT].push(createOptionsDiv());

        google.maps.event.addListener(_map, 'drag', function () {
          if (loading) {
            manuallyMoved = true;
          }
          centerMarker.setMap(_map);
          centerMarker.setPosition(_map.getCenter());
        });

        google.maps.event.addListener(_map, 'dragend', function () {
          if (loading) {
            manuallyMoved = true;
          }
          centerMarker.setMap(null);
          refreshViewData();
        });

        google.maps.event.addListener(_map, 'zoom_changed', function () {
          if (loading) {
            manuallyMoved = true;
          }
          saveZoom(_map.getZoom());
          refreshViewData();
        });
        var listener = null;
        // just want to invoke this once, for when the map first loads
        listener = google.maps.event.addListener(_map, 'bounds_changed', function () {
          refreshViewData();
          console.log("HERE");
          _map.fitBounds(_markers.bounds);

          if (listener) {
            google.maps.event.removeListener(listener);
          }
        });
        setupGlobalEventHandlers();

        if (Modernizr.geolocation) {
          navigator.geolocation.getCurrentPosition(function (position) {
            if (manuallyMoved || findPinMap()) {
              return;
            }
            loading = false;
            var latLng = new google.maps.LatLng(position.coords.latitude,
                    position.coords.longitude),
                distance = google.maps.geometry.spherical.computeDistanceBetween(center,
                    latLng, 3959);
            // sanity check.  Don't pan beyond 60 miles from default center
            if (distance < 60) {
              saveCenter(latLng);
              // refresh the distances
              _map.panTo(_center);
              refreshViewData();
            }
          }, function () {
            loading = false;
          });
        } else {
          loading = false;
        }
      }
      // reload the model every 5 minutes
      setInterval(function () {
        self.reload();
      }, 300000)
    }
  };
}();
