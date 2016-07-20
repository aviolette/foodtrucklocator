<%@ include file="header.jsp" %>
<%@ include file="include/rickshaw_css.jsp"%>
<style>
  dl.schedule {
    font-size: 125%;
  }

  dl.schedule dd {
    padding-bottom: 10px;
  }
</style>

<div id="content" >
  <ol class="breadcrumb">
    <li><a href="/trucks">Trucks</a></li>
    <li class="active">${truck.name}</li>
  </ol>

  <img src="${truck.biggestBackgroundImageUrl.protocolRelative}" width="100%" height="300"/>

  <div class="row top-row" style="padding-bottom: 0;">
    <div class="col-md-6">
      <img class="previewIcon" src="${truck.previewIconUrl.protocolRelative}" width="150" height="150"/>
    </div>
  </div>
  <div class="row second-top-row">
    <div class="col-md-6">
      <h1>${truck.name}<c:if test="${isAdmin}">
        <a class="btn btn-default" href="/admin/trucks/${truck.id}">Edit</a>
      </c:if></h1>
      <c:if test="${truck.popupVendor}"><p><span class="badge badge-info">Popup Vendor</span></p></c:if>
      <p class="lead">${truck.description}</p>
      <div>
        <c:if test="${!empty(truck.facebook)}"><a target="_blank" href="http://facebook.com${truck.facebook}"><img
            alt="Facebook" src="//storage.googleapis.com/ftf_static/img/facebook32x32.png"></a></c:if>
        <c:if test="${!empty(truck.twitterHandle)}"><a target="_blank"
                                                       href="http://twitter.com/${truck.twitterHandle}"><img
            alt="@${truck.twitterHandle} on twitter" src="//storage.googleapis.com/ftf_static/img/twitter32x32.png"></a></c:if>
        <c:if test="${!empty(truck.foursquareUrl)}"><a target="_blank"
                                                       href="http://foursquare.com/venue/${truck.foursquareUrl}"><img
            alt="Check in on foursquare" src="//storage.googleapis.com/ftf_static/img/foursquare32x32.png"></a></c:if>
        <c:if test="${!empty(truck.instagramId)}"><a target="_blank"
                                                       href="http://instagram.com/${truck.instagramId}"><img
            alt="View on instagram" src="//storage.googleapis.com/ftf_static/img/instagram32x32.png"></a></c:if>
      </div>
      <div style="padding-top:20px">
        <ul class="list-unstyled">
          <c:if test="${!empty(truck.url)}">
            <li class="lead"><a target="_blank" href="${truck.url}">${truck.url}</a></li>
          </c:if>
          <c:if test="${!empty(truck.publicEmail)}">
            <li><span class="glyphicon glyphicon-envelope"></span>&nbsp;
              <a target="_blank"
                   href="mailto:${truck.publicEmail}">${truck.publicEmail}</a></li>
          </c:if>
          <c:if test="${!empty(truck.phone)}">
            <li><span class="glyphicon glyphicon-earphone"></span>&nbsp;
              ${truck.phone}</li>
          </c:if>
        </ul>
      </div>
      <c:if test="${!empty(truck.menuUrl)}">
        <h2>Menu</h2>
        <div>
          <a target="_blank" href="${truck.menuUrl}">Click here to see this truck's current menu!</a>
        </div>
        <small><em>*disclaimer - this data may or may not be accurate or up-to-date.</em></small>
      </c:if>
      <c:if test="${!empty(dailyData)}">
        <h2>Today's Specials</h2>
        <ul class="list-unstyled">
        <c:forEach items="${dailyData.specials}" var="special">
          <li class="lead">${special.special} <c:if test="${special.soldOut}"><span class="label label-danger">sold out!</span></c:if></li>
        </c:forEach>
        </ul>
      </c:if>
    </div>
    <div class="col-md-6">
      <h2>This Week's Schedule</h2>
      <dl class="schedule">
        <c:forEach items="${stops}" var="schedule" varStatus="scheduleStatus">

          <c:forEach items="${schedule.stops}" var="stop" varStatus="stopStatus">
            <c:if test="${stopStatus.first}">
              <dt><joda:format value="${stop.startTime}" pattern="EEEE MM/dd"/>
              </dt>
            </c:if>
            <dd>                    <joda:format value="${stop.startTime}" style="-S"/> -
              <ftl:location at="${stop.startTime}" location="${stop.location}"/>
            </dd>
          </c:forEach>

        </c:forEach>
      </dl>
    </div>
   </div>
</div>


  <div class="row">
    <div class="col-md-12">
      <h2>Days on the Road (by week)</h2>
      <div id="chart"></div>
    </div>
  </div>

  <div class="row" style="padding-top: 20px">
    <div class="col-md-3">
    <div class="panel panel-primary" >
      <div class="panel-heading">
        <div class="panel-title text-center">First Seen</div>
      </div>
      <div class="panel-body">
        <joda:format value="${truck.stats.firstSeen}" style="MS"/> <c:if test="${!empty(truck.stats.whereFirstSeen.name)}">@ <br/>
        <ftl:location at="${truck.stats.firstSeen}" location="${truck.stats.whereFirstSeen}"/></c:if>
      </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="panel panel-primary">
        <div class="panel-heading">
          <div class="panel-title text-center">Last Seen</div>
        </div>
        <div class="panel-body">
          <joda:format value="${truck.stats.lastSeen}" style="MS"/> <c:if test="${!empty(truck.stats.whereLastSeen.name)}">@ <br/>
          <ftl:location at="${truck.stats.lastSeen}" location="${truck.stats.whereLastSeen}"/></c:if>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="panel panel-primary">
        <div class="panel-heading">
          <div class="panel-title text-center">Stops This Year</div>
        </div>
        <div class="panel-body text-center" style="font-size:2em">
          <strong>${truck.stats.stopsThisYear}</strong>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="panel panel-primary">
        <div class="panel-heading">
          <div class="panel-title text-center">Total Stops</div>
        </div>
        <div class="panel-body text-center" style="font-size:2em">
          <strong>${truck.stats.totalStops}</strong>
        </div>
      </div>
    </div>
  </div>
</div>

</div>
<%@include file="include/core_js.jsp" %>
<%@include file="include/graph_libraries.jsp" %>
<script>
  (function() {
    <c:if test="${enableGraphs}">
    var loopId;
    function resize() {
      $("#chart").empty();
      drawGraphs(["count.${truck.id}"], "chart");
    }
    $(window).resize(function () {
      clearTimeout(loopId);
      loopId = setTimeout(resize, 500);
    });
    resize();
    </c:if>
  })();
</script>


<%@ include file="footer.jsp" %>
