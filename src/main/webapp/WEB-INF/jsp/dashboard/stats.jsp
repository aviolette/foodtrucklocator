<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>
<link href="/css/rickshaw/rickshaw.min.css" rel="stylesheet">
<style type="text/css">
  .rickshaw_graph {
    margin: 20px 0;
  }
</style>

<script src="/script/lib/d3.min.js" type="text/javascript"></script>
<script src="/script/lib/d3.layout.min.js" type="text/javascript"></script>
<script src="/script/lib/rickshaw.min.js" type="text/javascript"></script>

<div class="row">
  <div class="span6">
    <h2>Trucks</h2>
    <div id="trucksOnRoad"></div>
  </div>
  <div class="span6">
    <h2>Database Cache Lookups</h2>
    <div id="databaseCache"></div>
  </div>
</div>
<div class="row">
  <div class="span6">
    <h2>Google Geolocation Lookups</h2>
    <div id="googlelookups"></div>
  </div>
  <div class="span6">
    <h2>Yahoo Geolocation Lookups</h2>
    <div id="yahoolookups"></div>
  </div>
</div>
<div class="row">
  <div class="span6">
    <h2>Twitter Connector</h2>
    <div id="twitterCache"></div>
  </div>
  <div class="span6">
    <h2>Google Calendar Connector</h2>
    <div id="calendarCache"></div>
  </div>
</div>





<h2>End Point Requests</h2>
<div class="row">
  <div class="span6">
    <h3>Schedule Service - /services/schedule/<code>truckId</code></h3>
    <div id="scheduleService"></div>
  </div>
  <div class="span6">
    <h3>Schedule Service - /services/daily_schedule</h3>
    <div id="dailySchedule"></div>
  </div>
</div>


<script type="text/javascript">
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
    var start = new Date(end.getTime() - DAY_IN_MILLIS);
    var url = "/services/stats/counts/" + encodeURIComponent(statNames.join(",")) + "?start=" +
        start.getTime() +
        "&end=" + end.getTime();
    new Rickshaw.Graph.Ajax({
      element: document.getElementById(containerId),
      width: 500,
      height: 100,
      renderer: 'area',
      stroke: true,
      dataURL: url,
      onData: function(d) {
        return d
      },
      onComplete: function(transport) {
        var graph = transport.graph;
        graph.renderer.unstack = true;
        var detail = new Rickshaw.Graph.HoverDetail({ graph: graph });
        var xAxis = new Rickshaw.Graph.Axis.Time({ graph: graph });
        xAxis.render();
        var yAxis = new Rickshaw.Graph.Axis.Y({ graph: graph });
        yAxis.render();
      },
      series: series
    });
  }
  $(document).ready(function() {
    drawGraphs(["foodtruck.geolocation.GoogleGeolocator_locate_total",
      "foodtruck.geolocation.GoogleGeolocator_locate_failed"], "googlelookups");
    drawGraphs(["foodtruck.geolocation.YahooGeolocator_locate_total",
      "foodtruck.geolocation.YahooGeolocator_locate_failed"], "yahoolookups");
    drawGraphs(["foodtruck.twitter.TwitterServiceImpl_updateTwitterCache_total",
      "foodtruck.twitter.TwitterServiceImpl_updateTwitterCache_failed"], "twitterCache");
    drawGraphs("foodtruck.schedule.GoogleCalendar_findForTime_total", "calendarCache");
    drawGraphs(["cacheLookup_total","cacheLookup_failed"], "databaseCache");
    drawGraphs(["foodtruck.server.api.FoodTruckScheduleServlet_doGet_total"], "scheduleService");
    drawGraphs(["foodtruck.server.resources.DailyScheduleResource_findForDay_total",
      "foodtruck.server.resources.DailyScheduleResource_findForDay_failed"], "dailySchedule");
    drawGraphs("trucksOnRoad", "trucksOnRoad");
  });
</script>

<%@include file="dashboardFooter.jsp" %>
