<%@include file="dashboardHeader1.jsp" %>
<div class="row">
  <div class="col-md-12">
    <h2 class="dashhead-title">Trucks</h2>
    <div>
      <button class="btn btn-outline-primary" id="new-truck">New Truck</button>
    </div>
  </div>
</div>
<div class="hr-divider my-4">
  <h3 class="hr-divider-content hr-divider-heading">Quick Stats</h3>
</div>
<div class="row statcards mt-3 mb-3 text-xs-center text-md-left">

  <div class="statcard col-6 col-md-4 mb-4">
    <h3 class="statcard-number text-success">${activeStops}</h3>
    <span class="statcard-desc">Stops</span>
  </div>

  <div class="statcard col-6 col-md-4 mb-4">
    <h3 class="statcard-number text-success">${totalTrucks}</h3>
    <span class="statcard-desc">Trucks on the Road</span>
  </div>

</div>

<div class="row">
  <div class="col-md-12">
    <div class="flextable table-actions">
      <div class="flextable-item">
        <div class="btn-group btn-group-toggle" data-toggle="buttons">
          <label class="btn btn-secondary active filter-radio-label"><input type="radio" class="filter-radio" id="with-stops" name="stops" autocomplete="off" checked="checked">
            Stops</label>
          <label class="btn btn-secondary filter-radio-label"><input type="radio" class="filter-radio"  id="without-stops" name="stops" autocomplete="off" >
            No Stops</label>
          <label class="btn btn-secondary filter-radio-label"><input type="radio" class="filter-radio"  id="inactive-stops" name="stops" autocomplete="off" >
            Inactive</label>
        </div>
      </div>
    </div>
    <div class="table-full">
      <table class="table" data-sort="table">
        <thead>
        <tr>
          <th>&nbsp;</th>
          <th>Truck</th>
          <th>Current Location</th>
          <th>Next Location</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="truckStops" varStatus="truckStopsStatus" items="${trucks}">

          <tr class="d-none <c:choose><c:when test="${truckStops.active}">active-stop</c:when><c:when test="${!truckStops.active and !truckStops.truck.inactive}">inactive-stops</c:when><c:otherwise>inactive-truck</c:otherwise></c:choose>">
            <td class="img-col"><a class="truckLink" href="/admin/trucks/${truckStops.truck.id}"><img alt="truck icon"
                                                                                                      class="media-object img-responsive img-rounded" width="60" heigh="60"
                                                                                                      src="${truckStops.truck.previewIconUrl.protocolRelative}"/></a>
            </td>
            <td><a class="truckLink pull-left" href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a>
            </td>
            <td><c:choose>
              <c:when
                  test="${!empty(truckStops.currentStop)}">
                <ftl:location admin="true" location="${truckStops.currentStop.location}"/><br/>
                Departs: <ftl:date at="${truckStops.currentStop.endTime}" style="-S"/>
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
                                                                        admin="true"/><br/>Starts: <ftl:date
                    at="${truckStops.nextStop.startTime}"
                    style="-S"/> </c:when>
                <c:otherwise>None</c:otherwise></c:choose>
            </td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</div>


<%@include file="dashboardFooter1.jsp" %>
