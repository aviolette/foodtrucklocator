var TimeSlider = function(initialTime, initialDate, map) {
  var sliderValue = (initialTime.getHours() * 60) +
      (Math.floor(initialTime.getMinutes() / 15) * 15);

  function computeTime(value) {
    // yuck - cleanup
    var val = value / 60;
    var hour = Math.floor(val);
    var min = (val - hour) * 60;
    if (hour > 12) {
      hour = hour - 12;
    }
    hour = (hour < 10) ? "0" + hour : "" + hour;
    min = (min < 10) ? "0" + min : "" + min;
    return [hour, min];
  }

  function displayTime(value) {
    var time = computeTime(value);
    $("#sliderTime").empty().append(time.join(":"));
  }

  displayTime(sliderValue);
  $("#slider").slider({
    min: 0,  max: 1440, value : sliderValue, step: 15,
    slide : function(event, ui) {
      displayTime(ui.value);
    },
    change: function(event, ui) {
      var time = computeTime(ui.value);
      map.clear();
      map.loadTrucksForTime(initialDate + "-" + time.join(""));
    }
  });
};

var TruckMap = function(lat, lng) {
  var latlng = new google.maps.LatLng(lat, lng);
  var trucks = [];
  var self = this;
  var map = new google.maps.Map(document.getElementById("map_canvas"), {
    zoom: 14,
    center: latlng,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  });

  self.clear = function () {
    $.each(trucks, function(idx, truck) {
      truck.removeMarker();
    });
    trucks = [];
  };

  self.loadTrucksForTime = function(requestTime) {
    $.ajax({
      url: "/service/stops?time=" + requestTime,
      context: document.body,
      dataType: 'json',
      success: function(data) {
        var menuSection = $("#foodTruckList");
        menuSection.empty();
        $.each(data, function(idx, truckGroup) {
          var letter = String.fromCharCode(65 + idx);
          if (typeof truckGroup['location'] != 'undefined') {
            var latlng = new google.maps.LatLng(truckGroup.location.latitude,
                truckGroup.location.longitude);
            var locationName = (typeof truckGroup.location['name'] == 'undefined') ? null :
                truckGroup.location.name;
            $.each(truckGroup.trucks, function(truckIdx, truck) {
              var truckObj = new Truck(latlng, locationName, truck);
              truckObj.buildMarker(map, letter);
              truckObj.buildMenuItem(menuSection, letter, truckIdx != 0);
              trucks.push(truckObj);
            });
          }
        });
      }
    })
  }
};

var Truck = function(latLng, locationName, opts) {
  var self = this;
  var options = opts;
  var marker = null;

  function buildIconUrl(letter) {
    return "http://www.google.com/mapfiles/marker" + letter + ".png"
  }

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
    div.append("<a class='activationLink' href='#'>" + options.name + "</a><br/> ")
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

  self.buildMarker = function (map, letter) {
    marker = new google.maps.Marker({
      map: map,
      icon: buildIconUrl(letter),
      position: latLng
    });
    var contentString = '<div id="content">' +
        '<img src="' + options.iconUrl + '"/>&nbsp;' + options.name

    '</div>';
    var infowindow = new google.maps.InfoWindow({
      content: contentString
    });
    google.maps.event.addListener(marker, 'click', function() {
      infowindow.open(map, marker);
    });
  };
};
