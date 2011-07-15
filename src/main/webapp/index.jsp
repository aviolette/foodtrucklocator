<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<style type="text/css">
  html { height: 100% }
  body { height: 100%; margin: 0px; padding: 0px }
  #map_canvas { height: 100% }
</style>
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
       var contentString = '<div id="content">'+
                           '<img src="' + iconUrl +'"/>&nbsp;' + name

           '</div>';

       var infowindow = new google.maps.InfoWindow({
           content: contentString
       });
        google.maps.event.addListener(marker, 'click', function() {
          infowindow.open(map,marker);
        });       
     }

  <c:forEach var="stop" items="${stops}">
    buildMarker("<c:out value="${stop.truck.iconUrl}"/>", map, <c:out value="${stop.location.latitude}"/>, ${stop.location.longitude}, "${stop.truck.name}");
  </c:forEach>


  }

</script>
</head>
<body onload="initialize()">
  <div>hello world</div>
  <div id="map_canvas" style="width:100%; height:100%"></div>
</body>
</html>

