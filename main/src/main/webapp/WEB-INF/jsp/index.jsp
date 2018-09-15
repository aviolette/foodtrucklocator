
<%@ include file="header.jsp" %>


<div class="row" id="sidebar">
  <div class="col-md-12" id="listContainer">
    <div id="motd" class="alert alert-success alert-dismissable d-none" role="alert">
      <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
      <p id="motd-message"></p>
    </div>
    <div class="alert alert-info" id="flashMsg" style="display:none">
    </div>
    <div id="tab-section">
      <ul class="nav nav-tabs" id="navTabs" role="tablist">
        <li class="nav-item"><a id="now-tab" data-toggle="tab" role="tab" class="nav-link active" href="#nowTrucks">Open Now</a></li>
        <li class="nav-item"><a id="later-tab" data-toggle="tab" role="tab" class="nav-link" href="#laterTrucks">Open Later</a></li>
      </ul>
      <div class="tab-content">
        <div id="nowTrucks" class="tab-pane fade active truckDL show" role="tabpanel">
        </div>
        <div id="laterTrucks" class="tab-pane fade truckDL" role="tabpanel">
        </div>
      </div>
    </div>
    <div class="jumbotron d-none" id="notrucks">
      <h1 class="display-4">There are No Trucks On The Road</h1>
      <p>Check back at a later time.</p>
    </div>
  </div>
</div>



<%@include file="include/core_js.jsp" %>
<script type="text/javascript" src="script/foodtruckfinder-1.33.js"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run("${mode}", { coords : {latitude: ${center.latitude}, longitude: ${center.longitude}}},
        new Date(${requestTimeInMillis}), ${payload}, "${appKey}", "${defaultCity}", ${mapButtons});
  });
</script>
<%@ include file="footer.jsp" %>
