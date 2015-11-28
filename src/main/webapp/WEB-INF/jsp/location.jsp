<%@ include file="header.jsp" %>
<%@ include file="include/rickshaw_css.jsp"%>

<div class="row" >
  <div class="col-md-6">
    <h2>${location.name}<c:if test="${isAdmin}">
      <a class="btn btn-small btn-default" href="/admin/locations/${location.key}"> <span class="glyphicon glyphicon-pencil"></span> Edit</a>
    </c:if></h2>


    <c:if test="${!empty(location.description)}">
      <p class="lead">${location.description}</p>
    </c:if>
    <c:if test="${!empty(location.url)}">
      <div><a href='${location.url}'>${location.url}</a></div>
    </c:if>
    <c:if test="${!empty(location.twitterHandle)}">
      <div><a href="http://twitter.com/${location.twitterHandle}">@${location.twitterHandle}</a></div>
    </c:if>
      <c:forEach items="${stops}" var="stop" varStatus="status">
        <c:if test="${status.index == 0}">
          <h3><a href="/locations/${location.key}?date=<joda:format value="${thedate}" pattern="YYYYMMdd"/>">Schedule for <joda:format value="${thedate}" pattern="MMM dd, YYYY"/></a></h3>
        </c:if>
        <div class="media">
          <a class="pull-left" href="/trucks/${stop.truck.id}">
            <img class="media-object" src="${stop.truck.iconUrl}" alt="${stop.truck.name} icon"/>
          </a>
          <div class="media-body">
            <a href="/trucks/${stop.truck.id}" class="truckLink">
            <h4 class="media-heading">${stop.truck.name}</h4>
            <joda:format value="${stop.startTime}" style="-S"/> - <joda:format value="${stop.endTime}" style="-S"/>
            </a>
          </div>
        </div>
      </c:forEach>

  </div>
  <div class="col-md-6">
    <div id="map_canvas" style="width:100%; height:300px; margin-top:10px; padding-bottom:20px;">
    </div>
  </div>
</div>

<c:if test="${hasPopularityStats}">
<div class="row">
  <div class="col-md-12">
    <h2>Popularity of this spot (by week)</h2>
    <div id="chart"></div>
  </div>
</div>
</c:if>

<%@ include file="include/core_js.jsp" %>
<script type="text/javascript"
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<%@include file="include/graph_libraries.jsp" %>

<script type="text/javascript">
  (function() {
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
      <c:if test="${hasPopularityStats}">
      var loopId;
      function resize() {
        $("#chart").empty();
        drawGraphs(["count.location.${location.key}"], "chart");
      }
      $(window).resize(function () {
        clearTimeout(loopId);
        loopId = setTimeout(resize, 500);
      });
      resize();
      </c:if>
    });
  })();
</script>
<%@ include file="footer.jsp" %>
