<%@ include file="common.jsp" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
  <link rel="stylesheet" href="css/base.css"/>
  <link rel="stylesheet" href="css/main.css"/>
  <script src="script/lib/modernizr-1.7.min.js"></script>
  <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=true"></script>
  <script type="text/javascript" src="script/map.js"></script>
</head>
<body onload="initialize()">
<div class="main" id="container">
  <div id="right">
    <div id="map_wrapper">
      <div class="section" id="map_canvas"></div>
    </div>
  </div>
  <div id="left">
    <div class="section">
      <%-- Left column --%>
      <h1>Chicago Food Trucks</h1>

      <p>The Chicago Food Truck locator is in its infancy. Currently we locate food trucks based on
        their published schedules on their websites. Our plan is to crystallize this data further
        via other real-time technologies.</p>

      <div id="foodTruckList">
      </div>
    </div>
  </div>
</div>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.5.1/jquery.js"></script>
<script>window.jQuery ||
document.write("<script src='script/lib/jquery-1.5.1.min.js'>\x3C/script>")</script>
<c:if test="${google_analytics_ua != null}">
  <jsp:include page="include/google_analytics.jsp"/>
</c:if>
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
    var truck, stop, i = 0, letter;
    var menuSection = $("#foodTruckList");
  <c:forEach var="locationTruck" items="${trucks}" varStatus="status">
  <c:if test="${locationTruck.location != null}">
    latlng = new google.maps.LatLng(${locationTruck.location.latitude},
        ${locationTruck.location.longitude});
  <c:forEach var="truck" items="${locationTruck.trucks}">
    truck = new Truck({
      latLng: latlng,
      id : "${truck.id}",
      name: "${truck.name}",
      url: "${truck.url}",
      twitter: "${truck.twitterHandle}",
      iconUrl: "${truck.iconUrl}"
    });
    letter = String.fromCharCode(${status.index + 65});
    truck.buildMarker(map, letter);
    truck.buildMenuItem(menuSection, letter);
  </c:forEach>
  </c:if>
  </c:forEach>
  }
</script>
</body>
</html>

