<%@ include file="truckHeader.jsp" %>

<div class="row">
  <div class="col-md-6">
    <h2>Statistics</h2>
    <table class="table">
      <tr>
        <td>Last active</td>
        <td><joda:format value="${truck.stats.lastSeen}" style="MS"/> @ <ftl:location
            location="${truck.stats.whereLastSeen}"/></td>
      </tr>
      <tr>
        <td>Stops this year</td>
        <td>${truck.stats.stopsThisYear}</td>
      </tr>
      <tr>
        <td>Total stops</td>
        <td>${truck.stats.totalStops}</td>
      </tr>
      <tr>
        <td>Twitter Linked?</td>
        <td>${truck.hasTwitterCredentials ? "YES" : "NO"}</td>
      </tr>
      <tr>
        <td>Facebook Linked?</td>
        <td>${truck.hasFacebookCredentials ? "YES" : "NO"}</td>
      </tr>
      <tr>
        <td>Post at New Stop</td>
        <td>${truck.postAtNewStop ? "YES" : "NO"}</td>
      </tr>
    </table>
  </div>
  <div class="col-md-12">
    <h2>Images</h2>
    <table class="table">
      <tr>
        <td>Icon Image</td>
        <td><c:choose><c:when test="${empty(truck.iconUrl)}">not set</c:when><c:otherwise><a
            href="${truck.iconUrl}"><img src="${truck.iconUrl}" width="48" height="48"/></a></c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Preview Image</td>
        <td><c:choose><c:when test="${empty(truck.previewIcon)}">not set</c:when><c:otherwise><a
            href="${truck.previewIcon}"><img src="${truck.previewIcon}" width="150"
                                             height="150"/></a></c:otherwise></c:choose></td>
      </tr>
      <tr>
        <td>Banner Image</td>
        <td><c:choose><c:when test="${empty(truck.backgroundImage)}">not set</c:when><c:otherwise><a
            href="${truck.backgroundImage}"><img src="${truck.backgroundImage}" width="320"
                                                 height="160"/></a></c:otherwise></c:choose></td>
      </tr>
      <tr>
        <td>Large Banner Image</td>
        <td><c:choose><c:when test="${empty(truck.backgroundImageLarge)}">not set</c:when><c:otherwise><a
            href="${truck.backgroundImageLarge}">${fn:substring(truck.backgroundImage, 0, 47)}...</a></c:otherwise></c:choose>
        </td>
      </tr>
    </table>
  </div>
</div>

<%@ include file="truckFooter.jsp" %>