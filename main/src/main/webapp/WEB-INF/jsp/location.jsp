<%@ include file="header.jsp" %>
<%@ include file="include/rickshaw_css.jsp" %>
<div id="map_canvas" style="width:100%; height:300px;margin-top:-20px;margin-bottom:20px;"></div>



<div class="row top-row">
  <div class="col-md-5">


    <h1>${location.shortenedName}<c:if test="${isAdmin}">
      <a class="btn btn-small btn-default" href="/admin/locations/${location.key}"> <span
          class="glyphicons glyphicons-pencil"></span> Edit</a>
      </c:if></h1>

    <img class="rounded previewIcon <c:if test="${empty(location.imageUrl.protocolRelative)}">hidden</c:if>"
         src="${location.imageUrl.protocolRelative}" width="100%" />


    <c:if test="${!empty(location.description)}">
      <p class="lead">${location.description}</p>
    </c:if>

    <div class="social-icons m-t">
      <c:if test="${!empty(location.facebookUri)}"><a target="_blank" href="http://facebook.com${location.facebookUri}"><span class="social social-facebook"></span></a>&nbsp;</c:if>
      <c:if test="${!empty(location.twitterHandle)}"><a target="_blank"
                                                        href="http://twitter.com/${location.twitterHandle}"><span class="social social-twitter"></span></a>&nbsp;</c:if>
    </div>

    <div class="m-t contact-section">
      <ul class="list-unstyled">
        <c:if test="${!empty(location.url)}">
          <li class="mb-2"><a href='${location.url}'>${location.urlObj.forDisplay}</a></li>
        </c:if>
        <c:if test="${!empty(location.email)}">
          <li class="mb-2"><span class="glyphicons glyphicons-envelope"></span>&nbsp;
            <a target="_blank"
               href="mailto:${location.email}">${location.email}</a></li>
        </c:if>
        <c:if test="${!empty(location.phoneNumber)}">
          <li class="mb-2"><span class="glyphicons glyphicons-phone-alt"></span>&nbsp;
              ${location.phoneNumber}</li>
        </c:if>

      </ul>
    </div>


  </div>
  <div class="col-md-7" style="padding-left:30px">
    <c:choose>
      <c:when test="${!empty(weeklyStops)}">
        <c:forEach items="${weeklyStops}" var="day" varStatus="dayStatus">
          <h2 class="date-header"><joda:format value="${day.day}" pattern="EEEE MMM dd"/></h2>
          <div class="row">
          <c:forEach items="${day.stops}" var="stop" varStatus="stopStatus">
            <div class="col-xs-6 col-sm-6 col-md-6 col-lg-4 col-xl-3">
              <a href="/trucks/${stop.truck.id}">
                <div class="thumbnail"><img width="180" height="180" src="${stop.truck.previewIconUrl.protocolRelative}"
                                            alt="" class="img-rounded"
                                            title="<joda:format pattern="hh:mm a" value="${stop.startTime}"/> - <joda:format pattern="hh:mm a" value="${stop.endTime}"/> ${stop.truck.name}"/>
                  <p><strong>${stop.truck.name}<br/><joda:format pattern="hh:mm a"
                                                                                     value="${stop.startTime}"/> -
                    <joda:format pattern="hh:mm a" value="${stop.endTime}"/></strong></p></div>
              </a>
            </div>
          </c:forEach>
          </div>
        </c:forEach>
      </c:when>
      <c:otherwise>
        <h2 class="date-header">Schedule for <joda:format value="${thedate}" pattern="MMM dd, YYYY"/></h2>
        <c:if test="${requestedTime}">
          <div>
            <small><a href="/locations/${location.key}">(click here for this week's schedule)</a></small>
          </div>
        </c:if>
        <div class="row">
        <c:forEach items="${stops}" var="stop" varStatus="stopStatus">
          <div class="col-xs-6 col-sm-6 col-md-6 col-lg-4 col-xl-3">
            <a href="/trucks/${stop.truck.id}">
              <div class="thumbnail"><img width="180" height="180" src="${stop.truck.previewIconUrl.protocolRelative}"
                                          alt="" class="img-rounded"
                                          title="<joda:format pattern="hh:mm a" value="${stop.startTime}"/> - <joda:format pattern="hh:mm a" value="${stop.endTime}"/> ${stop.truck.name}"/>
                <p class="text-center"><strong>${stop.truck.name}<br/><joda:format pattern="hh:mm a"
                                                                                   value="${stop.startTime}"/> -
                  <joda:format pattern="hh:mm a" value="${stop.endTime}"/></strong></p></div>
            </a>
          </div>

        </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<%@ include file="include/core_js.jsp" %>
<script type="text/javascript"
        src="//maps.google.com/maps/api/js?key=${googleApiKey}&libraries=geometry"></script>
<script type="text/javascript">
  (function () {
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
