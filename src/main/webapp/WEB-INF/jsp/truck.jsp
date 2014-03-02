<%@ include file="header.jsp" %>
<link href="/css/rickshaw/rickshaw.min.css" rel="stylesheet">


<div id="content" >

  <div class="row">
    <div class="span4">
      <h2>${truck.name}</h2>

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




    <div class="span5">
      <h2>Statistics</h2>
      <div id="chart"></div>
      <table class="table">
        <tr>
          <td>Last seen</td>
          <td><joda:format value="${truck.stats.lastSeen}" style="MS"/> @ <br/>
            ${truck.stats.whereLastSeen.name}</td>
        </tr>
        <tr>
          <td>First seen</td>
          <td><joda:format value="${truck.stats.firstSeen}" style="MS"/> @ <br/>
            ${truck.stats.whereFirstSeen.name}</td>
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
      <h2>This Week's Schedule</h2>
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

</div>

</div>
<%@include file="include/core_js.jsp" %>
<script src="/script/lib/d3.min.js" type="text/javascript"></script>
<script src="/script/lib/d3.layout.min.js" type="text/javascript"></script>
<script src="/script/lib/rickshaw.min.js" type="text/javascript"></script>

<script>

  function drawGraphs(statNames, containerId) {
    if (typeof(statNames) == "string") {
      drawGraph([statNames], containerId);
    } else {
      drawGraph(statNames, containerId);
    }
  }

  function drawGraph(statNames, containerId) {
    var series = [];
    var colors = ["steelblue", "red", "green"];
    $.each(statNames, function(i, statName) {
      series.push({name : statName, color : colors[i]});
    });
    var DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    var end = new Date();
    var start = new Date(end.getTime() - (1095 * DAY_IN_MILLIS));
    var url = "/services/stats/counts/" + encodeURIComponent(statNames.join(",")) + "?start=" +
        start.getTime() +
        "&interval=604800000" +
        "&end=" + end.getTime();
    new Rickshaw.Graph.Ajax({
      element: document.getElementById(containerId),
      width: 1140,
      height: 200,
      renderer: 'area',
      stroke: true,
      dataURL: url,
      onData: function(d) {
        return d
      },
      onComplete: function(transport) {
        var graph = transport.graph;
        graph.renderer.unstack = true;
        var xAxis = new Rickshaw.Graph.Axis.Time({ graph: graph });
        xAxis.render();
        var yAxis = new Rickshaw.Graph.Axis.Y({ graph: graph });
        yAxis.render();
      },
      series: series
    });
  }
//  drawGraphs(["count.${truck.id}"], "chart");
</script>


<%@ include file="footer.jsp" %>
