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
  $(function() {
    var map = new TruckMap(${center.latitude}, ${center.longitude});
    map.initialize();
    map.loadTrucksForTime("${requestTime}");
  });

</script>
</body>
</html>

