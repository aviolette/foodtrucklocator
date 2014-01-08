<%@ include file="header.jsp" %>

<h2>${event.name}</h2>

<div class="row">
  <div class="span6">
    <%@ include file="include/eventView.jsp" %>
  </div>
  <div class="span6">
    <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;">
    </div>
  </div>
</div>

<%@ include file="include/core_js.jsp" %>
<script type="text/javascript"
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>

<script type="text/javascript">
  $(document).ready(function () {
    var lat = ${event.location.latitude}, lng = ${event.location.longitude};
    if (!(typeof google == "undefined")) {
      var markerLat = new google.maps.LatLng(lat, lng);
      var myOptions = {
        center: markerLat,
        zoom: 14,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      };
      var map = new google.maps.Map(document.getElementById("map_canvas"),
          myOptions);

      var marker = new google.maps.Marker({
        draggable: false,
        position: markerLat,
        map: map
      });
    }
  });
</script>
<%@ include file="footer.jsp" %>
