
<%@ include file="header.jsp" %>

<div class="row" id="sidebar">
  <div class="col-md-4" id="listContainer">
    <div id="motd" class="alert alert-warning alert-dismissable hidden">
      <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
      <span id="motd-message"></span>
    </div>
    <div class="alert alert-info" id="flashMsg" style="display:none">
    </div>
    <ul class="nav nav-tabs" id="navTabs">
      <li class="active"><a href="#nowTrucks" data-toggle="tab">Open Now</a></li>
      <li><a href="#laterTrucks" data-toggle="tab">Open Later</a></li>
    </ul>
    <div class="tab-content">
      <div id="nowTrucks" class="tab-pane active truckDL">
      </div>
      <div id="laterTrucks" class="tab-pane truckDL">
      </div>
    </div>
  </div>
  <div id="content" class="col-md-8">
    <div id="map_wrapper">
      <div class="section" id="map_canvas"></div>
    </div>
  </div>
</div>
<c:if test="${not mobile}">
  <%@include file="include/truck_dialog.jsp" %>
</c:if>
<%@include file="include/core_js.jsp" %>
<script type="text/javascript"
        src="//maps.google.com/maps/api/js?key=${googleApiKey}&libraries=geometry"></script>
<script type="text/javascript" src="script/foodtruckfinder-1.19.js"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run("${mode}", new google.maps.LatLng(${center.latitude}, ${center.longitude}),
        new Date(${requestTimeInMillis}), ${payload}, "${appKey}", "${defaultCity}", ${mapButtons});
  });
</script>
<%@ include file="footer.jsp" %>
