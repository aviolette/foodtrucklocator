<%@ include file="header.jsp" %>
<style type="text/css">
  #map-canvas {
    width: 100%;
    margin: 0px;
    padding: 0px
  }
</style>

<%@include file="include/core_js.jsp" %>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization"></script>

<script type="text/javascript">

  var data = ${locations};
  var map, pointarray, heatmap;

  function initialize() {
    $("#map-canvas").height($(window).height() - $("#topBar").height()-60);
    var mapOptions = {
      zoom: 9,
      center: new google.maps.LatLng(${center.latitude}, ${center.longitude}),
      mapTypeId: google.maps.MapTypeId.SATELLITE
    };

    map = new google.maps.Map(document.getElementById('map-canvas'),
        mapOptions);

    var pointArray = new google.maps.MVCArray(data);

    heatmap = new google.maps.visualization.HeatmapLayer({
      data: pointArray,
      radius: 20
    });

    heatmap.setMap(map);
  }

  google.maps.event.addDomListener(window, 'load', initialize);

</script>

<p>This heatmap represents the active food truck stops in the city and in the suburbs over a 3 month period.</p>

<div id="map-canvas"></div>

<%@ include file="footer.jsp" %>
