var FoodTruckLocator = function () {
  var _appKey = null,
      _trucks = null,
      _mode = null,
      _center = null,
      _userLocation = null,
      _defaultCityRegex = null,
      _defaultCityLength = 0;

  function flash(msg) {
    $("#flashMsg").css("display", "block").html(msg);
  }

  function shouldDisplayDistances() {
    return _userLocation != null;
  }

  function refreshViewData() {
    updateDistanceFromCurrentLocation();
    updateTruckLists();
  }

  function toRadians(degree) {
    return degree / 180 * Math.PI;
  }

  function computeDistanceInMiles(location1, location2) {
    var lat1R = toRadians(location1.coords.latitude);
    var lat2R = toRadians(location2.coords.latitude);
    var latR = Math.abs(lat2R - lat1R);
    var lngR = Math.abs(toRadians(location2.coords.longitude - location1.coords.longitude));
    var a = Math.sin(latR / 2.0) * Math.sin(latR / 2.0) + Math.cos(lat1R) * Math.cos(lat2R) * Math.sin(lngR / 2.0) * Math.sin(lngR / 2.0);
    return 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a)) * 3959;
  }

  function setCookie(name, value, days) {
    var expires;
    if (days) {
      var date = new Date();
      date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
      // noinspection JSUnresolvedFunction
      expires = "; expires=" + date.toGMTString();
    }
    else expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
  }

  function findLocation() {
    return (_userLocation != null) ? _userLocation : _center;
  }

  function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
      var c = ca[i];
      // noinspection EqualityComparisonWithCoercionJS
      while (c.charAt(0) == ' ') c = c.substring(1, c.length);
      // noinspection EqualityComparisonWithCoercionJS
      if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
  }

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

    $.each(model["specials"], function(idx, item) {
      self.trucks[item["truckId"]]["specials"] = item["specials"];
    });

    function buildStop(stop) {
      return {
        stop: stop,
        now : false,
        truck: self.trucks[stop["truckId"]],
        location: model["locations"][stop["location"] - 1],
        position: {coords: {latitude: model["locations"][stop["location"] - 1].latitude,
            longitude: model["locations"][stop["location"] - 1].longitude}}
      }
    }

    this.all = function () {
      return this.stops;
    };

    this.updateDistanceFrom = function (location) {
      $.each(this.stops, function (idx, stop) {
        var distance = computeDistanceInMiles(location, stop.position);
        stop.distance = Math.round(distance * 100) / 100;
      });
    };

    this.openNow = function () {
      var now = Clock.now(), items = [];
      $.each(self.stops, function (idx, item) {
        if (item.stop["startMillis"] <= now && item.stop["endMillis"] > now) {
          item.now = true;
          items.push(item);
        }
      });
      return items;
    };

    this.openLater = function () {
      var now = Clock.now(), items = [];
      $.each(self.stops, function (idx, item) {
        if (item.stop["startMillis"] > now) {
          item.now = false;
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

  function makeRelative(url) {
    return /^http:/.exec(url) ? url.substr(5) : url;
  }

  function buildTimeRange(stop, time) {
    // noinspection JSUnresolvedVariable
    if (stop.startMillis < time && stop.endMillis > time) {
      // noinspection JSUnresolvedVariable
      if (stop.fromBeacon) {
        var result = "<em>Transmitting from beacon <span class='glyphicon glyphicon-flash'></span></span></em>";
        if (stop["me"]) {
          result += "<br/>Est. departure time: " + stop.endTime;
        }
        return result;
      } else {
        return "Est. departure time: " + stop.endTime;
      }
    } else {
      return stop.startTime + " - " + stop.endTime;
    }
  }

  function buildTruckList($truckList, stops) {
    $truckList.empty();
    var lastLocation = null, $row = null, now = Clock.now();
    $.each(stops, function(idx, stop) {
      if (lastLocation !== stop.location.name) {
        lastLocation = stop.location.name;
        $truckList.append("<h2><a href='/locations/" + stop.location.key + "'>" + formatLocation(lastLocation) + "</a></h2>");
        if (shouldDisplayDistances() && stop.distance) {
          $truckList.append("<p>(" + stop.distance + " miles from current location)</p>")
        }
        $row = $("<div class='row truck-row'></div>");
        $truckList.append($row);
      }
      // noinspection JSUnresolvedVariable
      $row.append("<div class='col-xs-6 col-md-3'><a href='/trucks/" + stop.truck.id + "'><div class='thumbnail'>" +
          "<img width='180' height='180' src='" + makeRelative(stop.truck.previewIcon) + "' title='' class='img-rounded'/>" +
          "<p class='text-center'><strong>" + stop.truck.name + "<br/>" + buildTimeRange(stop.stop, now) +
          "</strong></p></div></a></div>");
    });
  }

  function updateTruckLists() {
    var nowTrucks = sortByDistanceFromLocation(_trucks.openNow()),
        laterTrucks = sortByDistanceFromLocation(_trucks.openLater());
    if (nowTrucks.length === 0 && laterTrucks.length === 0) {
      $(".trucksListHeader").css("display", "none");
      $("#navTabs").css("display", "none");
      $(".truckDL").empty();
    } else {
      $(".trucksListHeader").css("display", "block");
      $("#navTabs").css("display", "block");
      var markers = [];
      buildTruckList($("#nowTrucks"), nowTrucks, markers);
      buildTruckList($("#laterTrucks"), laterTrucks, markers);
      if (nowTrucks.length === 0) {
        $('a[href="#laterTrucks"]').tab('show');
      } else {
        $('a[href="#nowTrucks"]').tab('show');
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
      if (typeof a.distance === "undefined" || a.distance == null) {
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

  function resize() {
    var $topBar = $("#topBar");
    $("#sidebar").height($(window).height() - $topBar.height() - 60);
    $("#listContainer").height($(window).height() - $topBar.height() - 60);
  }

  function displayMessageOfTheDay(model) {
    var id = getCookie("motd");
    if (model["message"] && (id !== model["message"]["id"])) {
      $("#motd-message").html(model["message"]["message"]);
      var $motd = $("#motd");
      $motd.removeClass("hidden");
      $motd.on('closed.bs.alert', function () {
        setCookie("motd", model["message"]["id"], 30)
      });
    }
  }

  return {
    setModel: function (model) {
      _trucks = new Trucks(model);
      refreshViewData();
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
    flash: function (msg, type) {
      flash(msg, type);
    },
    clear: function() {
    },
    run: function (mode, center, time, modelPayload, appKey, defaultCity) {
      var self = this;
      _appKey = appKey;
      _mode = mode;
      _defaultCityRegex = new RegExp(", " + defaultCity + "$");
      _defaultCityLength = defaultCity.length;
      _center = center;
      resize();
      displayMessageOfTheDay(modelPayload);
      _trucks = new Trucks(modelPayload);
      var loading = true;
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (position) {
          loading = false;
          var distance = computeDistanceInMiles(center, position);
          // sanity check.  Don't pan beyond 60 miles from default center
          if (distance < 60) {
            // refresh the distances
            _userLocation = position;
            refreshViewData();
          }
        }, function () {
          loading = false;
        });
      } else {
        loading = false;
      }
      // reload the model every 5 minutes
      setInterval(function () {
        self.reload();
      }, 300000)
    }
  };
}();
