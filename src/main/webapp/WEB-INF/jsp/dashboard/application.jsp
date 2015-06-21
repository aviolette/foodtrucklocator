<%@include file="dashboardHeaderBS3.jsp" %>

<link href="/css/rickshaw/rickshaw.min.css" rel="stylesheet">
<style type="text/css">
  .rickshaw_graph {
  }
</style>

<script src="/script/lib/d3.min.js" type="text/javascript"></script>
<script src="/script/lib/d3.layout.min.js" type="text/javascript"></script>
<script src="/script/lib/rickshaw.min.js" type="text/javascript"></script>


<div class="row">
  <div class="col-md-12">
    <h3>Application</h3>
    <dl>
      <dt>Name</dt>
      <dd>${application.name}</dd>
      <dt>Code</dt>
      <dd>${application.key}</dd>
      <dt>Description</dt>
      <dd>${application.description}</dd>
    </dl>
    <div id="appGraph"></div>
  </div>
</div>

<script>
  var colors = ["steelblue", "red", "green", "yellow", "orange", "cyan", "darkgray", "lawngreen", "midnightblue", "cadetblue"];

  function drawGraphs(statNames, containerId) {
    var series = [];
    $.each(statNames, function(i, statName) {
      series.push({name : statName, color : colors[i]});
    });
    var DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    var end = new Date();
    var start = new Date(end.getTime() - (30 *DAY_IN_MILLIS));
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
    drawGraphs(["service.count.daily.${application.key}"], "appGraph");
  });
</script>


<%@include file="dashboardFooterBS3.jsp" %>
