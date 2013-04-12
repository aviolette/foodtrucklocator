var FoodTruckLocator = function() {
  var _map = null;
  var _trucks = null;

  var clock = {
    now : function() {
      return 1365657504767;
    }
  };

  var Trucks = function(model) {
    this.model = model;
    this.trucks = {};
    var self = this;
    for (var i=0; i < model["trucks"].length; i++) {
      this.trucks[model["trucks"][i]["id"]] = model["trucks"][i];
    }

    function buildStop(stop) {
      return {
        stop : stop,
        truck : self.trucks[stop["truckId"]],
        location : self.model["locations"][stop["location"]-1]
      }
    }

    this.openNow = function() {
      var now = clock.now(), items = [];
      $.each(model["stops"], function(idx, item) {
        if (item["startMillis"] <= now && item["endMillis"] > now) {
          items.push(buildStop(item));
        }
      });
      return items;
    };
    this.openLater = function() {
      var now = clock.now(), items = [];
      $.each(model["stops"], function(idx, item) {
        if (item["startMillis"] > now) {
          items.push(buildStop(item));
        }
      });
      return items;
    }
  };

  function buildTruckList($truckList, stops) {
    $truckList.empty();
    var items = "<ul class='unstyled'>"
    $.each(stops, function(idx, stop){
       items +=   "<li style='padding-bottom:20px'>" +
           "<table><tr><td style='vertical-align:top;padding-right:5px'>" +
           "<img src='" + stop.truck.iconUrl + "'/></td><td style='vertical-align:top'>" +
           stop.truck.name + "<br/>" +
           stop.location.name + "<br/>" +
           stop.stop.startTime + " - " + stop.stop.endTime +
           "</td></tr></table>" +
           "</li>";
    });
    $truckList.append(items + "</ul>");
  }

  return {
    updateTruckLists : function() {
      buildTruckList($("#nowTrucks"), _trucks.openNow());
      buildTruckList($("#laterTrucks"), _trucks.openLater());
    },
    updateMap : function() {

    },
    setModel : function(model) {
      _trucks = new Trucks(model);
      this.updateTruckLists();
      this.updateMap();
    },
    reload : function() {
    },
    resize : function() {
      $("#map_canvas").height($(window).height() - $("#topBar").height());
      $("#sidebar").height($(window).height() - $("#topBar").height());
      $("#listContainer").height($(window).height() - $("#topBar").height());
    },
    run : function(mobile, center, time, modelPayload) {
      var self = this;
      self.resize();
      _map = new google.maps.Map(document.getElementById("map_canvas"), {
        zoom: 13,
        center: center,
        maxZoom : 18,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      });
      self.setModel(modelPayload);
      $(window).resize(function() {
        self.resize();
      });
    }
  };
}();
