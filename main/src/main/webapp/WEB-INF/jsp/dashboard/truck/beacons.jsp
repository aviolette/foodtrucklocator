<%@include file="../dashboardHeader1.jsp" %>

<style type="text/css">
  @media (max-width: 990px) {
    .location-related {
      display: none;
    }
  }

  @media (min-width: 990px) {
    .location-related {
      display: block;
    }
  }
</style>

<div class="btn-toolbar">
  <div class="btn-group">
    <a class="btn btn-primary" href="/admin/trucks/${truck.id}/linxup_config">Setup</a>
  </div>
</div>

<c:if test="${!empty(linxupAccount)}">
  <div class="row mt-4">
    <div class="col-md-12">
      <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;" class="location-related"></div>
    </div>
  </div>

  <div class="row mt-4">
    <div class="col-md-12">
      <h2>Beacons </h2>
      <table class="table">
        <thead>
        <tr>
          <th>Name</th>
          <th class="large-screen-only">Device Id</th>
          <th>Last Broadcast</th>
          <th class="large-screen-only">Last Checked</th>
          <th>Status</th>
          <th>&nbsp;</th>
        </tr>
        </thead>
        <tbody id="beacons">
        </tbody>
      </table>
    </div>
  </div>

</c:if>


<script type="application/json" id="blacklist">${blacklist}</script>
<script type="application/json" id="beaconData">{"truckId": "${truck.id}"}</script>


<%@include file="../dashboardFooter1.jsp" %>
