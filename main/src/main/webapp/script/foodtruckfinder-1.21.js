var FoodTruckLocator = function () {
  var _appKey = null,
      _trucks = null,
      _mode = null,
      _center = null,
      _userLocation = null,
      _defaultCityRegex = null,
      _defaultCityLength = 0;

  function hideFlash() {
    $("#flashMsg").css("display", "none");
  }

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
      while (c.charAt(0) == ' ') c = c.substring(1, c.length);
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

    this.findTruck = function (truckId) {
      return this.trucks[truckId];
    };

    this.findStopsForTruck = function (truckId) {
      var items = [];
      $.each(self.stops, function (idx, item) {
        if (item.stop["truckId"] === truckId) {
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

  function buildGroupTableRow(stop) {
    return "<div class='media'><a href='/trucks/" + stop.truck.id
        + "' class='pull-left'><img class='img-responsive img-rounded' src='" + makeRelative(stop.truck.iconUrl)
        + "'/></a><div class='media-body' >" + "<div><strong>" + stop.truck.name + "</strong></div><div>"
        + buildTimeRange(stop.stop, Clock.now()) + " </div></div></div>";
  }

  function buildTimeRange(stop, time) {
    if (stop.startMillis < time && stop.endMillis > time) {
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

  function buildTruckList($truckList, stops, markers) {
    $truckList.empty();
    var $items = $("<ul class='media-list'></ul>"), now = Clock.now(),
        $location, $div, lastMarkerGroup, lastLocation = null, centerNote = "current location";
    $items.appendTo($truckList);

    $.each(stops, function (idx, stop) {
      var $locationDescription = $("<div></div>");
      if (stop.location.url) $locationDescription.append("<div><a href='" + stop.location.url + "'>" + stop.location.url + "</a></div>");
      if (stop.location.description) $locationDescription.append("<div>" + stop.location.description + " </div>");
      if (stop.location.twitterHandle) $locationDescription.append("<div><small>Follow <a href='http://twitter.com/" + stop.location.twitterHandle + "'>@" + stop.location.twitterHandle + "</a> on twitter.</small></div>");
      if (shouldDisplayDistances() && stop.distance) $locationDescription.append("<div>(" + stop.distance + " miles from " + centerNote + ")</div>");
      if (lastLocation != stop.location.name) {
        $div = $("<div class='media-body'><h4><a href='/locations/" + stop.location.key + "'>" + formatLocation(stop.location.name) + "</a></h4></div>");
        $div.append($locationDescription);
        var linkBody = "";
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
      var special = "";
      if (stop.truck["specials"]) {
        var firstSpecial = stop.truck["specials"][0]["special"];
        special = "<div class='text-info'><strong>Special: " + firstSpecial + "</strong></div>";
      }
      $div.append($("<div class='media'><a class='pull-left truckLink' truck-id='" + stop.truck.id
          + "' href='#'><img class='media-object img-responsive img-rounded' src='"
          + makeRelative(stop.truck.iconUrl) + "'/></a><div class='media-body'><a class='truckLink' href='#' truck-id='" + stop.truck.id
          + "'><strong>" + stop.truck.name + "</strong><div>"
          + buildTimeRange(stop.stop, now)
          + "</div>" + special
          + "</a></div></div>"));
    });
    $("a.truckLink").each(function (idx, item) {
      var $item = $(item), truckId = $item.attr("truck-id");
      $item.attr("href", "/trucks/" + truckId);
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
      if (nowTrucks.length == 0) {
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

  function resize() {
    var $topBar = $("#topBar");
    $("#sidebar").height($(window).height() - $topBar.height() - 60);
    $("#listContainer").height($(window).height() - $topBar.height() - 60);
  }

  function buildTruckDialog(truck) {
    var $truckDialog = $("#truckDialog");
    $("#truckDialog .modal-header").css("background-image", 'url("/images/truckicons/' + truck.id + '_bannerlarge")');
    $("#truckIcon").attr("src", truck.iconUrl);
    var $truckSocial = $("#truckSocial"), $truckUrl = $("#truck-url");
    $truckSocial.empty();
    $truckUrl.empty();
    $("#truckInfoLink").attr("href", "/trucks/" + truck.id);
    if (truck.twitterHandle) {
      $truckSocial.append("<a target='_blank' href='http://twitter.com/" + truck.twitterHandle +
          "'><img alt='@" +
          truck.twitterHandle + "' src='//storage.googleapis.com/ftf_static/img/twitter32x32.png'/></a> ");
    }
    if (truck.foursquare) {
      $truckSocial.append("<a target='_blank' href='http://foursquare.com/venue/" +
          truck.foursquare +
          "'><img alt='Checkin on foursquare' src='//storage.googleapis.com/ftf_static/img/foursquare32x32.png'/></a> ");
    }
    if (truck.instagram) {
      $truckSocial.append("<a target='_blank' href='http://instagram.com/" +
          truck.instagram +
          "'><img alt='View instagram feed' src='//storage.googleapis.com/ftf_static/img/instagram32x32.png'/></a> ");
    }
    if (truck.facebook) {
      $truckSocial.append("<a target='_blank' href='http://facebook.com" + truck.facebook +
          "'><img alt='" +
          truck.facebook + "' src='//storage.googleapis.com/ftf_static/img/facebook32x32.png'/></a> ");
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

  function displayMessageOfTheDay(model) {
    var id = getCookie("motd");
    if (model["message"] && (id != model["message"]["id"])) {
      $("#motd-message").html(model["message"]["message"]);
      var $motd = $("#motd");
      $motd.removeClass("hidden");
      $motd.on('closed.bs.alert', function () {
        setCookie("motd", model["message"]["id"], 30)
      });
    }
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
    },
    refreshIt: function() {
      refreshViewData();
    },
    run: function (mode, center, time, modelPayload, appKey, defaultCity, buttons) {
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
