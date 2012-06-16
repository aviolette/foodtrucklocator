<%@ include file="dashboardHeader.jsp" %>


<a href="/cron/tweets" class="btn primary" id="twitterButton">Refresh all tweets</a>

<a href="#" class="btn" id="newTruck">New Truck</a>

<h3>Active Trucks</h3>
<table>
  <thead>
  <tr>
    <th>Truck</th>
    <th>Current Location</th>
    <th>Next Location</th>
    <th>Twittalyzer</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" items="${trucks}">
    <c:if test="${truckStops.active}">

      <tr>
        <td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
        <td><c:choose><c:when
            test="${!empty(truckStops.currentStop)}"><c:url value="/admin/locations"
                                                            var="locationUrl">
          <c:param name="q" value="${truckStops.currentStop.location.name}"/>
        </c:url><a
            href="${locationUrl}">${truckStops.currentStop.location.name}</a></c:when><c:otherwise>Not Active</c:otherwise></c:choose>
        </td>
        <td><c:choose><c:when
            test="${!empty(truckStops.nextStop)}"><c:url value="/admin/locations" var="locationUrl">
          <c:param name="q" value="${truckStops.nextStop.location.name}"/>
        </c:url><a
            href="${locationUrl}">${truckStops.nextStop.location.name}</a></c:when><c:otherwise>None</c:otherwise></c:choose>
        </td>
        <td><c:choose><c:when
            test="${truckStops.truck.usingTwittalyzer}"></c:when><c:otherwise><span
            class="label warning">off</span></c:otherwise></c:choose></td>
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
    <th>Twittalyzer</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" items="${trucks}">
    <c:if test="${!truckStops.active && !truckStops.truck.inactive}">
      <tr>
        <td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
        <td><c:choose><c:when
            test="${truckStops.truck.usingTwittalyzer}"></c:when><c:otherwise><span
            class="label warning">off</span></c:otherwise></c:choose></td>
      </tr>
    </c:if>
  </c:forEach>
  </tbody>
</table>
<div id="inactiveTrucks">
  <h3>Inactive Trucks</h3>
  <table>
    <thead>
    <tr>
      <th>Truck</th>
      <th>Twittalyzer</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="truckStops" items="${trucks}">
      <c:if test="${!truckStops.active && truckStops.truck.inactive}">
        <tr>
          <td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
          <td><c:choose><c:when
              test="${truckStops.truck.usingTwittalyzer}"></c:when><c:otherwise><span
              class="label warning">off</span></c:otherwise></c:choose></td>
        </tr>
      </c:if>
    </c:forEach>
    </tbody>
  </table>
</div>
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

    bindAjaxCallToButton("twitterButton", "/cron/tweets");

    function newTruckDialog() {
      var truckId = prompt("Enter truck ID:");
      $.ajax({
        url : "/services/trucks",
        type: "POST",
        contentType: "application/json",
        data : JSON.stringify({id : truckId, name : "UNNAMED-" + truckId, twitterHandle: truckId}),
        success : function() {
          location.href = "/admin/trucks/" + truckId
        }
      });
    }

    $("#newTruck").click(function(e) {
      e.preventDefault();
      newTruckDialog();
    });

  })();
</script>

<%@ include file="dashboardFooter.jsp" %>