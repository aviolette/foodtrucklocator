<%@ include file="header.jsp" %>
<%@ include file="include/rickshaw_css.jsp"%>

<div id="content" >
  <c:if test="${isAdmin}">
    <a href="/admin/trucks/${truck.id}">Edit on Admin Dashboard</a>
  </c:if>
  <div class="row">
    <div class="col-md-6">
      <h1>${truck.name}</h1>
      <c:if test="${truck.popupVendor}"><p><span class="badge badge-info">Popup Vendor</span></p></c:if>
      <p class="lead">${truck.description}</p>
      <div>
        <c:if test="${!empty(truck.facebook)}"><a target="_blank" href="http://facebook.com${truck.facebook}"><img
            alt="Facebook" src="http://storage.googleapis.com/ftf_static/img/facebook32x32.png"></a></c:if>
        <c:if test="${!empty(truck.twitterHandle)}"><a target="_blank"
                                                       href="http://twitter.com/${truck.twitterHandle}"><img
            alt="@${truck.twitterHandle} on twitter" src="http://storage.googleapis.com/ftf_static/img/twitter32x32.png"></a></c:if>
        <c:if test="${!empty(truck.foursquareUrl)}"><a target="_blank"
                                                       href="http://foursquare.com/venue/${truck.foursquareUrl}"><img
            alt="Check in on foursquare" src="http://storage.googleapis.com/ftf_static/img/foursquare32x32.png"></a></c:if>
        <c:if test="${!empty(truck.instagramId)}"><a target="_blank"
                                                       href="http://instagram.com/${truck.instagramId}"><img
            alt="View on instagram" src="http://storage.googleapis.com/ftf_static/img/instagram32x32.png"></a></c:if>
        <c:if test="${!empty(truck.yelpSlug)}"><a target="_blank"
                                                  href="http://yelp.com/biz/${truck.yelpSlug}"><img alt="Yelp"
                                                                                                    src="http://storage.googleapis.com/ftf_static/img/yelp32x32.png"></a></c:if>
      </div>
      <div style="padding-top:20px">
        <table class="table">
          <tr>
            <td>Website</td>
            <td><c:choose><c:when test="${empty(truck.url)}">none</c:when><c:otherwise><a target="_blank"
                                                                                          href="${truck.url}">${truck.url}</a></c:otherwise></c:choose>
            </td>
          </tr>
          <tr>
            <td>Email</td>
            <td><c:choose><c:when test="${empty(truck.publicEmail)}">none</c:when><c:otherwise><a target="_blank"
                                                                                            href="mailto:${truck.publicEmail}">${truck.publicEmail}</a></c:otherwise></c:choose>
            </td>
          </tr>
          <tr>
            <td>Phone</td>
            <td><c:choose><c:when
                test="${empty(truck.phone)}">none</c:when><c:otherwise>${truck.phone}</c:otherwise></c:choose>
            </td>
          </tr>
        </table>
      </div>
    </div>
    <div class="col-md-6">
      <div id="truck-image-container">
        <c:if test="${!empty(truck.fullsizeImage)}">
          <img src="${truck.fullsizeImage}" class="img-rounded img-responsive"/>
        </c:if>
      </div>
    </div>
   </div>

  <div class="row" style="padding-top: 20px">
    <div class="col-md-12">
      <h2>This Week's Schedule</h2>
      <table class="table table-bordered" id="scheduleTableView">
        <thead>
        <tr>
          <c:forEach items="${daysOfWeek}" var="day">
            <th style="width:14%">${day}</th>
          </c:forEach>
        </tr>
        </thead>
        <tbody id="scheduleTable">
        <tr>
          <c:forEach items="${stops}" var="schedule" varStatus="scheduleStatus">
            <c:if test="${scheduleStatus.index < 7}">
          <td style="min-height:200px">
            <c:choose>
              <c:when test="${schedule.hasStops}">
                <ul class="list-unstyled">
                  <c:forEach items="${schedule.stops}" var="stop">
                    <li style="padding-bottom:10px">
                    <joda:format value="${stop.startTime}" style="-S"/>
                    <ftl:location at="${stop.startTime}" location="${stop.location}"/>
                    </li>
                  </c:forEach>

                </ul>
              </c:when>
              <c:otherwise>
                <c:choose>
                  <c:when test="${schedule.afterToday}">
                    <em>No Scheduled Stops</em>
                  </c:when>
                  <c:otherwise>
                    <em>No Stops</em>
                  </c:otherwise>
                </c:choose>
              </c:otherwise>
            </c:choose>
          </td>
            </c:if>
          </c:forEach>
        </tr>
        </tbody>
      </table>

    <dl id="scheduleList">
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
