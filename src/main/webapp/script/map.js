window.FoodTruckLocator = function() {
  function buildIconUrl(letter) {
    return "http://www.google.com/mapfiles/marker" + letter + ".png"
  }

  function setCookie(name, value, days) {
    if (days) {
      var date = new Date();
      date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
      var expires = "; expires=" + date.toGMTString();
    }
    else var expires = "";
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
    };
  };

  var Trucks = {
    setPayload : function(payload) {
      this.payload = payload;
    },
    setCenter : function(center) {
      this.center = center;
    },
    getTrucks : function() {
      var self = this;
      if (typeof(this.trucks) != "undefined") {
        return this.trucks;
      }
      var payload = self.payload;
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
    getLocationGroups : function() {
      var self = this, groups = [],groupMap = {};
      var payload = this.payload;
      var trucks = self.getTrucks();
      var center = this.center;
      $.each(payload.stops, function(idx, stop) {
        var truckGroup = groupMap[stop.location.name];
        if (typeof(truckGroup) == "undefined") {
          var latlng = new google.maps.LatLng(stop.location.latitude,
              stop.location.longitude);
          truckGroup = new TruckGroup({name: stop.location.name, latLng : latlng });
          if (center) {
            distance = google.maps.geometry.spherical.computeDistanceBetween(center,
                truckGroup.position.latLng, 3959);
            truckGroup.distance = Math.round(distance * 100) / 100;
          }
          groupMap[stop.location.name] = truckGroup;
          groups.push(truckGroup);
        }
        truckGroup.addTruck({startTime : stop.startTime, truck: trucks[stop.truckId]});
      });
      return groups;
    },
    getGroups : function(time) {
      var self = this, groups = [],groupMap = {};
      var payload = this.payload;
      var trucks = self.getTrucks();
      var center = this.center;
      $.each(payload.stops, function(idx, stop) {
        if (time >= stop.startMillis && time < stop.endMillis) {
          var truckGroup = groupMap[stop.location.name];
          if (typeof(truckGroup) == "undefined") {
            var latlng = new google.maps.LatLng(stop.location.latitude,
                stop.location.longitude);
            truckGroup = new TruckGroup({name: stop.location.name, latLng : latlng });
            if (center) {
              distance = google.maps.geometry.spherical.computeDistanceBetween(center,
                  truckGroup.position.latLng, 3959);
              truckGroup.distance = Math.round(distance * 100) / 100;
            }
            groupMap[stop.location.name] = truckGroup;
            groups.push(truckGroup);
          }
          truckGroup.addTruck(trucks[stop.truckId]);
        }
      });
      return groups;
    }
  };

  var ListView = Backbone.View.extend({
    initialize : function() {
      var self = this;
      self.showControls();
      self.currentTime = self.options.initialTime;
      self.displayTime(self.currentTime);
      self.setupTimeSelector();
    },
    showControls : function() {
      $("#right").css("display", "none");
      $("#left").css("overflow-y", "visible");
      $("header h1").css("float", "none");
      $("#buttonSection").css("float", "none");
      $("#body").css("clear", "none");
      $("#left").css("margin-left", "0");
      $("#left").css("overflow-y", "visible");
      $(".sliderContainer").css("display", "none");
      $(".timeSelect").css("display", "block");
    },
    displayTime : function(time) {
      $("#timeValue").css("display", "inline");
      var minutes = (time.getMinutes() < 10) ? ("0" + time.getMinutes()) : time.getMinutes();
      $("#timeValue").append("at " + time.getHours() + ":" + minutes);
    },
    render : function() {
      var self = this;
      var groups = self.groups = this.model.getGroups(self.currentTime);
      var menuSection = $("#foodTruckList");
      menuSection.empty();
      if (groups.length == 0) {
        menuSection
            .append("<div class='flash'>There are presently no food trucks on the road.  Most are on the road around 11:30.</div>");
        return;
      }
      var sorted = groups.sort(distanceSort);
      $.each(sorted, function(groupIndex, group) {
        menuSection.append("<div class='truckGroup' id='group" + groupIndex + "'></div>");
        var section = $("#group" + groupIndex);
        section.append("<div class='locationContent' id='location" + groupIndex +
            "Section' class='contentSection'></div>");
        var div = $('#location' + groupIndex + 'Section');
        div.append("<address class='locationName mobile'>" + group.position.name + "</address>");
        if (group.distance) {
          div.append("<span>" + group.distance + " miles away</span></br>")
        }
        div
            .append(" <a style='font-size:1.2em;font-weight: bold' href='http://maps.google.com/maps?q=" +
            group.position.latLng.lat() + "," +
            group.position.latLng.lng() + "'>view map</a><br/><br/>")
        $.each(group.trucks, function(idx, truck) {
          div.append("<div class='truckSectionTextOnly' id='truck" + truck.id + "'></div>");
          var truckDiv = $('#truck' + truck.id);
          truckDiv.append("<div class='iconSection'><img src='" + truck.iconUrl + "'/></div>");
          truckDiv.append("<div id='truckLeft" + truck.id + "' class='truckLeft'></div>");
          var truckLeft = $("#truckLeft" + truck.id);

          truckLeft.append("<strong>" + truck.name + "</strong><br/>");
          var infoRow = "<div class='infoRow'>";

          if (truck.twitterHandle) {
            infoRow += "<a target='_blank' href='http://twitter.com/" + truck.twitterHandle +
                "'><img alt='@" +
                truck.twitterHandle + "' src='/img/twitter32x32.png'/></a> ";
          }
          if (truck.facebook) {
            infoRow += "<a target='_blank' href='http://facebook.com" + truck.facebook +
                "'><img alt='" +
                truck.facebook + "' src='/img/facebook32x32.png'/></a> ";
          }
          if (truck.foursquare) {
            infoRow += "<a href='http://m.foursquare.com/venue/" + truck.foursquare +
                "'><img alt='Checkin on foursquare' src='/img/foursquare32x32.png'/></a>";
          }
          infoRow += '</div>';
          truckLeft.append(infoRow);
        });
      });
      $(".locationContent").css("margin", "0");

    },
    setupTimeSelector : function() {
      var self = this, ampm = "am";
      var hours = self.currentTime.getHours();
      if (hours > 12) {
        hours = hours - 12;
        ampm = "pm";
      }
      var minutes = Math.floor(self.currentTime.getMinutes() / 15) * 15;
      $("#hourSelect").val(hours);
      $("#minSelect").val(minutes);
      $("#ampmSelect").val(ampm);
      $("#timeGoButton").live("click", function() {
        var selectedHour = parseInt($("#hourSelect").val());
        var selectedMin = parseInt($("#minSelect").val());
        var selectedAmPm = $("#ampmSelect").val();
        if (selectedAmPm == "pm" && parseInt(selectedHour) != 12) {
          selectedHour = parseInt(selectedHour) + 12;
        }
        self.currentTime.setHours(selectedHour);
        self.currentTime.setMinutes(selectedMin);
        self.render();
      });
    }
  });

  var BaseMapView = {
    initializeMap : function(map) {
      var self = this;
      self.fitMapToView();
      self.map = map;
      return this;
    },
    removeAllMarkers : function() {
      var groups = this.groups || [];
      $.each(groups, function(idx, group) {
        if (group.marker) {
          group.marker.setMap(null);
          group.marker = null;
        }
      });
      this.groups = [];
    },
    fitMapToView :function() {
      $("#right").css("display", "block");
      if (Modernizr.touch) {
        $("#left").css("overflow-y", "visible");
        $("#left").width(250);
        $("#map_wrapper").css("margin-left", "250px");
        $("#map_canvas").width($(window).width() - 250);
      } else {
        $("#right").width($(window).width() - $("#left").width());
        $("#left").css("margin-left", "-" + $("#map_canvas").width() + "px");
        $("#map_canvas").height($(window).height() - $("header").height());
        $("#right").height($("#map_canvas").height());
        $("#left").height($("#right").height());
        $("#left").css("min-height", $("#right").height() + "px");
        $("#foodTruckList").height($("#right").height() - $(".sliderContainer").height() -
            $("header").height() - this.headerHeight());
        $("#body").height($("#body").height() - $("header").height());
      }
    },
    buildGroupInfo : function(group, idx, letter, menuSection) {
      group.index = idx;
      menuSection.append("<div class='menuSection'  id='group" + idx + "'/>");
      var section = $('#group' + idx);
      var markerText = "&nbsp;";
      markerText =
          "<img class='markerIcon' id='markerIcon" + idx + "' src='" + buildIconUrl(letter) +
              "'/>";
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
    },
    buildTruckInfoLink : function(div, truck) {
      var self = this;
      div.append("<a class='truckLink truckLink" + truck.id + "' href='#'>" + truck.name +
          "</a><br/>");

      $(".truckLink" + truck.id).click(function(evt) {
        evt.preventDefault();
        self.buildTruckInfoDialog(truck);
      });
    },
    buildIconForTruck : function(truck, contentDiv, prefix) {
      contentDiv.append("<div class='truckSection' id='truck" + prefix + truck.id + "'/>");
      var section = $('#truck' + prefix + truck.id);
      section.append("<div class='iconSection'><img src='" + truck.iconUrl + "'/></div>");
      section.append("<div class='menuContent' id='truck" + prefix + truck.id +
          "Section' class='contentSection'></div>");
      var div = $('#truck' + prefix + truck.id + 'Section');

      this.buildTruckInfoLink(div, truck);
      var infoRow = "<div class='infoRow'>";
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
    },
    setupMarkerBounce : function(groupIndex, marker) {
      if (marker.getAnimation() != null) {
        return;
      }
      $("#markerIcon" + groupIndex).click(function() {
        marker.setAnimation(google.maps.Animation.BOUNCE);
        setTimeout(function() {
          marker.setAnimation(null);
        }, 3000);
      });
    },
    buildTruckInfoDialog : function(truck) {
      var $truckDialog = $("#truckDialog");
      $("#truckIcon").attr("src", truck.iconUrl);
      var $truckSocial = $("#truckSocial");
      $truckSocial.empty();
      if (truck.twitterHandle) {
        $truckSocial.append("<a target='_blank' href='http://twitter.com/" + truck.twitterHandle +
            "'><img alt='@" +
            truck.twitterHandle + "' src='/img/twitter32x32.png'/></a> ");
      }
      if (truck.foursquare) {
        $truckSocial.append("<a target='_blank' href='http://foursquare.com/venue/" +
            truck.foursquare +
            "'><img alt='Checkin on foursquare' src='/img/foursquare32x32.png'/></a> ");
      }
      if (truck.facebook) {
        $truckSocial.append("<a target='_blank' href='http://facebook.com" + truck.facebook +
            "'><img alt='" +
            truck.facebook + "' src='/img/facebook32x32.png'/></a> ");
      }
      var $truckInfo = $("#truckInfo");
      $truckInfo.empty();
      if (truck.url) {
        $truckInfo.append("<h3>Website</h3><a target='_blank' href='" + truck.url + "'>" +
            truck.url + "</a>");
      }
      $.ajax({
        url: "/service/schedule/" + truck.id,
        context: document.body,
        dataType: 'json',
        success: function(data) {
          var $truckSchedule = $("#truckSchedule");
          $truckSchedule.empty();
          $.each(data.stops, function(idx, stop) {
            $truckSchedule.append("<li>" + stop.startTime + " " + stop.location.name + "</li>")
          });
          $truckDialog.dialog({minWidth: 500, modal: true, title: truck.name});
        }});
    }
  };

  var LocationView = Backbone.View.extend($.extend({
    initialize : function(options) {
      this.groups = [];
      this.geocoder = new google.maps.Geocoder();
      this.defaultFilterParams = { filter : true, locationName: 'Dearborn and Monroe, Chicago, IL',
        radius: 10, locationCoords: { lat: 41.8807438, lng: -87.6293867} };
      var self = this;
      $("#radius").live("change", function(e) {
        self.saveRadius(parseFloat($(this).attr("value")));
        self.render();
      });
      $("#filterLocations").live("change", function(e) {
        self.persistLocationParams();
        self.render();
      });
      $("#changeLocationLink").live("click", function(e) {
        e.preventDefault();
        var addr = prompt("Please enter an address or intersection", null);
        if (!addr) {
          return;
        }
        if (!addr.match(/,/)) {
          addr = addr + ", Chicago, IL";
        }
        self.geocoder.geocode({ 'address': addr }, function(results, status) {
          if (status == google.maps.GeocoderStatus.OK) {
            self.defaultFilterParams['locationName'] = addr;
            self.defaultFilterParams['locationCoords'] = { lat : results[0].geometry.location.lat(),
              lng : results[0].geometry.location.lng() };
            self.model.setCenter(results[0].geometry.location);
            self.persistLocationParams();
            self.render();

          } else {
            alert("Unable to geocode your address");
          }
        });
      })
    },
    headerHeight : function() {
      return 100;
    },
    showControlsForLocation : function() {
      $(".sliderContainer").css("display", "none");
      $("hr").css("display", "block");
      $("#locationFilter").css("display", "block");
      var locationSetup = this.getFilterParams();
      $("#radius").attr("value", locationSetup.radius);
      $("#filterLocationName").html(locationSetup.locationName);
    },
    getFilterParams : function() {
      var radius = parseFloat(getCookie("radius"));
      if (radius && radius != NaN) {
        this.defaultFilterParams['radius'] = parseFloat(radius);
      } else {
        this.defaultFilterParams['radius'] = 10;
      }
      var filter = getCookie("filter");
      this.defaultFilterParams.filter = "false" != filter;

      var locationName = getCookie("locationName");
      if (locationName) {
        this.defaultFilterParams['locationName'] = locationName;
      }
      var latitude = getCookie("latitude");
      var longitude = getCookie("longitude");
      if (latitude && longitude) {
        this.defaultFilterParams["locationCoords"] =
        { lat : parseFloat(latitude), lng : parseFloat(longitude) };
      }
      return this.defaultFilterParams;
    },
    getDefaultFilterParams : function() {
      return this.defaultFilterParams;
    },
    persistLocationParams : function() {
      setCookie("filter", this.defaultFilterParams['filter'], 365);
      setCookie("radius", this.defaultFilterParams['radius'], 365);
      setCookie("locationName", this.defaultFilterParams['locationName'], 365);
      setCookie("latitude", this.defaultFilterParams['locationCoords']['lat'], 365);
      setCookie("longitude", this.defaultFilterParams['locationCoords']['lng'], 365);
    },
    saveRadius : function(radius) {
      this.defaultFilterParams['radius'] = radius;
      this.persistLocationParams();
    },
    filterLocations : function(groups) {
      if (!$("#filterLocations").is(":checked")) {
        return groups;
      }
      var filtered = [];
      var filterParams = this.getFilterParams();
      var latlng = new google.maps.LatLng(filterParams.locationCoords.lat,
          filterParams.locationCoords.lng);
      $.each(groups, function(index, group) {
        var distance = google.maps.geometry.spherical.computeDistanceBetween(latlng,
            group.position.latLng, 3959);
        if (distance <= filterParams.radius) {
          filtered.push(group);
        }
      });
      return filtered;
    },
    render : function() {
      var self = this;
      this.showControlsForLocation();
      this.initializeMap(this.options.map);
      self.removeAllMarkers();
      var groups = self.groups = self.filterLocations(this.model.getLocationGroups());
      var bounds = new google.maps.LatLngBounds();
      bounds.extend(this.options.center);
      var menuSection = $("#foodTruckList");
      menuSection.empty();
      if (groups.length == 0) {
        menuSection
            .append("<div class='flash'>Wow, there are no trucks out on the road today.  That sucks!</div>");
        return;
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
        var contentDiv = self.buildGroupInfo(group, groupIndex, letter, menuSection);
        var lastTime = null;
        $.each(group.trucks, function(idx, truckTime) {
          if (lastTime != truckTime.startTime) {
            contentDiv.append("<h3>" + truckTime.startTime + "</h3><br/>");
          }
          lastTime = truckTime.startTime;
          self.buildIconForTruck(truckTime.truck, contentDiv, "timeIdx" + groupIndex + "-" + idx);
        });
        $("#markerIcon" + groupIndex).click(function() {
          group.marker.setAnimation(google.maps.Animation.BOUNCE);
          setTimeout(function() {
            group.marker.setAnimation(null);
          }, 3000);
        });
      });
      self.map.fitBounds(bounds);

    }
  }, BaseMapView));

  var TimeView = Backbone.View.extend($.extend({
    initialize : function() {
      this.groups = [];
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
      self.currentTime = self.options.initialTime;
      self.slider = new TimeSlider(self.currentTime);
      return this;
    },
    headerHeight : function() {
      return 75;
    },
    showControlsForMap : function() {
      $(".sliderContainer").css("display", "block");
      $("hr").css("display", "block");
      $("#locationFilter").css("display", "none");
    },
    render: function() {
      var self = this;
      this.showControlsForMap();
      this.initializeMap(this.options.map);
      self.removeAllMarkers();
      var groups = self.groups = this.model.getGroups(self.currentTime);
      var bounds = new google.maps.LatLngBounds();
      bounds.extend(this.options.center);
      var menuSection = $("#foodTruckList");
      menuSection.empty();
      if (groups.length == 0) {
        menuSection
            .append("<div class='flash'>Presently, there are no food trucks on the road. Most food trucks come on the streets at <a href='#' id='advanceTime'>11:30</a> or so.</div>");
        $("#advanceTime").live("click", function(e) {
          e.preventDefault();
          self.slider.value(11, 30);
        });
        return;
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
        var contentDiv = self.buildGroupInfo(group, groupIndex, letter, menuSection);
        $.each(group.trucks, function(idx, truck) {
          self.buildIconForTruck(truck, contentDiv, "");
        });
        self.setupMarkerBounce(groupIndex, group.marker);
      });
      self.map.fitBounds(bounds);
      self.initialized = true;
      return this;
    }
  }, BaseMapView));


  return {
    isTouchScreenLandscape : function() {
      return Modernizr.touch && window.innerWidth > window.innerHeight;
    },
    isTouchScreenPortrait : function() {
      return Modernizr.touch && window.innerHeight > window.innerWidth;
    },
    pickView : function(trucks, mobile, center, time) {
      var self = this;
      if ($("#timeViewButton:checked").val() == "on") {
        this.currentView = this.timeView;
      } else {
        this.currentView = this.locationView;
      }
      return this.currentView;
    },
    setupView : function(trucks, mobile, center, time) {
      var self = this;
      if (Modernizr.touch || mobile) {
        this.currentView =
            new ListView({initialTime: time, center : center, model : trucks, el: "foodTruckList"});
      } else {
        if (typeof(this.map) == 'undefined') {
          this.map = new google.maps.Map(document.getElementById("map_canvas"), {
            zoom: 13,
            center: center,
            maxZoom : 18,
            mapTypeId: google.maps.MapTypeId.ROADMAP
          });
        }
        var initParams = { initialTime: time, map: this
            .map, center : center, model : trucks, el : "map_canvas"};
        this.timeView = new TimeView(initParams);
        this.locationView = new LocationView(initParams);
        this.pickView(trucks, mobile, center, time);
      }
      return self.currentView;
    },
    load : function(mobile, center, time, modelPayload) {
      // TODO: remove this logic once we have an equiv. view for mobile
      var self = this, trucks = Trucks;
      trucks.setCenter(center);
      if (Modernizr.touch || mobile) {
        $("#viewSelect").css("display", "none");
      } else {
        $(".pickViewButton").click(function() {
          self.currentView.removeAllMarkers();
          self.pickView(trucks, mobile, center, time).render();
        });
      }
      trucks.linkLocations(modelPayload);
      this.setupView(trucks, mobile, center, time);
      trucks.setPayload(modelPayload);
      this.currentView.render();
    },
    run : function(mobile, center, time, modelPayload) {
      var self = this;
      self.currentView = null;
      if (Modernizr.geolocation && Modernizr.touch) {
        navigator.geolocation.getCurrentPosition(function(position) {
          var currentLocation = new google.maps.LatLng(position.coords.latitude,
              position.coords.longitude);
          self.load(mobile, currentLocation, time, modelPayload);
        }, function() {
          self.load(mobile, center, time, modelPayload);
        });
      } else {
        self.load(mobile, center, time, modelPayload);
      }
    }
  };
}();

