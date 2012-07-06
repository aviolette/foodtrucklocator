<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>
<script src="/script/lib/d3.min.js" type="text/javascript"></script>
<script src="/script/lib/d3.layout.min.js" type="text/javascript"></script>
<script src="/script/lib/rickshaw.min.js" type="text/javascript"></script>


<div id="chart"></div>

<script>

  var graph = new Rickshaw.Graph({
    element: document.querySelector("#chart"),
    width: 500,
    height: 200,
    series: [
      {
        color: 'steelblue',
        data: [
          { x: 0, y: 40 },
          { x: 1, y: 49 },
          { x: 2, y: 38 },
          { x: 3, y: 30 },
          { x: 4, y: 32 }
        ]
      }
    ]
  });

  graph.render();

</script>

<%@include file="dashboardFooter.jsp" %>
