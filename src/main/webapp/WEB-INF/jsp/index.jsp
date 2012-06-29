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
  <link href="/bootstrap/bootstrap.css" rel="stylesheet"/>
  <link href="/css/main.css?ver=4" rel="stylesheet"/>
  <script src="script/lib/modernizr-1.7.min.js"></script>
</head>
<body>
<div id="topBar" class="topbar">
  <div class="topbar-inner">
    <div class="container-fluid">
      <a class="brand" href="/">Chicago Food Truck Finder</a>
      <ul class="nav">
        <%--
        <li><a href="#settings">Settings</a></li>
        <li><a href="#about">About</a></li>
        --%>
        <li><a target="_blank" href="http://blog.chicagofoodtruckfinder.com">Blog</a></li>
      </ul>
      <p class="pull-right"><a href="https://twitter.com/chifoodtruckz"
                               class="twitter-follow-button" data-button="grey"
                               data-text-color="#FFF" data-link-color="#FFF">Follow
        @chifoodtruckz</a></p>

      <div style="padding-right: 10px !important" class="pull-right fb-like"
           data-href="http://www.facebook.com/chicagofoodtruckfinder"
           data-send="false" data-layout="button_count" data-width="50"
           data-show-faces="false"></div>

    </div>
  </div>
</div>

<div class="container-fluid">
  <div class="sidebar">
    <div id="sidebarHeader">
      <div class="well">
        <c:if test="${not empty requestDate}">
          <h4>Schedule for ${requestDate}&nbsp;<span id="timeValue">&nbsp;</span></h4>
        </c:if>
        <div id="viewSelect">
          Show results by:
          <ul class="pills">
            <li id="timePill" class="active"><a href="#time">Time</a></li>
            <li><a href="#location">Location</a></li>
          </ul>
        </div>
        <%--
        <div id="locationControls">
          <div >
            <label for="radius">Filter trucks w/in &nbsp;</label>
            <div class="input">
              <div class="input-prepend">
                <label class="add-on active"><input id="filterLocations" checked="checked" type="checkbox"/></label>
                <input class="mini" id="radius" name="prependedInput2" size="3" type="text"> miles of your location
              </div>
            </div>
          </div>
        </div>
        --%>
        <div id="locationFilter" style="display:none">
          <input type="checkbox" id="filterLocations" checked="checked"/> &nbsp;Show results
          within <input class="mini" type="text" id="radius" size="2"/> miles of <strong
            id="filterLocationName">Dearborn and Monroe</strong>.</br>
          <a href="#" id="changeLocationLink">Change my location.</a>
        </div>
        <div id="timeControls">
          <div>Select a time</div>
          <div class="clearfix">
            <div class="input">
              <div class="inline-inputs"><select class="mini timechange" id="hourSelect">
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
              </select> <select class="mini timechange" id="minSelect">
                <option>00</option>
                <option>15</option>
                <option>30</option>
                <option>45</option>
              </select> <select class="mini timechange" id="ampmSelect">
                <option>am</option>
                <option>pm</option>
              </select>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="alert-message warning" style="display:none" id="flash"></div>
    </div>
    <div>

      <dl id="foodTruckList">

      </dl>
    </div>
  </div>
  <div class="content">
    <div id="map_wrapper">
      <div class="section" id="map_canvas"></div>
    </div>
  </div>
</div>
<script type="text/javascript"
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
<script>window.jQuery ||
document.write("<script src='script/lib/jquery-1.6.2.min.js'>\x3C/script>")</script>
<script type="text/javascript" src="script/lib/underscore-min.js"></script>
<script type="text/javascript" src="script/lib/backbone-min.js"></script>
<script type="text/javascript" src="/bootstrap/js/bootstrap-modal.js"></script>
<script type="text/javascript" src="script/map.js?ver=40"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run(${mobile}, new google.maps.LatLng(${center.latitude}, ${center.longitude}),
        new Date(${requestTimeInMillis}), ${payload});
  });
</script>
<%-- truck dialog // TODO: move to separate JSP --%>
<div id="truckDialog" class="modal hide fade">
  <div class="modal-header">
    <a href="#" class="close">&times;</a>

    <h3 id="truckTitle"></h3>
  </div>
  <div class="modal-body">
    <div id="truckIconDiv" class="truckSection">
      <div class="iconSection">
        <img id="truckIcon"/>
      </div>
      <div class="menuContent">
        <div id="truckSocial" class="infoRow"></div>
      </div>
    </div>
    <div id="truckInfo"></div>
    <h3>Scheduled Stops</h3>
    <ul id="truckSchedule"></ul>
  </div>
</div>
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

