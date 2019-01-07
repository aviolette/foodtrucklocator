<%@include file="dashboardHeader1.jsp" %>
<div class="row">
  <div class="col-sm-12">
    <h2 class="dashhead-title">Trucks</h2>

    <div class="flextable table-actions">
      <div class="flextable-item flextable-primary">
        <button class="btn btn-primary-outline" id="new-truck">New Truck</button>
      </div>
      <div class="flextable-item">
        <div class="radio-inline custom-control custom-radio">
          <label><input type="radio" id="with-stops" name="stops" checked="checked">
            <span class="custom-control-indicator"></span>With Stops</label>
        </div>
        <div class="radio-inline custom-control custom-radio">
          <label><input type="radio" id="without-stops" name="stops">
            <span class="custom-control-indicator"></span>No Stops</label>
        </div>
        <div class="radio-inline custom-control custom-radio">
          <label><input type="radio" id="inactive-stops" name="stops">
            <span class="custom-control-indicator"></span>Inactive</label>
        </div>
      </div>
    </div>
    <div class="table-full">
      <table class="table" data-sort="table">
        <tbody>
        <c:forEach var="truckStops" varStatus="truckStopsStatus" items="${trucks}">

          <tr class="hidden rowItem <c:choose><c:when test="${truckStops.active}">active-stop</c:when><c:when test="${!truckStops.active}">inactive-stops</c:when><c:otherwise>inactive-truck</c:otherwise></c:choose>">
            <td class="img-col"><a class="truckLink" href="/admin/trucks/${truckStops.truck.id}"><img alt="truck icon"
                                                                                                      class="media-object img-responsive img-rounded"
                                                                                                      src="${truckStops.truck.iconUrlObj.protocolRelative}"/></a>
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
