<%@include file="dashboardHeaderBS3.jsp" %>
<style>
  .form-inline {
    margin-bottom: 15px;
  }
</style>
<c:if test="${!empty(locationId)}">
  <div class="row">
    <div class="col-md-12">
      <div class="btn-toolbar">
        <div class="btn-group">
          <a href="/admin/event_at/${locationId}" class="btn btn-default"><span class="glyphicon glyphicon-plus"></span>
            New Event</a>
        </div>
        <div class="btn-group">
          <a id="locationViewButton" href="/locations/${locationId}" class="btn btn-default">View</a>
        </div>
      </div>
    </div>
  </div>
</c:if>

<div class="row" style="padding-top: 20px">
  <div class="col-md-8">
    <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;"></div>
  </div>
  <div class="col-md-4">
    <div>
      <a id="locationSearchButton" href="#" class="btn btn-default"><span class="glyphicon glyphicon-search"></span> Search for a location</a>
    </div>
    <ul id="searchLocations">
    </ul>
    <div>
      <h4>Current Aliases</h4>
      <ul>
        <c:forEach var="alias" items="${aliases}" varStatus="aliasStatus">
          <li <c:if test="${aliasStatus.count > 10}">class="extraalias hidden"</c:if>><a href="/admin/locations/${alias.key}">${alias.name}</a></li>
        </c:forEach>
      </ul>
      <c:if test="${aliasCount > 10}">
        <a href="#" id="readmore">Read More...</a>
      </c:if>
    </div>
  </div>
</div>

<div class="row">
  <div class="col-md-12">
    <div class="">
      <div class="form-group">
        <label for="name">Name</label>
        <input id="name" class="form-control" placeholder="Location Name" type="text"/>
      </div>
    </div>
    <div class="form-inline">
      <div class="form-group">
        <label for="latitude">Latitude</label>
        <input id="latitude" class="form-control" type="text"/>
      </div>
      <div class="form-group">
        <label for="longitude">Longitude</label>
        <input id="longitude" class="form-control" type="text"/>
      </div>
    </div>
    <div>
      <div class="form-group">
        <label for="radius">Radius</label>
        <input id="radius" class="form-control" type="text"/>
      </div>
      <div class="form-group">
        <label for="radiateTo">Radiate Direction</label>
        <input id="radiateTo" class="form-control" type="text"/>
      </div>
      <div class="form-group">
        <label for="alias">Alias for</label>
        <input id="alias" class="form-control" type="text" data-provider="typeahead" data-items="4"/>
        <a href="#" id="viewAlias">View</a>
      </div>
      <div class="form-group">
        <label for="ownedBy">Owned By</label>
        <input class="form-control" id="ownedBy" type="text"/>
      </div>
      <div class="form-group">
        <label for="description">Description</label>
        <textarea class="form-control" id="description" rows="5" cols="80"></textarea>

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
        <input id="facebookUri" class="form-control" type="text"/>
      </div>
      <div class="form-group">
        <label for="imageUrl">Image Url</label>
        <input id="imageUrl" class="form-control" type="url"/>
      </div>
      <div class="form-group">
        <label for="eventUrl">Event Url</label>
        <input id="eventUrl" class="form-control" type="text"/>
      </div>
      <div class="form-group">
        <label for="email">Email</label>
        <input id="email" class="form-control" type="text"/>
      </div>
      <div class="form-group">
        <label for="managerEmails">Manager Emails (comma separated)</label>
        <input id="managerEmails" class="form-control" type="text"/>
      </div>
      <div class="form-group">
        <label for="phone" class="control-label">Phone</label>
        <input id="phone" class="form-control" type="tel"/>
      </div>
      <div class="checkbox">
        <label><input id="invalidLoc" type="checkbox">&nbsp;Ignore in geolocation lookups</label>
      </div>
      <div class="checkbox">
        <label><input id="designatedStop" type="checkbox">&nbsp;Designated food truck stop</label>
      </div>
      <div class="checkbox">
        <label><input id="popular" type="checkbox">&nbsp;Popular?</label>
      </div>
      <div class="checkbox">
        <label><input id="autocomplete" type="checkbox">&nbsp;Autocomplete?</label>
      </div>
      <div class="checkbox">
        <label><input id="hasBooze" type="checkbox">&nbsp;Serves Alcohol?</label>
      </div>
      <div class="checkbox">
        <label><input id="closed" type="checkbox">&nbsp;Closed?</label>
      </div>
      <div class="checkbox">
        <label><input id="alexaProvided" type="checkbox">&nbsp;Available to Alexa?</label>
      </div>
      <div class="btn-group>">
        <button id="submitButton" class="btn btn-primary"><span class="glyphicon glyphicon-save"></span> Save</button>
      </div>
    </div>
  </div>
</div>


<script type="text/javascript"
        src="//maps.googleapis.com/maps/api/js?key=${googleApiKey}">
</script>
<script type="text/javascript" src="/script/typeahead-addon.js"></script>
<script type="text/javascript" src="/script/location-edit.js"></script>
<script type="text/javascript">
  $(document).ready(function() {

    locationMatching(${locations}, "alias");

    locationEdit(${location});

    $("#readmore").click(function() {
      $("#readmore").addClass("hidden");
      $(".extraalias").removeClass("hidden");
    });
  });
</script>

<%@include file="dashboardFooterBS3.jsp" %>