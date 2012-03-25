<%@ include file="common.jsp" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
  <meta name="author" content="Andrew Violette, @aviolette/@chifoodtruckz on twitter"/>
  <meta name="description"
        content="Find food trucks on the streets of Chicago by time and location.  Results are updated in real-time throughout the day."/>
  <title>Chicago Food Truck Finder</title>
  <link rel="stylesheet" href="css/base.css"/>
  <link rel="stylesheet" href="css/main.css"/>
  <link type="text/css"
        href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/start/jquery-ui.css"
        rel="stylesheet"/>
  <script src="script/lib/modernizr-1.7.min.js"></script>
</head>
<body>
<div class="main" id="container">
  <header><h1>The Chicago Food Truck Finder</h1>

    <div id="buttonSection">
      <a href="https://twitter.com/chifoodtruckz" class="twitter-follow-button" data-button="grey"
         data-text-color="#FFF" data-link-color="#FFF">Follow @chifoodtruckz</a>
    </div>
  </header>
  <div id="body">
    <div id="right">
      <div id="map_wrapper">
        <div class="section" id="map_canvas"></div>
      </div>
    </div>
    <div id="left">
      <div class="section">
        <div id="controlSection">
          <c:if test="${not empty requestDate}">
            <h2>Schedule for ${requestDate}&nbsp;<span id="timeValue">&nbsp;</span></h2>
          </c:if>
          <div id="viewSelect">
            Show results by: <input type="radio" class="pickViewButton" checked="checked"
                                    name="pickView" id="timeViewButton"/><label
              for="timeViewButton">&nbsp;Time</label>
            <input type="radio" class="pickViewButton" name="pickView"
                   id="locationViewButton"/><label for="locationViewButton">&nbsp;Location</label>
          </div>
          <div class="sliderContainer">
            <div class="sliderTimeWrapper">Select a time: <strong><span
                id="sliderTime"></span></strong></div>
            <div id="slider"></div>
          </div>
          <div id="locationFilter">
            <input type="checkbox" id="filterLocations" checked="checked"/> &nbsp;Show results
            within <input type="text" id="radius" size="2"/> miles of <strong
              id="filterLocationName">Dearborn and Monroe</strong>.</br>
            <a href="#" id="changeLocationLink">Change my location.</a>
          </div>
          <div class="timeSelect">
            Select a time: <br/><select id="hourSelect">
            <option>1</option>
            <option>2</option>
            <option>3</option>
            <option>4</option>
            <option>5</option>
            <option>6</option>
            <option>7</option>
            <option>8</option>
            <option>9</option>
            <option>10</option>
            <option>11</option>
            <option>12</option>
          </select>
            &nbsp;<select id="minSelect">
            <option>00</option>
            <option>15</option>
            <option>30</option>
            <option>45</option>
          </select>
            &nbsp;<select id="ampmSelect">
            <option>am</option>
            <option>pm</option>
          </select>
            &nbsp;<input type="button" id="timeGoButton" value="Find Trucks"/>
          </div>
          <hr/>
        </div>
        <div id="foodTruckList">
          <div class="flash">Determining your location for optimum results...</div>
        </div>

      </div>
    </div>
  </div>
</div>
<script type="text/javascript"
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
<script>window.jQuery ||
document.write("<script src='script/lib/jquery-1.6.2.min.js'>\x3C/script>")</script>
<script type="text/javascript"
        src="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js"></script>
<script type="text/javascript" src="script/lib/underscore-min.js"></script>
<script type="text/javascript" src="script/lib/backbone-min.js"></script>
<script type="text/javascript" src="script/map.js?ver=30"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run(${mobile}, new google.maps.LatLng(${center.latitude}, ${center.longitude}),
        new Date(${requestTimeInMillis}), ${payload});
  });
</script>

<jsp:include page="include/truckInfoDialog.jsp"/>
<%-- brighttag script --%>
<script src="//s.btstatic.com/tag.js">{
  site: "zIOrUTR"
}</script>
<noscript>
  <iframe src="//s.thebrighttag.com/iframe?c=zIOrUTR" width="1" height="1" frameborder="0"
          scrolling="no" marginheight="0" marginwidth="0"></iframe>
</noscript>
</body>
</html>

