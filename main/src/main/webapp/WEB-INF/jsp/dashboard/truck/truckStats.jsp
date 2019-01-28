<%@include file="../dashboardHeader1.jsp" %>


<ul class="nav nav-bordered mt-4 mt-md-2 mb-0 clearfix" role="tablist">
  <li class="nav-item" role="presentation">
    <a class="nav-link"  href="/admin/trucks/${truck.id}">Overview</a>
  </li>
  <li class="nav-item"><a class="nav-link" href="/admin/trucks/${truck.id}/configuration">Configuration</a></li>
  <li class="nav-item"><a class="nav-link active show" href="/admin/trucks/${truck.id}/stats">Stats</a></li>
  <li class="nav-item"><a class="nav-link" href="/admin/trucks/${truck.id}/beacons">Beacons</a></li>
  <li class="nav-item"><a class="nav-link" href="/admin/trucks/${truck.id}/danger">Danger Zone</a></li>


</ul>


<div class="row statcards mt-3 mb-3 text-xs-center text-md-left">

  <div class="statcard col-6 col-md-4 mb-4">
    <h3 class="statcard-number text-success"><joda:format value="${truck.stats.firstSeen}" style="MS"/> @ <ftl:location
        location="${truck.stats.whereFirstSeen}"/></h3>
    <span class="statcard-desc">First Active</span>
  </div>

  <div class="statcard col-6 col-md-4 mb-4">
    <h3 class="statcard-number text-success">${totalTrucks}</h3>
    <span class="statcard-desc">Trucks on the Road</span>
  </div>

</div>



<div class="row">
  <div class="col-md-6">
    <h2>Statistics</h2>
    <table class="table">
      <tr>
        <td>First active</td>
        <td><joda:format value="${truck.stats.firstSeen}" style="MS"/> @ <ftl:location
            location="${truck.stats.whereFirstSeen}"/></td>
      </tr>
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
    <p>
      This button syncs all timeseries stats from the beginning of the chicago food truck finder.
    </p>
    <button id="updateStats" class="btn btn-warning">Update Stats</button>
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

<script type="text/javascript">
  $("#updateStats").click(function (e) {
    e.preventDefault();
    $.ajax({
      url: '/cron/updateTruckStats?truckId=${truck.id}&force=true',
      complete: function () {
        location.reload();
      }
    });
  });
</script>

<%@include file="../dashboardFooter1.jsp" %>
