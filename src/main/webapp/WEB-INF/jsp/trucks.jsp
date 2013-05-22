<%@ include file="nextgenheader.jsp" %>
<div class="row" style="padding-top: 20px">
  <div class="span3">
    <ul class="nav nav-list">
      <c:forEach var="tr" items="${trucks}">
        <li><a class="<c:if test="${tr.id == truck.id}">active</c:if>" href="/trucks/${tr.id}">${tr.name}</a></li>
      </c:forEach>
    </ul>
  </div>
  <div class="span9">
    <c:choose>
      <c:when test="${!empty(truck)}">
        <div class="row">
          <div class="span4">
            <h2>${truck.name}</h2>

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
                  <td><c:choose><c:when test="${empty(truck.email)}">none</c:when><c:otherwise><a target="_blank"
                                                                                                  href="mailto:${truck.email}">${truck.email}</a></c:otherwise></c:choose>
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
          <div class="span5">
            <h2>Statistics</h2>

            <c:choose>
              <c:when test="${truck.inactive}">
                <div class="badge badge-important" style="margin-bottom: 20px">Inactive</div>
              </c:when>
              <c:otherwise>
                <div class="badge badge-success" style="margin-bottom: 20px">Active</div>
              </c:otherwise>
            </c:choose>

            <table class="table">
              <tr>
                <td>Last active</td>
                <td><joda:format value="${truck.stats.lastSeen}" style="MS"/> @ <br/>
                    ${truck.stats.whereLastSeen.name}</td>
              </tr>
              <tr>
                <td>Stops this year</td>
                <td>${truck.stats.stopsThisYear}</td>
              </tr>
              <tr>
                <td>Total stops</td>
                <td>${truck.stats.totalStops}</td>
              </tr>
            </table>
          </div>
        </div>
        <div class="row" style="padding-top: 20px">
          <div class="span9">
            <h2>Schedule</h2>
            <table class="table table-striped">
              <thead>
              <tr>
                <th>Day</th>
                <th>Start Time</th>
                <th>End Time</th>
                <th>Location</th>
              </tr>
              </thead>
              <tbody id="scheduleTable">

              <c:forEach items="${stops}" var="schedule">
                <c:forEach items="${schedule.stops}" var="stop">
                  <tr>
                    <td><joda:format value="${schedule.day}" pattern="EEE yyyy-MM-dd"/></td>
                    <td><joda:format value="${stop.startTime}" style="-S"/></td>
                    <td><joda:format value="${stop.endTime}" style="-S"/></td>
                    <td>${stop.location.name}</td>
                  </tr>
                </c:forEach>
              </c:forEach>

              </tbody>
            </table>
          </div>
        </div>

      </c:when>
    </c:choose>
  </div>
</div>
<%@ include file="footer.jsp" %>
