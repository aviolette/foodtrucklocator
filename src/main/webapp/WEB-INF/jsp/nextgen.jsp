<%@ include file="nextgenheader.jsp" %>

<div class="row-fluid" id="sidebar">
  <div class="span4" id="listContainer">
    <div style="margin-top:10px;" class="alert alert-block" id="flashMsg" style="display:none">
    </div>
    <h3 class="trucksListHeader">Trucks Open Now</h3>
    <dl id="nowTrucks" class="truckDL">
    </dl>
    <h3 class="trucksListHeader">Trucks Open Later</h3>
    <dl id="laterTrucks" class="truckDL">
    </dl>
  </div>
  <div id="content" class="span8">
    <div id="map_wrapper">
      <div class="section" id="map_canvas"></div>
    </div>
   </div>
</div>
<c:if test="${not mobile}">
  <%@include file="include/mobile_dialog.jsp" %>
  <%@include file="include/truck_dialog.jsp" %>
</c:if>
<%@include file="include/core_js.jsp" %>
<script type="text/javascript"
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<script type="text/javascript" src="script/chiftf-view.js?ver=5"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run(${mobile}, new google.maps.LatLng(${center.latitude}, ${center.longitude}),
        new Date(${requestTimeInMillis}), ${payload}, "${appKey}");
  });
</script>
<%@ include file="footer.jsp" %>
