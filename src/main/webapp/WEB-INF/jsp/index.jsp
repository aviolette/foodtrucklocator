<%@ include file="header.jsp" %>
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

<%@include file="include/core_js.jsp"%>
<script type="text/javascript"
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<script type="text/javascript" src="script/lib/underscore-min.js"></script>
<script type="text/javascript" src="script/lib/backbone-min.js"></script>
<script type="text/javascript" src="script/map.js?ver=40"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run(${mobile}, new google.maps.LatLng(${center.latitude}, ${center.longitude}),
        new Date(${requestTimeInMillis}), ${payload});
  });
</script>
<%@ include file="footer.jsp" %>
