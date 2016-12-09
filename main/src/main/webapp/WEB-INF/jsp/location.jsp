<%@ include file="header.jsp" %>
<%@ include file="include/rickshaw_css.jsp"%>
<div id="map_canvas" style="width:100%; height:300px;"></div>

<div class="row top-row">
  <div class="col-md-12">
    <img <c:if test="${empty(location.imageUrl.protocolRelative)}">style="display:none"</c:if> class="previewIcon" src="${location.imageUrl.protocolRelative}" width="150" height="150"/>
  </div>
</div>

<div class="row second-top-row" style="padding-top: 0;">
  <div class="col-md-6">
    <h2>${location.shortenedName}<c:if test="${isAdmin}">
      <a class="btn btn-small btn-default" href="/admin/locations/${location.key}"> <span class="glyphicon glyphicon-pencil"></span> Edit</a>
    </c:if></h2>


    <c:if test="${!empty(location.description)}">
      <p class="lead">${location.description}</p>
    </c:if>
    <c:if test="${!empty(location.url)}">
      <div class="lead"><a href='${location.url}'>${location.url}</a></div>
    </c:if>

    <div>
      <c:if test="${!empty(location.facebookUri)}"><a target="_blank" href="http://facebook.com${location.facebookUri}"><img
          alt="Facebook" src="//storage.googleapis.com/ftf_static/img/facebook32x32.png"></a></c:if>
      <c:if test="${!empty(location.twitterHandle)}"><a target="_blank"
                                                     href="http://twitter.com/${location.twitterHandle}"><img
          alt="@${location.twitterHandle} on twitter" src="//storage.googleapis.com/ftf_static/img/twitter32x32.png"></a></c:if>
    </div>
  </div>
  <div class="col-md-6">
    <c:choose>
      <c:when test="${!empty(weeklyStops)}">
        <c:forEach items="${weeklyStops}" var="day" varStatus="dayStatus">
          <h3><joda:format value="${day.day}" pattern="EEEE"/></h3>
          <c:forEach items="${day.stops}" var="stop">
            <a href="/trucks/${stop.truck.id}"><img src="${stop.truck.iconUrlObj.protocolRelative}" alt="" class="img-rounded"
                                                    title="<joda:format pattern="hh:mm a" value="${stop.startTime}"/> - <joda:format pattern="hh:mm a" value="${stop.endTime}"/> ${stop.truck.name}"/></a>
          </c:forEach>
        </c:forEach>
      </c:when>
      <c:otherwise>
        <c:forEach items="${stops}" var="stop" varStatus="status">
          <c:if test="${status.index == 0}">
            <h3>Schedule for <joda:format value="${thedate}" pattern="MMM dd, YYYY"/></h3>
            <c:if test="${requestedTime}"><div><small><a href="/locations/${location.key}">(click here for this week's schedule)</a></small></div></c:if>
          </c:if>
          <div class="media">
            <a class="pull-left" href="/trucks/${stop.truck.id}">
              <img class="media-object img-rounded" src="${stop.truck.iconUrlObj.protocolRelative}" alt="${stop.truck.name} icon"/>
            </a>
            <div class="media-body">
              <a href="/trucks/${stop.truck.id}" class="truckLink">
                <h4 class="media-heading">${stop.truck.name}</h4>
                <joda:format value="${stop.startTime}" style="-S"/> - <joda:format value="${stop.endTime}" style="-S"/>
              </a>
            </div>
          </div>
        </c:forEach>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<%@ include file="include/core_js.jsp" %>
<script type="text/javascript"
        src="//maps.google.com/maps/api/js?key=${googleApiKey}&libraries=geometry"></script>
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
          scrollwheel: false,
          mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        var map = new google.maps.Map(document.getElementById("map_canvas"),
            myOptions);
        new google.maps.Marker({
          draggable: false,
          position: markerLat,
          map: map
        });
      }
    });
  })();
</script>
<%@ include file="footer.jsp" %>
