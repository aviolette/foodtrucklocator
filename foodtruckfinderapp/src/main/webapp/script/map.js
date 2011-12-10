window.FoodTruckLocator = function() {
  function buildIconUrl(letter) {
    return "http://www.google.com/mapfiles/marker" + letter + ".png"
  }

  function distanceSort(a, b) {
    if (typeof a.distance == "undefined" || a.distance == null) {
      return 0;
    }
    return a.distance > b.distance ? 1 : ((a.distance == b.distance) ? 0 : -1);
  }

  function buildMarker(objectOnMap, letter, bounds, map) {
    objectOnMap.marker = new google.maps.Marker({
      map: map,
      icon: buildIconUrl(letter),
      position: objectOnMap.position.latLng
    });
    bounds.extend(objectOnMap.position.latLng);
  }

  function removeChicago(location) {
    if (/, Chicago, IL$/i.test(location)) {
      location = location.substring(0, location.length - 13);
    }
    return location;
  }

  var TruckGroup = function(location) {
    var self = this;
    self.trucks = [];
    self.position = location;
    self.addTruck = function(truck) {
      self.trucks.push(truck);
    }
  };

  var Trucks = Backbone.Model.extend({
    getTrucks : function() {
      var self = this;
      if (typeof(this.trucks) != "undefined") {
        return this.trucks;
      }
      var payload = this.get("payload");
      self.trucks = {};
      $.each(payload.trucks, function(idx, truck) {
        self.trucks[truck.id] = truck;
      });
      return self.trucks;
    },
    linkLocations : function(payload) {
      var locations = payload.locations;
      $.each(payload.stops, function(idx, stop) {
        stop.location = locations[stop.location - 1];
      });
    },
    getGroups : function(time) {
      var self = this, groups = [],groupMap = {};
      var payload = this.get("payload");
      var trucks = self.getTrucks();
      $.each(payload.stops, function(idx, stop) {
        if (time >= stop.startMillis && time < stop.endMillis) {
          var truckGroup = groupMap[stop.location.name];
          if (typeof(truckGroup) == "undefined") {
            var latlng = new google.maps.LatLng(stop.location.latitude,
                stop.location.longitude);
            truckGroup = new TruckGroup({name: stop.location.name, latLng : latlng });
            groupMap[stop.location.name] = truckGroup;
            groups.push(truckGroup);
          }
          truckGroup.addTruck(trucks[stop.truckId]);
        }
      });
      return groups;
    }
  });

  var MapView = Backbone.View.extend({
    initialize : function() {
      this.map = new google.maps.Map(document.getElementById("map_canvas"), {
        zoom: 13,
        center: this.options.center,
        maxZoom : 15,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      });
      var self = this;
      var TimeSlider = function(initialTime) {
        var sliderValue = (initialTime.getHours() * 60) +
            (Math.floor(initialTime.getMinutes() / 5) * 5);

        function computeTime(value, twelveHour) {
          // yuck - cleanup
          var val = value / 60;
          var hour = Math.floor(val);
          var min = Math.round((val - hour) * 60);
          if (hour > 12 && twelveHour) {
            hour = hour - 12;
          }
          hour = (hour < 10) ? "0" + hour : "" + hour;
          min = (min < 10) ? "0" + min : "" + min;
          return [hour, min];
        }

        function displayTime(value) {
          var time = computeTime(value, true);
          $("#sliderTime").empty().append(time.join(":"));
        }

        displayTime(sliderValue);
        $("#slider").slider({
          min: 0,  max: 1440, value : sliderValue, step: 5,
          slide : function(event, ui) {
            displayTime(ui.value);
          },
          change: function(event, ui) {
            var time = computeTime(ui.value, false);
            self.currentTime.setHours(time[0]);
            self.currentTime.setMinutes(time[1]);
            self.render();
          }
        });

        this.value = function(hour, min) {
          var val = (hour * 60) + min;
          displayTime(val);
          $("#slider").slider("option", "value", val);
        };
      };
      self.currentTime = self.model.get("initialTime");
      var slider = new TimeSlider(self.currentTime);
      return this;
    },
    render: function() {
      var self = this;
      var groups = this.model.getGroups(self.currentTime);
      var bounds = new google.maps.LatLngBounds();
      bounds.extend(this.options.center);
      var menuSection = $("#foodTruckList");
      menuSection.empty();
      function buildGroupInfo(group, idx, letter) {
        group.index = idx;
        menuSection.append("<div class='menuSection'  id='group" + idx + "'/>");
        var section = $('#group' + idx);
        var markerText = "&nbsp;";
        markerText =
            "<img class='markerIcon' id='markerIcon" + idx + "' src='" + buildIconUrl(letter) + "'/>";
        section.append("<div class='markerSection'>" + markerText + "</div>");
        section.append("<div class='locationContent' id='location" + idx +
            "Section' class='contentSection'></div>");
        var div = $('#location' + idx + 'Section');
        div.append("<address class='locationName'>" + removeChicago(group.position.name) +
            "</address>");
        if (group.distance && self.showDistance) {
          div.append("<span>" + group.distance + " miles away</span></br></br>")
        }
        return div;
      }
      function buildIconForTruck(truck, contentDiv) {
        contentDiv.append("<div class='truckSection' id='truck" + truck.id + "'/>");
        var section = $('#truck' + truck.id);
        section.append("<div class='iconSection'><img src='" + truck.iconUrl + "'/></div>");
        section.append("<div class='menuContent' id='truck" + truck.id +
            "Section' class='contentSection'></div>");
        var div = $('#truck' + truck.id + 'Section');
        div.append("<a class='truckLink truckLink" + truck.id + "' href='#'>" + truck.name +
            "</a><br/>");
        var infoRow = "<div class='infoRow'>";

        $(".truckLink" + truck.id).click(function(evt) {
          evt.preventDefault();
          buildTruckInfoDialog(truck);
        });
        if (truck.twitterHandle) {
          infoRow += "<a target='_blank' href='http://twitter.com/" + truck.twitterHandle +
              "'><img alt='@" +
              truck.twitterHandle + "' src='/img/twitter16x16.png'/></a> ";
        }
        if (truck.facebook) {
          infoRow += "<a target='_blank' href='http://facebook.com" + truck.facebook +
              "'><img alt='" +
              truck.facebook + "' src='/img/facebook16x16.png'/></a> ";
        }
        infoRow += '</div>';
        div.append(infoRow);
      }

      function buildInfoWindow(group) {
        var contentString = "<div class='infoWindowContent'><address class='locaitonName'>" +
            group.position.name + "</address>";
        contentString = contentString + "<ul class='iconList'>"
        $.each(group.trucks, function(truckIdx, truck) {
          contentString +=
              "<li class='iconListItem' style='background-image: url(" + truck.iconUrl + ")'>" +
                  truck.name + "</li>"
        });
        contentString = contentString + "</ul></div>";
        var infowindow = new google.maps.InfoWindow({
          content: contentString
        });

        google.maps.event.addListener(group.marker, 'click', function() {
          infowindow.open(self.map, group.marker);
        });
        group.infowindow = infowindow;
      }
      var sorted = groups.sort(function (a, b) {
        if (typeof a.distance == "undefined" || a.distance == null) {
          return 0;
        }
        return a.distance > b.distance ? 1 : ((a.distance == b.distance) ? 0 : -1);
      });
      $.each(sorted, function(groupIndex, group) {
        var letter = String.fromCharCode(65 + groupIndex);
        buildMarker(group, letter, bounds, self.map);
        var contentDiv = buildGroupInfo(group, groupIndex, letter);
        $.each(group.trucks, function(idx, truck) {
          buildIconForTruck(truck, contentDiv);
        });
        buildInfoWindow(group);
        $("#markerIcon" + groupIndex).click(function() {
          $.each(sorted, function(gIndex, g) {
            g.infowindow.close();
            $(".menuSection").removeClass("hilightedSection");
          });
          group.infowindow.open(self.map, group.marker);
          $("#group" + groupIndex).addClass("hilightedSection");
        });
      });
      self.map.fitBounds(bounds);
      return this;
    }
  });

  function fitMapToView() {
    if (Modernizr.touch) {
      $("#left").css("overflow-y", "visible");
      $("#left").width(250);
      $("#map_wrapper").css("margin-left", "250px");
      $("#map_canvas").width($(window).width() - 250);
    } else {
      $("#right").width($("#map_canvas").width() - $("#left").width());
      $("#left").css("margin-left", "-" + $("#map_canvas").width() + "px");
      $("#map_canvas").height($(window).height() - $("header").height());
      $("#right").height($("#map_canvas").height());
      $("#left").height($("#right").height());
      $("#foodTruckList").height($("#left").height() - $("#sliderContainer").height() -
          $("header").height() - 75);
      $("#body").height($("#body").height() - $("header").height());
    }
  }

  function loadWithoutGeo(view, date, center) {
    view.showDistance = false;
    var truckObj = new Trucks(view, center);
    truckObj.loadTrucks(date, null);
  }

  function loadAllTrucks(view, date, center) {
    // this isn't very practical on the desktop and takes a while on firefox
    if (Modernizr.geolocation && Modernizr.touch) {
      navigator.geolocation.getCurrentPosition(function(position) {
        var currentLocation = new google.maps.LatLng(position.coords.latitude,
            position.coords.longitude);
        var truckObj = new Trucks(view, currentLocation);
        truckObj.loadTrucks(date);
      }, function() {
        loadWithoutGeo(view, date, center)
      });
    } else {
      loadWithoutGeo(view, date, center);
    }
  }

  function showControlsForNoMap() {
    $("#right").css("display", "none");
    $("#left").css("overflow-y", "visible");
    $("header h1").css("float", "none");
    $("#buttonSection").css("float", "none");
    $("#body").css("clear", "none");
    $("#left").css("margin-left", "0");
    $("#left").css("overflow-y", "visible");
    $(".sliderContainer").css("display", "none");
    $(".timeSelect").css("display", "block");
  }

  function showControlsForMap() {
    $(".sliderContainer").css("display", "block");
    $("hr").css("display", "block");
  }

  function displayTime(time) {
    $("#timeValue").css("display", "inline");
    var minutes = (time.getMinutes() < 10) ? ("0" + time.getMinutes()) : time.getMinutes();
    $("#timeValue").append("at " + time.getHours() + ":" + minutes);
  }

  return {
    isTouchScreenLandscape : function() {
      return Modernizr.touch && window.innerWidth > window.innerHeight;
    },
    isTouchScreenPortrait : function() {
      return Modernizr.touch && window.innerHeight > window.innerWidth;
    },
    run : function(mobile, center, time, modelPayload) {
      if (this.isTouchScreenPortrait() || mobile) {
        this.loadTrucksWithoutMap(center, time, modelPayload);
      } else {
        this.loadTrucksWithMap(center, time, modelPayload);
      }
    },
    loadTrucksWithoutMap : function(center, time, mobilePayload) {
      showControlsForNoMap();
      displayTime(time);
    },
    loadTrucksWithMap : function(center, time, mobilePayload) {
      $("#right").css("display", "block");
      fitMapToView();
      showControlsForMap();
      var trucks = new Trucks({initialTime: time});
      var view = new MapView({ center : center, model : trucks, el : "map_canvas"});
      trucks.bind('change:payload', function(model, payload) {
        model.linkLocations(payload);
        view.render();
      });
      trucks.set({ payload : mobilePayload });
    }
  };
}();

