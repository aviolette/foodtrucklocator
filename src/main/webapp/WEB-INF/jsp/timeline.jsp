<%@ include file="header.jsp" %>

<%@include file="include/core_js.jsp" %>

<style type="text/css">
  #timeline {
    width:100%;
  }
  table.graph {
    width: 100%;
  }
  td.firstCell {
    text-align:right;
  }
  td.graph {
    background-color: blue;
    color: white;
  }
</style>

<h2>Truck Timeline</h2>

<table id="timeline">
  <tbody id="timelineBody">

  </tbody>
</table>

<script type="text/javascript">
  (function() {
    var trucks = ${trucks}, $tb = $("#timelineBody"),
        sep15 = 1316062800000,
        now = new Date().getTime(),
        total = now - sep15;

    function buildGraph(truck) {
      var first = truck.firstSeen - sep15;
      if (first < 0) {
        first = 0;
      }
      var last = truck.lastSeen - truck.firstSeen,
        firstPercent = Math.floor((first / total) * 100),
        graphPercent = Math.floor(last / total * 100) + 1,
        firstText = "&nbsp;",
        graphText = "&nbsp;";
      if (firstPercent > graphPercent) {
        firstText = truck.name;
      } else {
        graphText = truck.name;
      }
      return "<table class='graph'><tr><td style='width:" + firstPercent + "%' class='firstCell'>" + firstText + "</td><td style='width:" + graphPercent + "%' class='graph'>" + graphText + "</td><td class='third'>&nbsp;</td></tr></table>";
    }

    $.each(trucks.sort(function(a, b) {
      if (a.firstSeen == b.firstSeen) {
        return 0;
      }
      return (a.firstSeen > b.firstSeen) ? -1 : 1;
    }), function(idx, truck) {
      if (truck.firstSeen > 0) {
        $tb.append("<tr><td> " + buildGraph(truck) + " </td></tr>");
      }
    });
  })();
</script>


<%@ include file="footer.jsp" %>
