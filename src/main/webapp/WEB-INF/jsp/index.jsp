<%@ include file="common.jsp" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
  <title>Chicago Food Truck Locator</title>
  <link rel="stylesheet" href="css/base.css"/>
  <link rel="stylesheet" href="css/main.css"/>
  <link type="text/css" href="css/start/jquery-ui-1.8.16.custom.css" rel="stylesheet"/>
  <script src="script/lib/modernizr-1.7.min.js"></script>
</head>
<body>
<div class="main" id="container">
  <header><h1>The Chicago Food Truck Locator</h1></header>
  <div id="right">
    <div id="map_wrapper">
      <div class="section" id="map_canvas"></div>
    </div>
  </div>
  <div id="left">
    <div class="section">
      <c:if test="${not empty requestDate}">
        <h2>Schedule for ${requestDate}</h2>
      </c:if>
      <div class="sliderContainer">
        <div class="sliderTimeWrapper">Select a time: <span id="sliderTime"></span></div>
        <div id="slider"></div>
      </div>
      <hr/>
      <div id="foodTruckList">
        <div class="flash">Determining your location for optimum results...</div>
      </div>

    </div>
  </div>
</div>
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<script type="text/javascript" src="script/map.js?ver=5"></script>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
<script>window.jQuery || document.write("<script src='script/lib/jquery-1.6.2.min.js'>\x3C/script>")</script>
<script type="text/javascript" src="script/lib/jquery-ui-1.8.16.custom.min.js"></script>
<c:if test="${google_analytics_ua != null}">
  <jsp:include page="include/google_analytics.jsp"/>
</c:if>
<script type="text/javascript">
  $(function() {
    FoodTruckLocator.center = new google.maps.LatLng(${center.latitude}, ${center.longitude});
    <c:choose>
      <c:when test="${empty showScheduleFor}">
        if ($(window).height < 500 && $(window.width) < 400) {
          FoodTruckLocator.loadTrucksWithoutMap(new Date(${requestTimeInMillis}), "${requestTime}");
        } else {
          FoodTruckLocator.loadTrucksWithMap(new Date(${requestTimeInMillis}), "${requestTime}");
        }
      </c:when>
      <c:otherwise>
        FoodTruckLocator.loadTruckSchedule("${showScheduleFor}");
      </c:otherwise>
    </c:choose>
  });
</script>
</body>
</html>

