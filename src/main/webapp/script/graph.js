
function drawGraphs(statNames, containerId, interval, start, end, nocache) {
  if (typeof(statNames) == "string") {
    drawGraph([statNames], containerId, interval, start, end, nocache);
  } else {
    drawGraph(statNames, containerId, interval, start, end, nocache);
  }
}

function drawGraph(statNames, containerId, interval, start, end, nocache) {
  var series = [], type = "area", nocacheParam="";
  var colors = ["steelblue", "red", "green", "yellow", "orange", "cyan", "darkgray", "lawngreen", "midnightblue", "cadetblue", "burlywood", "chocolate", "coral", "darkmagenta", "darkolivegreen", "darkorchid", "darkviolet", "deepskyblue", "fuschia"];
  $.each(statNames, function(i, statName) {
    series.push({name : statName, color : colors[i]});
  });
  if (nocache) {
    nocacheParam = "&nocache=true";
  }
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
      "&end=" + end.getTime() + nocacheParam;
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
      var graph = transport.graph, xAxis, yAxis;
      graph.renderer.unstack = true;
      new Rickshaw.Graph.HoverDetail({ graph: graph });
      xAxis = new Rickshaw.Graph.Axis.Time({ graph: graph });
      xAxis.render();
      yAxis = new Rickshaw.Graph.Axis.Y({ graph: graph });
      yAxis.render();
    },
    series: series
  });
}
