<%@ include file="header.jsp" %>

<div class="row" id="sidebar">
  <div class="col-md-4" id="listContainer">
    <div id="appbadges" class="hidden">
      <a id="androidBadge" class="hidden" href="https://play.google.com/store/apps/details?id=net.andrewviolette.truckz"><img src="/img/en_generic_rgb_wo_45.png" title="Google Play Button"/></a>
      <a id="iphoneBadge" class="hidden" href="https://itunes.apple.com/us/app/chicago-food-truck-finder/id1002801516"><img height="45px" src="/img/Download_on_the_App_Store_Badge_US-UK_135x40.svg"/></a>
    </div>
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
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<script type="text/javascript" src="script/foodtruckfinder-1.1.js"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run("${mode}", new google.maps.LatLng(${center.latitude}, ${center.longitude}),
        new Date(${requestTimeInMillis}), ${payload}, "${appKey}", ${designatedStops}, ${removeDesignatedStops}, "${defaultCity}");
  });
</script>
<%@ include file="footer.jsp" %>
