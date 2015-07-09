<%@include file="dashboardHeaderBS3.jsp" %>

<link href="/css/rickshaw/rickshaw.min.css" rel="stylesheet">
<%@include file="../include/graph_libraries.jsp" %>

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
  $(document).ready(function() {
    var end = new Date(), start = new Date(end.getTime() - (30 *86400000));
    drawGraphs(["service.count.daily.${application.key}"], "appGraph", 86400000, start, end, true);
  });
</script>

<%@include file="dashboardFooterBS3.jsp" %>
