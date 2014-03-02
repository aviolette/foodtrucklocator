
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
  var start = new Date(2011, 9, 1);
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
