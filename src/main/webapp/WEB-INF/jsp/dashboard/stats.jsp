<%@ include file="../common.jsp" %>
<%@ include file="dashboardHeaderBS3.jsp" %>
<%@ include file="../include/rickshaw_css.jsp" %>
<%@ include file="../include/graph_libraries.jsp"%>

<div class="row">
  <div class="col-md-12">
    <div class="form-group">
      <label for="statList">Stats</label>
      <select id="statList" class="form-control">
        <option>System Errors</option>
        <option>Google Lookups</option>
        <option>Yahoo Lookups</option>
        <option>Database Geolocation Hits</option>
        <option>Trucks on the Road</option>
        <option>Alexa Requests</option>
      </select>
    </div>
  </div>
</div>
<div class="row">
  <div class="col-md-12 form-inline">
    <div class="form-group">
      <label for="startDate">Start</label>
      <input type="datetime-local" class="form-control" id="startDate"/>
    </div>
    <div class="form-group">
      <label for="endDate">End</label>
      <input type="datetime-local" class="form-control" id="endDate"/>
    </div>
    <button style="margin-top:25px" class="btn btn-primary" id="applyButton">Apply</button>
  </div>
</div>
<div class="row" style="margin-top:30px">
  <div class="col-md-12">
    <div id="statGraph"></div>
  </div>
</div>

<script type="text/javascript">
  var colors = ["steelblue", "red", "green", "yellow", "orange", "cyan", "darkgray", "lawngreen", "midnightblue", "cadetblue"];
  var DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

  var endDate = new Date(), startDate = new Date(endDate.getTime() - DAY_IN_MILLIS);
  $("#startDate").val(startDate.toISOString().substring(0, 16));
  $("#endDate").val(endDate.toISOString().substring(0, 16));

  function drawGraphs(statNames) {
    if (typeof(statNames) == "string") {
      drawGraph([statNames]);
    } else {
      drawGraph(statNames);
    }
  }

  function drawGraph(statNames) {
    $("#statGraph").empty();
    var series = [];
    $.each(statNames, function(i, statName) {
      series.push({name : statName, color : colors[i]});
    });
    var end = new Date($("#endDate").val());
    var start = new Date($("#startDate").val());
    var interval = "";
    var duration = end.getTime() - start.getTime();
    if (duration > 5184000000) {
      interval = "&interval=604800000";
    } else if (duration > 172800000) {
      interval = "&interval=86400000"
    }
    var url = "/services/stats/counts/" + encodeURIComponent(statNames.join(",")) + "?start=" +
        start.getTime() +
        "&end=" + end.getTime() + interval + "&nocache=true";
    new Rickshaw.Graph.Ajax({
      element: document.getElementById("statGraph"),
      width: 920,
      height: 300,
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

  function drawSystemErrors() {
    drawGraphs("app_error_count", "statGraph");
  }

  function drawSelectedGraph() {
    var val = $("#statList").val();
    switch (val) {
      case "System Errors":
        drawSystemErrors();
        break;
      case "Google Lookups":
        drawGraphs(["foodtruck.geolocation.GoogleGeolocator_locate_total",
          "foodtruck.geolocation.GoogleGeolocator_locate_failed"]);
        break;
      case "Yahoo Lookups":
        drawGraphs(["foodtruck.geolocation.YahooGeolocator_locate_total",
          "foodtruck.geolocation.YahooGeolocator_locate_failed"]);
        break;
      case "Twitter Hits":
        drawGraphs(["foodtruck.twitter.TwitterServiceImpl_updateTwitterCache_total",
          "foodtruck.twitter.TwitterServiceImpl_updateTwitterCache_failed"]);
        break;
      case "Database Geolocation Hits":
        drawGraphs(["cacheLookup_total", "cacheLookup_failed"]);
        break;


      case "Alexa Requests":
        drawGraphs(["alexa_intent_GetFoodTrucksAtLocation", "alexa_intent_WhereIsTruck", "alexa_intent_DailySpecials",
          "alexa_intent_AboutTruck", "alexa_intent_CategorySearch"]);
        break;
      case "Trucks on the Road":
        drawGraphs("trucks_on_the_road");
        break;
    }
  }

  $("#applyButton").click(function () {
    drawSelectedGraph();
  });

  $(document).ready(function() {
    drawSystemErrors();
  });

  $("#statList").change(function () {
    drawSelectedGraph();
  });
</script>

<%@include file="dashboardFooterBS3.jsp" %>
