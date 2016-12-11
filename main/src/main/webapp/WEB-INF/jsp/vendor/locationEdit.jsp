<%@ include file="vendorheader.jsp" %>

<div class="row">
  <div class="col-md-12">
    <h2 id="nameHeader"></h2>
    <p class="lead">Drag the red pin to location or use the "Search for Location" button to specify an address.   Then click 'Continue'.</p>
  </div>
</div>

<div class="row">
  <div class="col-md-8">
    <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;"></div>
  </div>
  <div class="col-md-4">
    <div>
      <a id="locationSearchButton" href="#" class="btn btn-default"><span class="glyphicon glyphicon-search"></span> Search for a location</a>
    </div>
    <nav>
      <p id="markerMessage" class="hidden">Select a location to move the marker</p>
      <ul style="margin-top:20px" class="nav list-unstyled" id="searchLocations">
      </ul>
    </nav>
  </div>
</div>


<div class="row">
  <div class="col-md-12">
    <div class="form-inline">
      <div class="form-group ">
        <label for="latitude">Latitude</label>
        <input id="latitude" class="form-control" readonly type="text"/>
      </div>
      <div class="form-group">
        <label for="longitude">Longitude</label>
        <input id="longitude" class="form-control disabled" readonly type="text"/>
      </div>
    </div>
    <div>
      <div class="form-group">
        <label for="description">Description</label>
        <textarea class="form-control" placeholder="Optional description" id="description" rows="5"
                  cols="80"></textarea>

      </div>
      <div class="form-group">
        <label for="url">URL</label>
        <div class="input-group">
          <input id="url" class="form-control" type="url"/>
          <span class="input-group-btn">
            <button id="viewUrl" class="btn btn-default"><span class="glyphicon glyphicon-new-window"></span></button>
          </span>
        </div>
      </div>
      <div class="form-group">
        <label for="twitterHandle">Twitter Handle</label>
        <div>
          <div class="input-group">
            <span class="input-group-addon">@</span>
            <input id="twitterHandle" class="form-control" type="text"/>
            <span class="input-group-btn">
              <button id="viewTwitter" class="btn btn-default"><span
                  class="glyphicon glyphicon-new-window"></span></button>
            </span>
          </div>
        </div>
      </div>
      <div class="form-group">
        <label for="facebookUri">Facebook</label>
        <input id="facebookUri" placeholder="For example /chicagofoodtruckfinder" class="form-control" type="text"/>
      </div>
      <div class="form-group">
        <label for="email">Email</label>
        <input id="email" class="form-control" type="email"/>
      </div>
      <div class="form-group">
        <label for="phone" class="control-label">Phone</label>
        <input id="phone" class="form-control" type="tel"/>
      </div>
      <div class="checkbox">
        <label><input id="hasBooze" type="checkbox">&nbsp;Serves Alcohol?</label>
      </div>
      <div class="btn-toolbar">
        <div class="btn-group">
          <a href="/vendor" class="btn btn-default">Cancel</a>
        </div>
        <div class="btn-group">
          <button id="submitButton" class="btn btn-primary">Continue</button>
        </div>
      </div>
    </div>
  </div>
</div>



<script type="text/javascript"
        src="//maps.googleapis.com/maps/api/js?key=${googleApiKey}">
</script>
<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript" src="/script/location-edit.js"></script>

<script type="text/javascript">
  $(document).ready(function() {
    var loc = ${location};
    <%-- force to initial location --%>
    if (!loc.valid) {
      loc.latitude = 41.8807438;
      loc.longitude = -87.6293867;
      loc.valid = true;
    }

    locationEdit(loc, true, function() {
      <c:choose>
      <c:when test="${!empty(startTime) && !empty(endTime)}">
        $.post("/vendor/locations/" + loc.key + "/edit?startTime=${startTime}&endTime=${endTime}&locationId=${locationId}", function() {
          location.href="/vendor";
        });
      </c:when>
      <c:otherwise>
        location.href = "/vendor";
      </c:otherwise>
      </c:choose>
    }, function (e) {
      $("#flash").append(e);
      $("#flash").addClass("alert-danger");
      $("#flash").removeClass("alert-warning");
      $("#flash").removeClass("hidden");
    });

    $("#nameHeader").append(loc.name);
  });
</script>
<%@ include file="vendorfooter.jsp" %>
