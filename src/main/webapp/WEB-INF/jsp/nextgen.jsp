<%@ include file="nextgenheader.jsp" %>

<div class="row-fluid" id="sidebar">
  <div class="span4" id="listContainer">
    <div class="alert alert-block" id="filteredWarning" style="display:none">
      <h4>Warning!</h4>
      You are looking at a filtered view.<br/>
      <button id="resetButton" class="btn btn-warning">Reset</button>
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
<script type="text/javascript" src="script/chiftf-view.js"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run(${mobile}, new google.maps.LatLng(${center.latitude}, ${center.longitude}),
        new Date(${requestTimeInMillis}), ${payload});
  });
</script>
<%@ include file="footer.jsp" %>
