function buildIconUrl(letter) {
  return "http://www.google.com/mapfiles/marker" + letter + ".png"
}

var TimeSlider = function(initialTime, initialDate, map) {
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
      map.clear();
      map.loadTrucksForTime(initialDate + "-" + time.join(""));
    }
  });
};

var TruckMap = function(center) {
  var trucks = [];
  var self = this;
  var map = new google.maps.Map(document.getElementById("map_canvas"), {
    zoom: 13,
    maxZoom: 15,
    center: center,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  });

  self.clear = function () {
    $.each(trucks, function(idx, truck) {
      truck.removeMarker();
    });
    trucks = [];
  };

  self.loadTruckSchedule = function(truckId) {
    $.ajax({
      url: "/service/schedule/" + truckId,
      context: document.body,
      dataType: 'json',
      success: function(data) {
        var menuSection = $("#foodTruckList");
        menuSection.empty();
        var truck = data.truck;
        var t = new Truck(null, null, truck);
        menuSection.append("<a href='/'>&#171; Back to Main List</a><br/> ")
        t.buildMenuItem(menuSection, null, true);
        var bounds = new google.maps.LatLngBounds ();
        bounds.extend(center);
        $.each(data.stops, function(idx, scheduleItem) {
          var latlng = new google.maps.LatLng(scheduleItem.location.latitude,
              scheduleItem.location.longitude);
          var locationName = (typeof scheduleItem.location['name'] == 'undefined') ? null :
              scheduleItem.location.name;
          var letter = String.fromCharCode(65 + idx);
          var truckObj = new Truck(latlng, locationName, truck);
          truckObj.buildMarker(map, letter, bounds);
          menuSection.append("<div class='menuSection' id='menu" + idx +"'/>");
          var section = $('#menu' + idx) ;
          var markerText = "<img src='" + buildIconUrl(letter) + "'/>";
          section.append("<div class='markerSection'>" + markerText + "</div>");
          section.append("<div class='menuContent' id='menu" + idx +
               "Section' class='contentSection'></div>");
          var div = $('#menu' + idx + 'Section');
          div.append(scheduleItem.startTime + "<br/>");
          div.append(locationName);
        });
        map.fitBounds(bounds);
      }
    });
  };

  self.loadTrucksForTime = function(requestTime) {
    $.ajax({
      url: "/service/stops?time=" + requestTime,
      context: document.body,
      dataType: 'json',
      success: function(data) {
        var menuSection = $("#foodTruckList");
        menuSection.empty();
        var bounds = new google.maps.LatLngBounds ();
        bounds.extend(center);
        $.each(data, function(idx, truckGroup) {
          var letter = String.fromCharCode(65 + idx);
          if (typeof truckGroup['location'] != 'undefined') {
            var latlng = new google.maps.LatLng(truckGroup.location.latitude,
                truckGroup.location.longitude);
            var locationName = (typeof truckGroup.location['name'] == 'undefined') ? null :
                truckGroup.location.name;
            $.each(truckGroup.trucks, function(truckIdx, truck) {
              var truckObj = new Truck(latlng, locationName, truck);
              truckObj.buildMarker(map, letter, bounds);
              truckObj.buildMenuItem(menuSection, letter, truckIdx != 0);
              trucks.push(truckObj);
            });
          }
        });
        map.fitBounds(bounds);
      }
    })
  }
};

var Truck = function(latLng, locationName, opts) {
  var self = this;
  var options = opts;
  var marker = null;


  self.buildMenuItem = function (menuSection, letter, letterUsed) {
    menuSection.append("<div class='menuSection' id='" + options.id + "'/>");
    var section = $('#' + options.id)
    var markerText = "&nbsp;";
    if (!letterUsed) {
      markerText = "<img src='" + buildIconUrl(letter) + "'/>";
    }
    section.append("<div class='markerSection'>" + markerText + "</div>");
    section.append("<div class='iconSection'> <img src='" + options.iconUrl + "'/></div>")
    section.append("<div class='menuContent' id='" + options.id +
        "Section' class='contentSection'></div>");
    var div = $('#' + options.id + 'Section');
    div.append("<a class='activationLink' href='/" +  options.id + "'>" + options.name + "</a><br/> ")
    if (locationName) {
      div.append("<span class='locationName'>" + locationName + "</span></br>");
    }
    if (options.url) {
      div.append("Website: <a target='_blank' href='" + options.url + "'>" + options.url +
          "</a><br/>")
    }
    if (options.twitterHandle) {
      div.append("Twitter: <a target='_blank' href='http://twitter.com/" + options.twitterHandle +
          "'>@" +
          options.twitterHandle + "</a><br/>")
    }
  };

  self.removeMarker = function() {
    marker.setMap(null);
    marker = null;
  };

  self.buildMarker = function (map, letter, bounds) {
    marker = new google.maps.Marker({
      map: map,
      icon: buildIconUrl(letter),
      position: latLng
    });
    bounds.extend(latLng);
  };
};
