window.FoodTruckLocator = function() {
  function buildIconUrl(letter) {
    return "http://www.google.com/mapfiles/marker" + letter + ".png"
  }

  function buildMarker(objectOnMap,letter, bounds, map) {
    objectOnMap.marker = new google.maps.Marker({
      map: map,
      icon: buildIconUrl(letter),
      position: objectOnMap.position.latLng
    });
    bounds.extend(objectOnMap.position.latLng);
  }

  function buildMenuItem(menuSection, truck, letter, letterUsed, locationName) {
    menuSection.append("<div class='menuSection' id='" + truck.id + "'/>");
    var section = $('#' + truck.id);
    var markerText = "&nbsp;";
    if (!letterUsed) {
      markerText = "<img src='" + buildIconUrl(letter) + "'/>";
    }
    section.append("<div class='markerSection'>" + markerText + "</div>");
    section.append("<div class='iconSection'> <img src='" + truck.iconUrl + "'/></div>")
    section.append("<div class='menuContent' id='" + truck.id +
        "Section' class='contentSection'></div>");
    var div = $('#' + truck.id + 'Section');
    div.append("<a class='activationLink' href='/" +  truck.id + "'>" + truck.name + "</a><br/> ")
    if (locationName) {
      div.append("<span class='locationName'>" + locationName + "</span></br>");
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

  var Trucks = function(truckListener, currentTime) {
    var self = this;
    var groups = [];
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
              $.each(truckGroup.trucks, function(truckIdx, truck) {
                group.addTruck(truck);
              });
              truckListener.groupAdded(group, idx);
              groups.push(group);
            }
          });
          truckListener.finished();
        }
      })
    };
    self.loadTrucks(currentTime);
    truckListener.trucks = self;
  };

  var TruckGroupMap = function(center, requestTime, requestDate) {
    var self = this;
    var map = new google.maps.Map(document.getElementById("map_canvas"), {
      zoom: 13,
      center: center,
      maxZoom : 15,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    });

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

    self.groupAdded = function(group, groupIndex) {
      var letter = String.fromCharCode(65 + groupIndex);
      buildMarker(group, letter, bounds, map);
      $.each(group.trucks, function(idx, truck) {
        buildMenuItem(menuSection, truck, letter, idx > 0, group.position.name);
      });
    };

    self.finished = function() {
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

  return {
    loadTrucksWithMap : function(center, time, date) {
      var truckView = new TruckGroupMap(center, time, date.split("-")[0]);
      var trucks = new Trucks(truckView, date);
    },
    loadTruckSchedule : function(truckId, center) {
      $(".sliderContainer").css("display", "none");
      var truckView = new ScheduleMap(center);
      var schedule = new Schedule(truckView);
      schedule.loadTruckSchedule(truckId);
    }
  };
}();

