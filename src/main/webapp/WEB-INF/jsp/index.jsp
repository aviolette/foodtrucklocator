<%@ include file="header.jsp" %>

<div class="row" id="sidebar">
  <div class="col-md-4" id="listContainer">
    <div class="alert alert-info" id="flashMsg" style="display:none">
    </div>
    <h3 class="trucksListHeader">Trucks Open Now</h3>
    <div id="nowTrucks" class="truckDL">
    </div>
    <h3 class="trucksListHeader">Trucks Open Later</h3>
    <div id="laterTrucks" class="truckDL">
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
<script type="text/javascript" src="script/chiftf-view.js?ver=8"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run(${mobile}, new google.maps.LatLng(${center.latitude}, ${center.longitude}),
        new Date(${requestTimeInMillis}), ${payload}, "${appKey}");
  });
</script>
<%@ include file="footer.jsp" %>
