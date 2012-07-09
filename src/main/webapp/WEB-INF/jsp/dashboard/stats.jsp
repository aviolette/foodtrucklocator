<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>
<link href="/css/rickshaw/graph.css" rel="stylesheet">
<style type="text/css">
  .rickshaw_graph {
    margin: 10px 0;
  }
</style>

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
    var DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    var end = new Date();
    var start = new Date(end.getTime() - DAY_IN_MILLIS);
    var url = "/services/stats/" + encodeURIComponent(statName) + "?start=" + start.getTime() +
        "&end=" + end.getTime();
    new Rickshaw.Graph.Ajax({
      element: document.getElementById(containerId),
      width: 500,
      height: 100,
      renderer: 'line',
      dataURL: url,
      onData: function(d) {
        return d
      },
      onComplete: function(transport) {
        var graph = transport.graph;
        var detail = new Rickshaw.Graph.HoverDetail({ graph: graph });
        var xAxis = new Rickshaw.Graph.Axis.Time({ graph: graph });
        xAxis.render();
      },
      series: [
        {
          name: statName,
          color: 'steelblue'
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
