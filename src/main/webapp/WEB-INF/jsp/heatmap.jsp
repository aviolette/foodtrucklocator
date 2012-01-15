<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
  <title>Google Maps JavaScript API v3 Example: Circle Simple</title>
  <link href="http://code.google.com/apis/maps/documentation/javascript/examples/default.css"
        rel="stylesheet" type="text/css"/>
  <script type="text/javascript" src="//maps.googleapis.com/maps/api/js?sensor=false"></script>
  <script type="text/javascript">

    var datapoints = <%@ include file="/heatmap/result.json" %>;
    var center = new google.maps.LatLng(41.8807438, -87.6293867);

    function initialize() {
      var mapOptions = {
        zoom: 13,
        center: center,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      };

      var map = new google.maps.Map(document.getElementById("map_canvas"),
          mapOptions);

      for (var i = 0; i < datapoints.length; i++) {
        // Construct the circle for each value in citymap. We scale population by 20.
        var city = datapoints[i];
        var populationOptions = {
          strokeColor: "#FF0000",
          strokeOpacity: 0.8,
          strokeWeight: 2,
          fillColor: "#FF0000",
          fillOpacity: 0.35,
          map: map,
          center: new google.maps.LatLng(city[0], city[1]),
          radius: 100
        };
        var cityCircle = new google.maps.Circle(populationOptions);
      }
    }
  </script>
</head>
<body onload="initialize()">
<div id="map_canvas"></div>
</body>
</html>
