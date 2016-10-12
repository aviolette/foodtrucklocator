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
  td.graph a {
    display:block;
    width:100%;
    color:white;
  }
  td.graph a.visited {
    color:white;
    text-decoration:none;
  }
</style>

<h2>Truck Timeline</h2>

<p>This graph represents the relative start and end time of food trucks as they were first and last seen on my site. Most recent trucks to appear, are on top whereas oldest trucks are on the bottom.</p>

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

    function dateFormat(d) {
      return (d.getMonth() + 1) + "/" + d.getDate() + "/" + d.getFullYear();
    }

    function truckLink(truck) {
      var first = dateFormat(new Date(truck.firstSeen)),
          last = dateFormat(new Date(truck.lastSeen)),
          title = "First seen on " + first + "; last seen: " + last;
      return "<a title='" + title +"' href='/trucks/" + truck.id + "'>" + truck.name + "</a>";
    }

    function buildGraph(truck) {
      var first = truck.firstSeen - sep15;
      if (first < 0) {
        first = 0;
      }
      var last = truck.lastSeen - truck.firstSeen,
        firstPercent = Math.floor((first / total) * 100),
        graphPercent = Math.floor(last / total * 100) + 1,
        lastPercent = Math.floor(Math.max(0, 100 - firstPercent - graphPercent)),
        firstText = "&nbsp;",
        graphText = "&nbsp;";
      if (firstPercent > graphPercent) {
        firstText = truckLink(truck);
      } else {
        graphText = truckLink(truck);
      }
      return "<table class='graph'><tr><td style='width:" + firstPercent + "%' class='firstCell'>" + firstText + "</td><td style='width:" + graphPercent + "%' class='graph'>" + graphText + "</td><td style='width:" + lastPercent +"%'>&nbsp;</td></tr></table>";
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
