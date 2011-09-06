<%@ include file="common.jsp" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
  <link rel="stylesheet" href="css/base.css"/>
  <link rel="stylesheet" href="css/main.css"/>
  <link type="text/css" href="css/ui-lightness/jquery-ui-1.8.16.custom.css" rel="stylesheet"/>
  <script src="script/lib/modernizr-1.7.min.js"></script>
</head>
<body>
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

      <p>If you are a food truck and would like to be included in this application or have other
        suggestions,
        please contact us via our twitter account <a target="_blank"
                                                     href="http://twitter.com/chifoodtruckz">@chifoodtruckz</a>.
      </p>

      <div class="sliderContainer">
        <div id="sliderTime"></div>
        <div id="slider"></div>
      </div>
      <hr/>
      <div id="foodTruckList">
      </div>
    </div>
  </div>
</div>
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=true"></script>
<script type="text/javascript" src="script/map.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
<script>window.jQuery ||
document.write("<script src='script/lib/jquery-1.6.2.min.js'>\x3C/script>")</script>
<script type="text/javascript" src="script/lib/jquery-ui-1.8.16.custom.min.js"></script>
<c:if test="${google_analytics_ua != null}">
  <jsp:include page="include/google_analytics.jsp"/>
</c:if>
<script type="text/javascript">
  $(function() {
    var map = new TruckMap(${center.latitude}, ${center.longitude});
    map.loadTrucksForTime("${requestTime}");
    new TimeSlider(new Date(${requestTimeInMillis}), "${requestTime}".split("-")[0], map);
  });
</script>
</body>
</html>

