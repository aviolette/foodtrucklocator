<%@ include file="header.jsp" %>
<link href="/css/rickshaw/rickshaw.min.css" rel="stylesheet">


<div id="content" >

  <c:if test="${isAdmin}">
    <a href="/admin/trucks/${truck.id}">Edit on Admin Dashboard</a>
  </c:if>
  <div class="row">
    <div class="col-md-6">
      <h1>${truck.name}</h1>

      <c:if test="${truck.popupVendor}"><p><span class="badge badge-info">Popup Vendor</span></p></c:if>

      <p>${truck.description}</p>

      <div>
        <c:if test="${!empty(truck.facebook)}"><a target="_blank" href="http://facebook.com${truck.facebook}"><img
            alt="Facebook" src="/img/facebook32x32.png"></a></c:if>
        <c:if test="${!empty(truck.twitterHandle)}"><a target="_blank"
                                                       href="http://twitter.com/${truck.twitterHandle}"><img
            alt="@${truck.twitterHandle} on twitter" src="/img/twitter32x32.png"></a></c:if>
        <c:if test="${!empty(truck.foursquareUrl)}"><a target="_blank"
                                                       href="http://foursquare.com/venue/${truck.foursquareUrl}"><img
            alt="Check in on foursquare" src="/img/foursquare32x32.png"></a></c:if>
        <c:if test="${!empty(truck.instagramId)}"><a target="_blank"
                                                       href="http://instagram.com/${truck.instagramId}"><img
            alt="View on instagram" src="/img/instagram32x32.png"></a></c:if>
        <c:if test="${!empty(truck.yelpSlug)}"><a target="_blank"
                                                  href="http://yelp.com/biz/${truck.yelpSlug}"><img alt="Yelp"
                                                                                                    src="/img/yelp32x32.png"></a></c:if>
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
   </div>

  <div class="row" style="padding-top: 20px">
    <div class="span9">
      <h2>This Week's Schedule</h2>
      <table class="table table-bordered">
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
                    <ftl:location location="${stop.location}"/>
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
    </div>
  </div>


  <div class="row">
    <h2>Days on the Road (by week)</h2>
    <div id="chart"></div>
  </div>

  <div class="row" style="padding-top: 20px">
    <div class="col-md-3">
    <div class="panel panel-primary" >
      <div class="panel-heading">
        <div class="panel-title">First Seen</div>
      </div>
      <div class="panel-body">
        <joda:format value="${truck.stats.firstSeen}" style="MS"/> <c:if test="${!empty(truck.stats.whereFirstSeen.name)}">@ <br/>
        <ftl:location location="${truck.stats.whereFirstSeen}"/></c:if>
      </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="panel panel-primary">
        <div class="panel-heading">
          <div class="panel-title">Last Seen</div>
        </div>
        <div class="panel-body">
          <joda:format value="${truck.stats.lastSeen}" style="MS"/> <c:if test="${!empty(truck.stats.whereLastSeen.name)}">@ <br/>
          <ftl:location location="${truck.stats.whereLastSeen}"/></c:if>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="panel panel-primary">
        <div class="panel-heading">
          <div class="panel-title">Stops This Year</div>
        </div>
        <div class="panel-body text-center" style="font-size:2em">
          <strong>${truck.stats.stopsThisYear}</strong>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="panel panel-primary">
        <div class="panel-heading">
          <div class="panel-title">Total Stops</div>
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
<script src="/script/lib/d3.min.js" type="text/javascript"></script>
<script src="/script/lib/d3.layout.min.js" type="text/javascript"></script>
<script src="/script/lib/rickshaw.min.js" type="text/javascript"></script>
<script src="/script/graph.js"></script>
<script>
  <c:if test="${enableGraphs}">
    drawGraphs(["count.${truck.id}"], "chart");
  </c:if>
</script>


<%@ include file="footer.jsp" %>
