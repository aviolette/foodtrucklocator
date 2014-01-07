<%@ include file="header.jsp" %>

<div class="row" >
  <div class="col-md-6">
    <h2>Trucks at ${location.name} on <joda:format value="${thedate}" pattern="MMM dd, YYYY"/></h2>

    <table>
      <tbody>
      <c:forEach items="${stops}" var="stop">
        <tr>
        <tr>
          <td style="width:48px; vertical-align:top; padding-right:5px"><img src="${stop.truck.iconUrl}" alt="${stop.truck.name} icon"/></td>
          <td><a class="truckLink" href="/trucks/${stop.truck.id}">${stop.truck.name}</a><br/>
            <joda:format value="${stop.startTime}" style="-S"/> - <joda:format value="${stop.endTime}" style="-S"/></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>

  </div>
  <div class="col-md-6">
    <div id="map_canvas" style="width:100%; height:300px; margin-top:10px; padding-bottom:20px;">
    </div>
  </div>
</div>

<%@ include file="include/core_js.jsp" %>
<script type="text/javascript"
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>

<script type="text/javascript">
  $(document).ready(function () {
    var lat = ${location.latitude}, lng = ${location.longitude};
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
