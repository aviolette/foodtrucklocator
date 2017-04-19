<%@ include file="dashboardHeaderBS3.jsp" %>
<style type="text/css">
  .img-col {
    width: 60px !important;
  }
</style>

<div class="btn-toolbar" >
  <div class="btn-group">
    <a href="#" class="btn btn-primary" id="newTruck"><span class="glyphicon glyphicon-plus"></span> New Truck</a>&nbsp;
  </div>
</div>

<!-- Nav tabs -->
<ul class="nav nav-tabs" role="tablist">
  <li <c:if test="${tab == 'home'}">class="active"</c:if>><a href="/admin/trucks" role="tab">Trucks with Stops</a></li>
  <li <c:if test="${tab == 'noStops'}">class="active"</c:if>><a href="/admin/trucks?tab=noStops" role="tab">Trucks with No stops</a></li>
  <li <c:if test="${tab == 'inactiveTrucks'}">class="active"</c:if>><a href="/admin/trucks?tab=inactiveTrucks" role="tab" >Inactive</a></li>
</ul>

<div class="tab-content">
  <div class="tab-pane <c:if test="${tab == 'home'}">active</c:if>" id="home">
    <table class="table table-striped active-trucks">
      <thead>
      <tr>
        <th class="img-col">&nbsp;</th>
        <th>Truck</th>
        <th>Current Location</th>
        <th>Next Location</th>
      </tr>
      </thead>
      <tbody>
      <c:forEach var="truckStops" varStatus="truckStopsStatus" items="${trucks}">
        <c:if test="${truckStops.active}">

          <tr class="rowItem">
            <td class="img-col"><a class="truckLink" href="/admin/trucks/${truckStops.truck.id}"><img alt="truck icon" class="media-object img-responsive img-rounded" src="${truckStops.truck.iconUrlObj.protocolRelative}" /></a></td>
            <td><a class="truckLink pull-left" href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
            <td><c:choose>
              <c:when
                  test="${!empty(truckStops.currentStop)}">
                <ftl:location admin="true" location="${truckStops.currentStop.location}"/><br/>
                Departs: <joda:format value="${truckStops.currentStop.endTime}" style="-S"/>
              </c:when>
              <c:otherwise>
                None
              </c:otherwise>
            </c:choose>
            </td>
            <td>
              <c:choose>
              <c:when
                  test="${!empty(truckStops.nextStop)}"><ftl:location location="${truckStops.nextStop.location}"
                                                                      admin="true"/><br/>Starts: <joda:format
                  value="${truckStops.nextStop.startTime}"
                  style="-S"/></c:when><c:otherwise>None</c:otherwise></c:choose>

          </td>
          </tr>
        </c:if>
      </c:forEach>
      </tbody>
    </table>
  </div>
  <div class="tab-pane <c:if test="${tab == 'noStops'}">active</c:if>" id="noStops">
    <label>Show muted <input type="checkbox" id="showMuted"></label>
    <table class="table table-striped inactive-trucks">
      <thead>
      <tr>
        <th class="img-col"></th>
        <th>Truck</th>
        <th class="hidden-xs">Categories</th>
        <th class="hidden-xs">Twittalyzer</th>
      </tr>
      </thead>
      <tbody>
      <c:forEach var="truckStops" items="${trucks}">
        <c:if test="${!truckStops.active && !truckStops.truck.inactive}">
          <tr <c:choose><c:when test="${truckStops.truck.muted}">class="muted rowItem"</c:when><c:otherwise>class="rowItem"</c:otherwise></c:choose>>
            <td class="img-col"><a class="truckLink" href="/admin/trucks/${truckStops.truck.id}"><img alt="truck icon" class="media-object img-responsive img-rounded" src="${truckStops.truck.iconUrlObj.protocolRelative}" /></a></td>
            <td><a class="truckLink" href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
            <td class="hidden-xs"><c:forEach var="category"
                                             items="${truckStops.truck.categories}">&nbsp;<span class="label label-primary">${category}</span></c:forEach>
            </td>
            <td class="hidden-xs"><c:choose><c:when
                test="${truckStops.truck.usingTwittalyzer}"></c:when><c:otherwise><span
                class="label warning">off</span></c:otherwise></c:choose></td>
          </tr>
        </c:if>
      </c:forEach>
      </tbody>
    </table>
  </div>
  <div class="tab-pane  <c:if test="${tab == 'inactiveTrucks'}">active</c:if>" id="inactiveTrucks">
    <table class="table table-striped">
      <thead>
      <tr>
        <th>Truck</th>
        <th>Categories</th>
      </tr>
      </thead>
      <tbody>
      <c:forEach var="truck" items="${inactiveTrucks}">
          <tr class="rowItem">
            <td><a href="/admin/trucks/${truck.id}">${truck.name}</a></td>
            <td>${truck.categoryList}</td>
            <td><c:choose><c:when
                test="${truck.usingTwittalyzer}"></c:when><c:otherwise><span
                class="label warning">off</span></c:otherwise></c:choose></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
 </div>

<script type="text/javascript">
  (function() {

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
          location.href = "/admin/trucks/" + truckId.toLowerCase()
        }
      });
    }

    $("#newTruck").click(function(e) {
      e.preventDefault();
      newTruckDialog();
    });

  })();
</script>

<%@ include file="dashboardFooterBS3.jsp" %>