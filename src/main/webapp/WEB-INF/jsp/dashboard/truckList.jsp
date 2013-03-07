<%@ include file="dashboardHeader.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a href="/cron/tweets" class="btn primary" id="twitterButton">Refresh all tweets</a>
  </div>
  <div class="btn-group toggle-visibility" >
    <button id="inactiveButton" type="button" class="btn active">Inactive</button>
    <button id="muteButton" type="button" class="btn active">Muted</button>
  </div>
  <div class="btn-group">
    <a href="#" class="btn" id="newTruck">New Truck</a>&nbsp;
  </div>
</div>
<h3>Active Trucks</h3>
<table class="table table-striped">
  <thead>
  <tr>
    <th>Truck</th>
    <th>Current Location</th>
    <th>Ends At</th>
    <th>Starts At</th>
    <th>Next Location</th>
    <th>Categories</th>
    <th>Twittalyzer</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" items="${trucks}">
    <c:if test="${truckStops.active}">

      <tr>
        <td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
        <td>
          <c:choose>
          <c:when
              test="${!empty(truckStops.currentStop)}">
          <c:url value="/admin/locations"
                 var="locationUrl">
            <c:param name="q" value="${truckStops.currentStop.location.name}"/>
          </c:url> <a
            href="${locationUrl}">${truckStops.currentStop.location.name}</a></td>
        <td><joda:format value="${truckStops.currentStop.endTime}" style="-S"/></c:when><c:otherwise>
          Not Active
        </td>
        <td>&nbsp;</c:otherwise>
          </c:choose>

        </td>
        <td>
          <c:choose>
          <c:when
              test="${!empty(truckStops.nextStop)}">
          <c:url value="/admin/locations" var="locationUrl">
            <c:param name="q" value="${truckStops.nextStop.location.name}"/>
          </c:url> <joda:format value="${truckStops.nextStop.startTime}" style="-S"/></td>
        <td><a
            href="${locationUrl}">${truckStops.nextStop.location.name}</a></c:when>
          <c:otherwise></td><td>None</c:otherwise>
          </c:choose>
        </td>
        <td>${truckStops.truck.categoryList}</td>
        <td><c:choose><c:when
            test="${truckStops.truck.usingTwittalyzer}"></c:when><c:otherwise><span
            class="label warning">off</span></c:otherwise></c:choose></td>
      </tr>
    </c:if>
  </c:forEach>
  </tbody>
</table>
<h3>Trucks That Are Inactive Today</h3>
<table class="table table-striped">
  <thead>
  <tr>
    <th>Truck</th>
    <th>Categories</th>
    <th>Twittalyzer</th>
    <th>&nbsp;</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" items="${trucks}">
    <c:if test="${!truckStops.active && !truckStops.truck.inactive}">
      <tr <c:if test="${truckStops.truck.muted}">class="muted"</c:if>>
        <td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
        <td>${truckStops.truck.categoryList}</td>
        <td><c:choose><c:when
            test="${truckStops.truck.usingTwittalyzer}"></c:when><c:otherwise><span
            class="label warning">off</span></c:otherwise></c:choose></td>
        <td>
          <button class="btn mute-button" for-truck="${truckStops.truck.id}"><c:choose><c:when
              test="${truckStops.truck.muted}">Unmute</c:when><c:otherwise>Mute</c:otherwise></c:choose></button>
        </td>
      </tr>
    </c:if>
  </c:forEach>
  </tbody>
</table>
<div id="inactiveTrucks" style="display:none">
  <h3>Inactive Trucks</h3>
  <table class="table table-striped">
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
          <td>${truckStops.truck.categoryList}</td>
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
    function toggleMuted($muteButton) {
      var displayValue = $muteButton.hasClass("active") ? "table-row" : "none";
      $(".muted").css("display", displayValue);
    }

    $('.toggle-visibility button').click(function(e) {
      var $target = $(e.target);
      if ($target.attr("id") == 'inactiveButton') {
        var displayValue = $target.hasClass("active") ? "block" : "none";
        $("#inactiveTrucks").css("display", displayValue);
      } else {
        var displayValue = $target.hasClass("active") ? "table-row" : "none";
        $(".muted").css("display", displayValue);
      }
      if ($target.hasClass("active")) {
        $target.removeClass("active");
      } else {
        $target.addClass("active");
      }
    });
    $(".mute-button").click(function(e) {
      var truckId = $(e.target).attr("for-truck");
      var inner = $(e.target).html();
      var verb = inner == "Mute" ? "mute" : "unmute";
      $.ajax({
        url : "/services/trucks/" + truckId + "/" + verb,
        type: "POST",
        success : function() {
          $(e.target).html(verb == "mute" ? "Unmute" : "Mute");
          if (verb == "mute") {
            $(e.target.parentNode.parentNode).addClass("muted");
          } else {
            $(e.target.parentNode.parentNode).removeClass("muted");
          }
          var displayValue = $("#muteButton").hasClass("active") ? "none" : "table-row";
          $(".muted").css("display", displayValue);
        }
      })
    });
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
      if (!truckId) {
        return;
      }
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