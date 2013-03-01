window.FoodTruckLocator = function() {
  function buildIconUrl(letter) {
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

  function truckNameSort(a, b) {
    if (typeof a.name == "undefined" || a.name == null) {
      return 0;
    }
    return a.name > b.name ? 1 : (a.name == b.name ) ? 0 : -1;
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

  function flash(message, type) {
    var $flash = $("#flash");
    $flash.empty();
    $flash.append("<p>" + message + "</p>");
    $flash.addClass(type);
    $flash.css("display", "block");
  }

  function dismissFlash() {
    var $flash = $("#flash");
    $flash.css("display", "none");
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
          truckGroup = new TruckGroup({name: stop.location.name, latLng : latlng,
            description : stop.location.description });
          if (center) {
            distance = google.maps.geometry.spherical.computeDistanceBetween(center,
                truckGroup.position.latLng, 3959);
            truckGroup.distance = Math.round(distance * 100) / 100;
          } else {
            truckGroup.distance = null;
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
            truckGroup =
                new TruckGroup({name: stop.location.name, latLng : latlng, description: stop
                    .location.description });
            if (center) {
              distance = google.maps.geometry.spherical.computeDistanceBetween(center,
                  truckGroup.position.latLng, 3959);
              truckGroup.distance = Math.round(distance * 100) / 100;
            } else {
              truckGroup.distance = null;
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
      $("#map_wrapper").css("display", "none");
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
        flash("There are presently no food trucks on the road.  Most are on the road around 11:30.");
        return;
      }
      var sorted = groups.sort(distanceSort);
      $.each(sorted, function(groupIndex, group) {
        menuSection.append("<dt class='truckGroup' id='group" + groupIndex + "'></dt>");
        var section = $("#group" + groupIndex);
        section.append("<address class='locationName mobile'>" + group.position.name +
            "</address>");
        if (group.distance != null) {
          menuSection.append("<dd>" + group.distance + " miles away</dd>")
        }
        menuSection
            .append("<dd><a style='font-size:1.2em;font-weight: bold' href='http://maps.google.com/maps?q=" +
            group.position.latLng.lat() + "," +
            group.position.latLng.lng() + "'>view map</a></dd>");
        if (group.position.description) {
          menuSection.append("<dd>" + group.position.description + "</dd>");
        }
        $.each(group.trucks.sort(truckNameSort), function(idx, truck) {
          menuSection.append("<dd class='truckSectionTextOnly' id='truck" + truck.id + "'></dd>");
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
            if (navigator.userAgent.match(/iPhone/i) || navigator.userAgent.match(/iPad/i) ||
                navigator.userAgent.match(/iPod/i)) {
              infoRow += "<a href='foursquare://venues/" + truck.foursquare + "'>" +
                  "<img alt='Checkin on foursquare' src='/img/foursquare32x32.png'/></a>";
            } else {
              infoRow += "<a href='http://m.foursquare.com/venue/" + truck.foursquare +
                  "'><img alt='Checkin on foursquare' src='/img/foursquare32x32.png'/></a>";
            }
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
      $(".timechange").change(function() {
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
      self.aboutDialog();
      self.mobileDialog();
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
    aboutDialog : function() {
      $("#aboutLink").click(function(e) {
        e.preventDefault();
        $("#aboutDialog").modal({ show: true, keyboard : true, backdrop: true});
      });
    },
    mobileDialog : function() {
      $("#mobileLink").click(function(e) {
        e.preventDefault();
        $("#mobileDialog").modal({ show: true, keyboard : true, backdrop: true});
      });
    },
    buildInfoWindow : function(group) {
      var self = this;
      var contentString = "<div class='infoWindowContent'><address class='locationName'>" +
          group.position.name + "</address>";
      if (group.distance != null) {
        contentString += "<p>" + group.distance + " miles from your location</p>"
      }
      contentString = contentString + "<table><tbody>"
      $.each(group.trucks, function(truckIdx, truck) {
        contentString += self.buildGroupTableRow(truck);
      });
      contentString = contentString + "</tbody></table></div>";
      var infowindow = new google.maps.InfoWindow({
        content: contentString
      });

      google.maps.event.addListener(group.marker, 'click', function() {
        infowindow.open(self.map, group.marker);
      });
      group.infowindow = infowindow;
    },
    fitMapToView :function() {
      $("#map_canvas").height($(window).height() - $("#topBar").height());
      $("#foodTruckList").height(window.innerHeight - $("#sidebarHeader").height() - 90);
    },
    buildGroupInfo : function(group, idx, letter, menuSection) {
      group.index = idx;
      menuSection.append("<dt class='menuSection'  id='group" + idx + "'/>");
      var section = $('#group' + idx);
      section.append(
          "<img class='markerIcon' id='markerIcon" + idx + "' src='" + buildIconUrl(letter) +
              "'/>&nbsp;");
      section.append(removeChicago(group.position.name));
      if (group.distance) {
        menuSection.append("<dd>" + group.distance + " miles away</dd>")
      }

      if (group.position.description) {
        menuSection.append("<dd>" + group.position.description + "</dd>");
      }

      if (group.position.url) {
        menuSection.append("<dd>" + group.position.url + "</dd>");
      }

      return section;
    },
    buildTruckInfoLink : function(div, truck) {
      var self = this;
      div.append("<img src='" + truck.iconUrl + "'/>&nbsp;<a class='truckLink truckLink" + truck.id + "' href='#'>" + truck.name +
          "</a><br/>");

      $(".truckLink" + truck.id).click(function(evt) {
        evt.preventDefault();
        self.buildTruckInfoDialog(truck);
      });
    },
    buildIconForTruck : function(truck, contentDiv, prefix) {
      contentDiv.append("<dd class='truckSection' id='truck" + prefix + truck.id + "'/>");
      var section = $('#truck' + prefix + truck.id);
      this.buildTruckInfoLink(section, truck);
      return section;
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
        url: "/services/schedule/" + truck.id,
        context: document.body,
        dataType: 'json',
        success: function(data) {
          var $truckSchedule = $("#truckSchedule");
          $truckSchedule.empty();
          $.each(data.stops, function(idx, stop) {
            $truckSchedule.append("<li>" + stop.startTime + " " + stop.location.name + "</li>")
          });
          $("#truckTitle").empty();
          $("#truckTitle").append(truck.name);
          $truckDialog.modal({ show: true, keyboard : true, backdrop: true});
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
    buildGroupTableRow : function(truckTime) {
      return "<tr><td><img src='" + truckTime.truck.iconUrl + "'/></td><td style='padding-left:10px'>" +
          truckTime.startTime + "</td><td>" + truckTime.truck.name + "</td></tr>";
    },
    buildGroupLinkItem : function(truckTime) {
      return "<li class='iconListItem' style='background-image: url(" + truckTime.truck.iconUrl +
          ")'>" +
          truckTime.startTime + " - " + truckTime.truck.name + "</li>";

    },
    headerHeight : function() {
      return 100;
    },
    showControlsForLocation : function() {
      $("#timeControls").css("display", "none");
      $("hr").css("display", "block");
      $("#locationFilter").css("display", "block");
      var locationSetup = this.getFilterParams();
      if (Modernizr.touch && window.innerWidth > window.innerHeight) {
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0");
        $("#filterLocations").removeAttr("checked");
      }
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
    nameSort: function(a, b) {
      return truckNameSort(a.truck, b.truck);
    },
    render : function() {
      var self = this;
      self.showControlsForLocation();
      self.initializeMap(self.options.map);
      self.removeAllMarkers();
      var groups = self.groups = self.filterLocations(this.model.getLocationGroups());
      var bounds = new google.maps.LatLngBounds();
      bounds.extend(this.options.center);
      var menuSection = $("#foodTruckList");
      menuSection.empty();
      if (groups.length == 0) {
        flash("Wow, there are no trucks out on the road today.  That sucks!");
        return;
      }
      dismissFlash(self);
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
        var sortedByName = group.trucks.sort(self.nameSort);
        $.each(sortedByName, function(idx, truckTime) {
          lastTime = truckTime.startTime;
          var section = self.buildIconForTruck(truckTime.truck, contentDiv,
              "timeIdx" + groupIndex + "-" + idx);
          section.prepend(truckTime.startTime + " - ");
        });
        self.buildInfoWindow(group);
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
      self.currentTime = self.options.initialTime;
      self.setupTimeSelector();
      return this;
    },
    buildGroupTableRow : function(truck) {
      return "<tr><td><img src='" + truck.iconUrl + "'/></td><td style='padding-left:10px'>" +
          truck.name + "</td></tr>";
    },
    buildGroupLinkItem : function(truck) {
      return "<li class='iconListItem' style='background-image: url(" + truck.iconUrl + ")'>" +
          truck.name + "</li>";

    },
    headerHeight : function() {
      return 75;
    },
    showControlsForMap : function() {
      $("#timeControls").css("display", "block");
      $("#locationFilter").css("display", "none");
    },
    changeTime: function() {
      var self = this;
      var selectedHour = parseInt($("#hourSelect").val());
      var selectedMin = parseInt($("#minSelect").val());
      var selectedAmPm = $("#ampmSelect").val();
      if (selectedAmPm == "pm" && parseInt(selectedHour) != 12) {
        selectedHour = parseInt(selectedHour) + 12;
      }
      self.currentTime.setHours(selectedHour);
      self.currentTime.setMinutes(selectedMin);
      self.render();
    },
    setupTimeSelector : function() {
      var self = this, ampm = "am";
      var hours = self.currentTime.getHours();
      if (hours > 12) {
        hours = hours - 12;
        ampm = "pm";
      } else if (hours == 0) {
        hours = 12;
        ampm = "am"
      }
      var minutes = Math.floor(self.currentTime.getMinutes() / 15) * 15;
      $("#hourSelect").val(hours);
      $("#minSelect").val(minutes);
      $("#ampmSelect").val(ampm);
      $(".timechange").change(function() {
        self.changeTime();
      });
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
        flash("Presently, there are no food trucks on the road. Most food trucks come on the streets at <a href='#' id='advanceTime'>11:30</a> or so.")
        $("#advanceTime").live("click", function(e) {
          e.preventDefault();
          $("#hourSelect").val("11");
          $("#minSelect").val("30");
          $("#ampmSelect").val("am");
          self.changeTime();
        });
        self.fitMapToView();
        return;
      }
      dismissFlash(self);
      self.fitMapToView();
      var sorted = groups.sort(function (a, b) {
        if (typeof a.distance == "undefined" || a.distance == null) {
          return 0;
        }
        return a.distance > b.distance ? 1 : ((a.distance == b.distance) ? 0 : -1);
      });
      $.each(sorted, function(groupIndex, group) {
        var letter = String.fromCharCode(65 + groupIndex);
        buildMarker(group, letter, bounds, self.map);
        var contentDT = self.buildGroupInfo(group, groupIndex, letter, menuSection);
        $.each(group.trucks.sort(truckNameSort), function(idx, truck) {
          self.buildIconForTruck(truck, menuSection, "");
        });
        self.setupMarkerBounce(groupIndex, group.marker);
        self.buildInfoWindow(group);
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
      if (self.isTouchScreenLandscape()) {
        this.currentView = this.locationView;
      } else if ($("#timePill").hasClass("active")) {
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
        $("ul.nav-pills a").click(function(e) {
          e.preventDefault();
          $("ul.nav-pills li").removeClass("active");
          $(this.parentElement).addClass("active");
          self.currentView.removeAllMarkers();
          self.pickView(trucks, mobile, center, time).render();
        });
      }
      trucks.linkLocations(modelPayload);
      this.setupView(trucks, mobile, center, time);
      trucks.setPayload(modelPayload);
      this.currentView.render();
      if (document.location.href.match(/#about/)) {
        $("#aboutDialog").modal({ show: true, keyboard : true, backdrop: true});
      }
      if (document.location.href.match(/#mobile/)) {
        $("#mobileDialog").modal({ show: true, keyboard : true, backdrop: true});
      }
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

