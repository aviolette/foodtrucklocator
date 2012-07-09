<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>
<script src="/script/lib/d3.min.js" type="text/javascript"></script>
<script src="/script/lib/d3.layout.min.js" type="text/javascript"></script>
<script src="/script/lib/rickshaw.min.js" type="text/javascript"></script>

<h2>Geolocation</h2>

<h3>Google Lookups</h3>

<div id="googlelookups"></div>

<h3>Yahoo Lookups</h3>

<div id="yahoolookups"></div>

<h2>Twitter Connector</h2>

<div id="twitterCache"></div>

<h2>Google Calendar Connector</h2>

<div id="calendarCache"></div>

<script type="text/javascript">

  function drawGraph(statName, containerId) {
    var start = new Date();
    start.setSeconds(0);
    start.setMinutes(0);
    start.setHours(0);
    var end = new Date();
    var url = "/services/stats/" + encodeURIComponent(statName) + "?start=" + start.getTime() +
        "&end=" + end.getTime();
    new Rickshaw.Graph.Ajax({
      element: document.getElementById(containerId),
      width: 235,
      height: 85,
      renderer: 'line',
      dataURL: url,
      onData: function(d) {
        return d
      },
      onComplete: function(transport) {
        var graph = transport.graph;
        var detail = new Rickshaw.Graph.HoverDetail({ graph: graph });
      },
      series: [
        {
          name: statName,
          color: '#c05020'
        }
      ]
    });
  }
  drawGraph("foodtruck.geolocation.GoogleGeolocator_locate_total", "googlelookups");
  drawGraph("foodtruck.geolocation.YahooGeolocator_locate_total", "yahoolookups");
  drawGraph("foodtruck.twitter.TwitterServiceImpl_updateTwitterCache_total", "twitterCache");
  drawGraph("foodtruck.schedule.GoogleCalendar_findForTime_total", "calendarCache");
</script>

<%@include file="dashboardFooter.jsp" %>
