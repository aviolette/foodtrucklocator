
<%@ include file="header.jsp" %>


<div class="row" id="sidebar">
  <div class="col-md-12" id="listContainer">
    <div id="motd" class="alert alert-warning alert-dismissable hidden">
      <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
      <span id="motd-message"></span>
    </div>
    <div class="alert alert-info" id="flashMsg" style="display:none">
    </div>
    <ul class="nav nav-tabs" id="navTabs">
      <li class="active"><a href="#nowTrucks" data-toggle="tab">Open Now</a></li>
      <li><a href="#laterTrucks" data-toggle="tab">Open Later</a></li>
    </ul>
    <div class="tab-content">
      <div id="nowTrucks" class="tab-pane active truckDL">
        <div class="row truck-row">
          <h2>Foo</h2>
          <div class="row">
            <div class="col-xs-6 col-md-3">
              <a href="/trucks/cornerfarmacy">
                <div class="thumbnail">
                  <img src="https://storage.googleapis.com/truckicons/cornerfarmacy_preview.jpg" width="180" height="180" class="img-rounded">
                  <p class="text-center"><strong>Corner Farmacy<br/>8:00am - 10:00am</strong></p>
                </div>
              </a>
            </div>
          </div>
        </div>

      </div>
      <div id="laterTrucks" class="tab-pane truckDL">
      </div>
    </div>
  </div>
</div>



<%@include file="include/core_js.jsp" %>
<script type="text/javascript" src="script/foodtruckfinder-1.3.1.js"></script>
<script type="text/javascript">
  $(document).ready(function() {
    FoodTruckLocator.run("${mode}", { coords : {latitude: ${center.latitude}, longitude: ${center.longitude}}},
        new Date(${requestTimeInMillis}), ${payload}, "${appKey}", "${defaultCity}", ${mapButtons});
  });
</script>
<%@ include file="footer.jsp" %>
