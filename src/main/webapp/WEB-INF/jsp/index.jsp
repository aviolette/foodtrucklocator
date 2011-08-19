<%@ include file="common.jsp" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
  <link rel="stylesheet" href="css/base.css"/>
  <link rel="stylesheet" href="css/main.css"/>

  <script type="text/javascript"
          src="http://maps.google.com/maps/api/js?sensor=true">
  </script>
  <script type="text/javascript">
    function initialize() {
      var latlng = new google.maps.LatLng(${center.latitude}, ${center.longitude});
      var myOptions = {
        zoom: 14,
        center: latlng,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      };
      var map = new google.maps.Map(document.getElementById("map_canvas"),
          myOptions);

      function buildMarker(iconUrl, map, lat, lng, name) {
        var latLng = new google.maps.LatLng(lat, lng);
        var marker = new google.maps.Marker({
          map: map,
          position: latLng
        });
        var contentString = '<div id="content">' +
            '<img src="' + iconUrl + '"/>&nbsp;' + name

        '</div>';
        var infowindow = new google.maps.InfoWindow({
          content: contentString
        });
        google.maps.event.addListener(marker, 'click', function() {
          infowindow.open(map, marker);
        });
      }

    <c:forEach var="stop" items="${stops}">
      buildMarker("<c:out value="${stop.truck.iconUrl}"/>", map,
          <c:out value="${stop.location.latitude}"/>, ${stop.location.longitude},
          "${stop.truck.name}");
    </c:forEach>
    }
  </script>
  <c:if test="${google_analytics_ua != null}">
    <jsp:include page="include/google_analytics.jsp"/>
  </c:if>
</head>
<body onload="initialize()">
<div class="main" id="container">
  <header>
    <div class="section"><h1>Chicago Food Truck Locator</h1></div>
  </header>
  <div id="right">
    <div id="contentWrapper">
      <div class="section" id="map_canvas"></div>
    </div>
  </div>
  <div id="left">
    <div class="section">Left column</div>
  </div>
  <footer>Footer</footer>
</div>
</body>
</html>

