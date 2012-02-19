<%@ include file="dashboardHeader.jsp" %>


<a href="/cron/recache" class="btn primary" id="recacheButton">Reload from calendar</a>
<a href="/cron/tweets" class="btn primary" id="twitterButton">Refresh all tweets</a>

<h3>Active Trucks</h3>
<table>
  <thead>
  <tr>
    <th>Truck</th>
    <th>Current Location</th>
    <th>Next Location</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" items="${trucks}">
    <c:if test="${truckStops.active}">
      <tr>
        <td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
        <td><c:choose><c:when
            test="${!empty(truckStops.currentStop)}">${truckStops.currentStop.location.name}</c:when><c:otherwise>Not Active</c:otherwise></c:choose></td>
        <td><c:choose><c:when
            test="${!empty(truckStops.nextStop)}">${truckStops.nextStop.location.name}</c:when><c:otherwise>None</c:otherwise></c:choose></td>
      </tr>
    </c:if>
  </c:forEach>
  </tbody>
</table>
<h3>Trucks That Are Inactive Today</h3>
<table>
  <thead>
  <tr>
    <th>Truck</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" items="${trucks}">
    <c:if test="${!truckStops.active && !truckStops.truck.inactive}">
      <tr>
        <td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
      </tr>
    </c:if>
  </c:forEach>
  </tbody>
</table>
<h3>Inactive Trucks</h3>
<table>
  <thead>
  <tr>
    <th>Truck</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" items="${trucks}">
    <c:if test="${!truckStops.active && truckStops.truck.inactive}">
      <tr>
        <td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
      </tr>
    </c:if>
  </c:forEach>
  </tbody>
</table>
<script type="text/javascript">
  (function() {
    function bindAjaxCallToButton(button, url) {
      var link = $("#" + button);
      link.click(function(evt) {
        evt.preventDefault();
        link.addClass("disabled");
        $.ajax({
          context: document.body,
          url: url,
          complete : function() {
            link.removeClass("disabled");
          },
          success: function() {
            window.location.reload();
          }
        });
      });
    }

    bindAjaxCallToButton("recacheButton", "/cron/recache");
    bindAjaxCallToButton("twitterButton", "/cron/tweets");
  })();
</script>

<%@ include file="dashboardFooter.jsp" %>