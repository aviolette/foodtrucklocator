<%@ include file="header.jsp" %>

<div class="row" >
  <div class="col-md-6">
    <h2>${location.name}</h2>

    <c:if test="${isAdmin}">
      <div>
      <a class="btn btn-primary" href="/admin/locations/${location.key}">Edit</a>
      </div>
    </c:if>

    <c:if test="${!empty(location.description)}">
      <div>${location.description}</div>
    </c:if>
    <c:if test="${!empty(location.url)}">
      <div><a href='${location.url}'>${location.url}</a></div>
    </c:if>
    <c:if test="${!empty(location.twitterHandle)}">
      <div><a href="http://twitter.com/${location.twitterHandle}">@${location.twitterHandle}</a></div>
    </c:if>
      <c:forEach items="${stops}" var="stop" varStatus="status">
        <c:if test="${status.index == 0}">
          <h3>Schedule for <joda:format value="${thedate}" pattern="MMM dd, YYYY"/></h3>
        </c:if>
        <div class="media">
          <a class="pull-left" href="/trucks/${stop.truck.id}">
            <img class="media-object" src="${stop.truck.iconUrl}" alt="${stop.truck.name} icon"/>
          </a>
          <div class="media-body">
            <h4 class="media-heading">${stop.truck.name}</h4>
            <joda:format value="${stop.startTime}" style="-S"/> - <joda:format value="${stop.endTime}" style="-S"/>
          </div>
        </div>
      </c:forEach>

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
