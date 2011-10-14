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

  function buildMarker(objectOnMap,letter, bounds, map) {
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

  function buildMenuItem(menuSection, truck, letter, letterUsed, locationName, distance) {
    menuSection.append("<div class='menuSection' id='" + truck.id + "'/>");
    var section = $('#' + truck.id);
    var markerText = "&nbsp;";
    if (!letterUsed) {
      markerText = "<img src='" + buildIconUrl(letter) + "'/>";
    }
    section.append("<div class='markerSection'>" + markerText + "</div>");
    section.append("<div class='iconSection'> <img src='" + truck.iconUrl + "'/></div>");
    section.append("<div class='menuContent' id='" + truck.id +
        "Section' class='contentSection'></div>");
    var div = $('#' + truck.id + 'Section');
    div.append("<a class='activationLink' href='/" +  truck.id + "'>" + truck.name + "</a><br/> ");
    if (locationName) {
      div.append("<span class='locationName'>" + locationName + "</span></br>");
    }
    if (distance) {
      div.append("<span>" + distance + " miles away</span></br>")
    }
    if (truck.url) {
      div.append("Website: <a target='_blank' href='" + truck.url + "'>" + truck.url +
          "</a><br/>")
    }
    if (truck.twitterHandle) {
      div.append("Twitter: <a target='_blank' href='http://twitter.com/" + truck.twitterHandle +
          "'>@" +
          truck.twitterHandle + "</a><br/>")
    }
  }


  var TruckGroup = function(location) {
    var self = this;
    self.trucks = [];
    self.position = location;
    self.addTruck = function(truck) {
      self.trucks.push(truck);
    }
  };

  var Trucks = function(truckListener, position) {
    var groups = [];
    var self = this;
    var currentPosition = position;
    truckListener.trucks = self;

    self.removeAll = function () {
      $.each(groups, function(idx, group) {
        truckListener.groupRemoved(group);
      });
      groups = [];
    };

    self.loadTrucks = function(requestTime) {
      self.removeAll();
      $.ajax({
        url: "/service/stops?time=" + requestTime,
        context: document.body,
        dataType: 'json',
        success: function(data) {
          truckListener.start();
          $.each(data, function(idx, truckGroup) {
            if (typeof truckGroup['location'] != 'undefined') {
              var latlng = new google.maps.LatLng(truckGroup.location.latitude,
                  truckGroup.location.longitude);
              var locationName = (typeof truckGroup.location['name'] == 'undefined') ? null :
                  truckGroup.location.name;
              var group = new TruckGroup({name: locationName, latLng : latlng });
              var distance = null;
              if (currentPosition) {
                distance = google.maps.geometry.spherical.computeDistanceBetween(currentPosition,
                    group.position.latLng, 3959);
                group.distance = Math.round(distance * 100) / 100;
              }
              $.each(truckGroup.trucks, function(truckIdx, truck) {
                group.addTruck(truck);
              });
              groups.push(group);
            }
          });
          truckListener.finished(groups);
        }
      })
    };
  };

  var TruckListView = function(center, requestTime, requestDate) {
    var self = this;
    var menuSection = $("#foodTruckList");
    self.start = function() {
      menuSection.empty();
    };

    self.groupRemoved = function(group) {
    };

    self.finished = function(groups) {
      if (groups.length == 0) {
        menuSection.append("<div class='flash'>There are currently no food trucks on the streets that we know of.</div>");
        return;
      }
      var sorted = groups.sort(distanceSort);
      $.each(sorted, function(groupIndex, group) {
        menuSection.append("<div class='truckGroup' id='group" + groupIndex +"'></div>");
        var section = $("#group" + groupIndex);
        section.append("<div class='locationContent' id='location" + groupIndex +
            "Section' class='contentSection'></div>");
        var div = $('#location' + groupIndex + 'Section');
        div.append("<address class='locationName'>" + group.position.name + "</address>");
        if (group.distance) {
          div.append("<span>" + group.distance + " miles away</span></br>")
        }
        div.append(" <a href='http://maps.google.com/maps?q="+group.position.name+"'>map</a><br/><br/>")
        $.each(group.trucks, function(idx, truck) {
          div.append("<div class='truckSectionTextOnly' id='truck" + truck.id + "'></div>");
          var truckDiv = $('#truck' + truck.id );
          truckDiv.append("<strong>" + truck.name + "</strong><br/>");
          if (truck.url) {
            truckDiv.append("Website: <a target='_blank' href='" + truck.url + "'>" + truck.url +
                "</a><br/>")
          }
          if (truck.twitterHandle) {
            truckDiv.append("Twitter: <a target='_blank' href='http://twitter.com/" + truck.twitterHandle +
                "'>@" +
                truck.twitterHandle + "</a><br/>");
          }
          if (truck.foursquare) {
            div.append("<a href='http://m.foursquare.com/venue/" + truck.foursquare +
                "'><img alt='Checkin on foursquare' src='http://playfoursquare.s3.amazonaws.com/press/logo/checkin-blue.png'/></a><br/>");
          }
        });
      });
      $(".locationContent").css("margin", "0");
    };
  };

  var TruckGroupMap = function(center, requestTime, requestDate) {
    var self = this;
    var map = new google.maps.Map(document.getElementById("map_canvas"), {
      zoom: 13,
      center: center,
      maxZoom : 15,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    });
    var trucks = null;
    var TimeSlider = function(initialTime, initialDate) {
      var sliderValue = (initialTime.getHours() * 60) +
          (Math.floor(initialTime.getMinutes() / 15) * 15);

      function computeTime(value, twelveHour) {
        // yuck - cleanup
        var val = value / 60;
        var hour = Math.floor(val);
        var min = (val - hour) * 60;
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
        min: 0,  max: 1440, value : sliderValue, step: 15,
        slide : function(event, ui) {
          displayTime(ui.value);
        },
        change: function(event, ui) {
          var time = computeTime(ui.value, false);
          self.trucks.loadTrucks(initialDate + "-" + time.join(""));
        }
      });
    };
    var slider = new TimeSlider(requestTime, requestDate);
    var bounds, menuSection = $("#foodTruckList");

    self.start = function() {
      menuSection.empty();
      bounds = new google.maps.LatLngBounds ();
      bounds.extend(center);
    };

    self.groupRemoved = function(group) {
      if (group.marker) {
        group.marker.setMap(null);
        group.marker = null;
      }
    };

    function buildGroupInfo(group, idx, letter) {
      menuSection.append("<div class='menuSection'  id='group" + idx + "'/>");
      var section = $('#group' + idx);
      var markerText = "&nbsp;";
      markerText = "<img src='" + buildIconUrl(letter) + "'/>";
      section.append("<div class='markerSection'>" + markerText + "</div>");
      section.append("<div class='locationContent' id='location" + idx +
          "Section' class='contentSection'></div>");
      var div = $('#location' + idx + 'Section');
      div.append("<address class='locationName'>" + removeChicago(group.position.name) + "</address>");
      if (group.distance) {
        div.append("<span>" + group.distance + " miles away</span></br></br>")
      }
      return div;
    }

    function buildIconForTruck(truck, contentDiv) {
      menuSection.append("<div class='truckSection' id='truck" + truck.id + "'/>");
      var section = $('#truck' + truck.id);
      section.append("<div class='markerSection'>&nbsp;</div>");
      section.append("<div class='iconSection'><img src='" + truck.iconUrl + "'/></div>");
      section.append("<div class='menuContent' id='truck" + truck.id +
          "Section' class='contentSection'></div>");
      var div = $('#truck' + truck.id + 'Section');
      div.append("<a href='/" + truck.id + "'>" + truck.name + "</a><br/>");
      if (truck.url) {
        div.append("Website: <a target='_blank' href='" + truck.url + "'>" + truck.url +
            "</a><br/>")
      }
      if (truck.twitterHandle) {
        div.append("Twitter: <a target='_blank' href='http://twitter.com/" + truck.twitterHandle +
            "'>@" +
            truck.twitterHandle + "</a><br/>")
      }
    }

    function buildInfoWindow(group) {
      var contentString = "<address class='locaitonName'>" + group.position.name + "</address>";
      contentString = contentString + "<ul>"
      $.each(group.trucks, function(truckIdx, truck) {
        contentString += "<li>" + truck.name + "</li>"
      });
      contentString = contentString + "</ul>";
      var infowindow = new google.maps.InfoWindow({
          content: contentString
      });

      google.maps.event.addListener(group.marker, 'click', function() {
        infowindow.open(map, group.marker);
      });
    }

    self.finished = function(groups) {
      if (groups.length == 0) {
        menuSection.append("<div class='flash'>There are currently no food trucks on the streets that we know of.</div>");
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
        buildMarker(group, letter, bounds, map);
        var contentDiv = buildGroupInfo(group, groupIndex, letter);
        $.each(group.trucks, function(idx, truck) {
          buildIconForTruck(truck, contentDiv);
        });
        buildInfoWindow(group);
      });
      map.fitBounds(bounds);
    };
  };

  var ScheduleMap = function(center) {
    var map = new google.maps.Map(document.getElementById("map_canvas"), {
      zoom: 13,
      center: center,
      maxZoom : 15,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    });
    var self = this;
    var bounds, truck;
    var menuSection = $("#foodTruckList");

    self.start = function(tr) {
      menuSection.empty();
      menuSection.append("<a href='/'>&#171; Back to Main List</a><br/> ")
      bounds  = new google.maps.LatLngBounds ();
      bounds.extend(center);
      truck = tr;
      buildMenuItem(menuSection, truck, null, true, null);
    };

    self.itemAdded = function(item, idx) {
      var letter = String.fromCharCode(65 + idx);
      buildMarker(item, letter, bounds, map);
      menuSection.append("<div class='menuSection' id='menu" + idx +"'/>");
      var section = $('#menu' + idx) ;
      var markerText = "<img src='" + buildIconUrl(letter) + "'/>";
      section.append("<div class='markerSection'>" + markerText + "</div>");
      section.append("<div class='menuContent' id='menu" + idx +
           "Section' class='contentSection'></div>");
      var div = $('#menu' + idx + 'Section');
      div.append(item.startTime + "<br/>");
      div.append(item.position.name);

    };

    self.finish = function() {
      map.fitBounds(bounds);
    }
  };

  var Schedule = function(scheduleListener) {
    var self = this;
    var truck = null;
    var scheduleItems = [];

    self.loadTruckSchedule = function(truckId) {
       $.ajax({
         url: "/service/schedule/" + truckId,
         context: document.body,
         dataType: 'json',
         success: function(data) {
           truck = data.truck;
            scheduleListener.start(truck);
           $.each(data.stops, function(idx, scheduleItem) {
             var latlng = new google.maps.LatLng(scheduleItem.location.latitude,
                 scheduleItem.location.longitude);
             var locationName = (typeof scheduleItem.location['name'] == 'undefined') ? null :
                 scheduleItem.location.name;
             scheduleItem.position = {name: locationName, latLng : latlng }
             scheduleItems.push(scheduleItem);
             scheduleListener.itemAdded(scheduleItem, idx);
           });
           scheduleListener.finish();
         }
       });
     };
  };

  function fitMapToView() {
    if (Modernizr.touch) {
      $("#left").css("overflow-y", "visible");
    } else {
      $("#map_canvas").height($(window).height() - $("header").height());
      $("#right").width($("#map_canvas").width() - $("#left").width());
      $("#right").height($("#map_canvas").height());
      $("#left").css("margin-left", "-" + $("#map_canvas").width() + "px");
      $("#body").height($("#body").height() - $("header").height());
    }
  }

  function loadWithoutGeo(view, date) {
    var truckObj = new Trucks(view);
    truckObj.loadTrucks(date, null);
  }

  function loadAllTrucks(view, date) {
    if (Modernizr.geolocation) {
      navigator.geolocation.getCurrentPosition(function(position) {
        var currentLocation = new google.maps.LatLng(position.coords.latitude,
            position.coords.longitude);
        var truckObj = new Trucks(view, currentLocation);
        truckObj.loadTrucks(date);
      }, function() {
        loadWithoutGeo(view, date)
      });
    } else {
      loadWithoutGeo(view, date);
    }
  }

  function hideControls() {
    $(".sliderContainer").css("display", "none");
    $("hr").css("display", "none");
  }

  function showControls() {
    $(".sliderContainer").css("display", "block");
    $("hr").css("display", "block");
  }

  function displayTime(time) {
    $("#timeValue").css("display", "inline");
    var minutes = (time.getMinutes() < 10) ? ("0" + time.getMinutes()) : time.getMinutes();
    $("#timeValue").append("at " + time.getHours() + ":" + minutes);
  }

  return {
    loadTrucksWithoutMap : function(time, date) {
      $("#right").css("display", "none");
      $("#left").css("overflow-y", "visible");
      $("header h1").css("float", "none");
      $("#buttonSection").css("float", "none");
      $("#body").css("clear", "none");
      $("#left").css("margin-left", "0");
      hideControls();
      displayTime(time);
      var truckView = new TruckListView(this.center, time, date.split("-")[0]);
      loadAllTrucks(truckView, date);
    },
    loadTrucksWithMap : function(time, date) {
      fitMapToView();
      showControls();
      var truckView = new TruckGroupMap(this.center, time, date.split("-")[0]);
      loadAllTrucks(truckView, date);
    },
    loadTruckSchedule : function(truckId) {
      fitMapToView();
      hideControls();
      var truckView = new ScheduleMap(this.center);
      var schedule = new Schedule(truckView);
      schedule.loadTruckSchedule(truckId);
    }
  };
}();

