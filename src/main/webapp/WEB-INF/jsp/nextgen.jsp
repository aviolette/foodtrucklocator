<%@ include file="nextgenheader.jsp" %>

<div class="row-fluid" id="sidebar">
  <div class="span4" id="listContainer">
    <div style="padding-top:10px;" class="alert alert-block" id="filteredWarning" style="display:none">
      The result list is currently being filtered based on the zoom-level of the map.  To see all trucks, zoom out.
    </div>
    <h3>Trucks Open Now</h3>
    <dl id="nowTrucks">
    </dl>
    <h3>Trucks Open Later</h3>
    <dl id="laterTrucks">
    </dl>
  </div>
  <div id="content" class="span8">
    <div id="map_wrapper">
      <div class="section" id="map_canvas"></div>
    </div>
   </div>
</div>
<%@include file="include/mobile_dialog.jsp" %>
<%@include file="include/core_js.jsp" %>
<script type="text/javascript"
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<script type="text/javascript" src="script/chiftf-view.js?ver=1"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run(${mobile}, new google.maps.LatLng(${center.latitude}, ${center.longitude}),
        new Date(${requestTimeInMillis}), ${payload}, "${appKey}");
  });
</script>
<%@ include file="footer.jsp" %>
