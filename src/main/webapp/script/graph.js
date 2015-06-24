
function drawGraphs(statNames, containerId, interval, start, end) {
  if (typeof(statNames) == "string") {
    drawGraph([statNames], containerId, interval, start, end);
  } else {
    drawGraph(statNames, containerId, interval, start, end);
  }
}

function drawGraph(statNames, containerId, interval, start, end) {
  var series = [], type = "area";
  var colors = ["steelblue", "red", "green", "yellow", "orange", "cyan", "darkgray", "lawngreen", "midnightblue", "cadetblue"];
  $.each(statNames, function(i, statName) {
    series.push({name : statName, color : colors[i]});
  });
  if (!interval) {
    interval = 604800000;
  }
  if (typeof(end) != "object") {
    end = new Date();
    start = new Date(2011, 9, 1);
  }
  if (statNames.length > 1) {
    type = "line";
  }
  var url = "/services/stats/counts/" + encodeURIComponent(statNames.join(",")) + "?start=" +
      start.getTime() +
      "&interval=" + interval +
      "&end=" + end.getTime();
  new Rickshaw.Graph.Ajax({
    element: document.getElementById(containerId),
    width: $("#" + containerId).width() - 20,
    height: 200,
    renderer: type,
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
