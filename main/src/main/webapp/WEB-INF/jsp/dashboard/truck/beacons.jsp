<%@include file="../dashboardHeader1.jsp" %>

<style type="text/css">
  .gm-style-iw h4, .gm-style-iw td {
    color: black;
  }

  .gm-style-iw .glyphicons, .gm-style-iw td, .gm-style-iw p {
    font-size:20px;
    color: black;
  }

</style>

<%@include file="truckNav.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a class="btn btn-outline-primary" href="/admin/trucks/${truck.id}/linxup_config"><span class="icon icon-cog"></span> Settings</a>
  </div>
</div>

<div class="row mt-4">
  <div class="col-md-12">
    <div id="map_canvas" style="width:100%; height:400px; padding-bottom:20px;"></div>
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


<script type="application/json" id="blacklist">${blacklist}</script>
<script type="application/json" id="beaconData">{
  "truckId": "${truck.id}"
}</script>


<%@include file="../dashboardFooter1.jsp" %>
