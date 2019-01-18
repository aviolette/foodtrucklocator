<%@include file="dashboardHeader1.jsp" %>
<c:if test="${!empty(locationId)}">
  <div class="row">
    <div class="col-md-12">
      <h2 class="dashhead-title">Location</h2>
      <div class="btn-group">
        <a href="/admin/event_at/${locationId}" class="btn btn-outline-primary"><span
            class="glyphicon glyphicon-plus"></span>
          New Event</a>
      </div>
      <div class="btn-group">
        <a id="locationViewButton" href="/locations/${locationId}" class="btn btn-outline-secondary">View</a>
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
      <a id="locationSearchButton" href="#" class="btn btn-outline-secondary"><span class="glyphicon glyphicon-search"></span>
        Search for a location</a>
    </div>
    <ul id="searchLocations">
    </ul>
    <div>
      <h4>Current Aliases</h4>
      <ul>
        <c:forEach var="alias" items="${aliases}" varStatus="aliasStatus">
          <li <c:if test="${aliasStatus.count > 6}">class="extraalias d-none"</c:if>><a
              href="/admin/locations/${alias.key}">${alias.name}</a></li>
        </c:forEach>
      </ul>
      <c:if test="${aliasCount > 6}">
        <a href="#" id="readmore">Read More...</a>
      </c:if>
    </div>
  </div>
</div>

<div class="row">
  <div class="col-md-12">

    <ul class="nav nav-pills mb-3 mt-3">
      <li class="nav-item">
        <a class="nav-link active" id="basic-tab" data-toggle="tab" href="#basic" role="tab" aria-controls="basic"
           aria-selected="true">Basic</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" id="contact-tab" data-toggle="tab" href="#contact" role="tab" aria-controls="contact"
           aria-selected="false">Contact</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" id="image-tab" data-toggle="tab" href="#image" role="tab" aria-controls="image"
           aria-selected="false">Image</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" id="attributes-tab" data-toggle="tab" href="#attributes" role="tab"
           aria-controls="attributes" aria-selected="false">Attributes</a>
      </li>
    </ul>

    <div class="tab-content" id="control-tabs">
      <div class="tab-pane fade show active" id="basic" role="tabpanel" aria-labelledby="basic-tab">

        <div class="form-group">
          <label for="name">Name</label>
          <input id="name" class="form-control" placeholder="Location Name" type="text"/>
        </div>

        <div class="form-row">
          <div class="form-group col-md-6">
            <label for="latitude">Latitude</label>
            <input id="latitude" class="form-control" type="text"/>
          </div>
          <div class="form-group col-md-6">
            <label for="longitude">Longitude</label>
            <input id="longitude" class="form-control" type="text"/>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group col-md-6">
            <label for="radius">Radius</label>
            <input id="radius" class="form-control" type="text"/>
          </div>
          <div class="form-group col-md-6">
            <label for="radiateTo">Radiate Direction</label>
            <input id="radiateTo" class="form-control" type="text"/>
          </div>
        </div>

        <div class="form-group">
          <label for="description">Description</label>
          <textarea class="form-control" id="description" rows="5" cols="80"></textarea>
        </div>
      </div>

      <div class="tab-pane fade" id="contact" role="tabpanel" aria-labelledby="contact-tab">
        <div class="form-group">
          <label for="twitterHandle">Twitter Handle</label>
          <div class="input-group">
            <div class="input-group-prepend">
              <div class="input-group-text">@</div>
            </div>
            <input id="twitterHandle" class="form-control" type="text"/>
            <div class="input-group-append">
              <button id="viewTwitter" class="btn btn-default"><span
                  class="icon icon-forward"></span></button>
            </div>

          </div>
        </div>
        <div class="form-group">
          <label for="facebookUri">Facebook</label>
          <input id="facebookUri" class="form-control" type="text"/>
        </div>
        <div class="form-group">
          <label for="ownedBy">Owned By</label>
          <input class="form-control" id="ownedBy" type="text"/>
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
        <div class="form-group">
          <label for="eventUrl">Event Url</label>
          <input id="eventUrl" class="form-control" type="text"/>
        </div>

      </div>

      <div class="tab-pane fade" id="image" role="tabpanel" aria-labelledby="image-tab">
        <div class="form-group">
          <label for="imageUrl">Image Url</label>
          <input id="imageUrl" class="form-control" type="url"/>
        </div>
      </div>
      <div class="tab-pane fade" id="attributes" role="tabpanel" aria-labelledby="attributes-tab">
        <div class="form-group">
          <label for="alias">Alias for</label>
          <input id="alias" class="form-control" type="text" data-provider="typeahead" data-items="4"/>
          <a href="#" id="viewAlias">View</a>
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
        <div class="checkbox">
          <label><input id="blacklisted" type="checkbox">&nbsp;Blacklisted from Calendar Search?</label>
        </div>
      </div>
    </div>


    <div>
      <div class="btn-group>">
        <button id="submitButton" class="btn btn-outline-primary btn-lg">Save</button>
      </div>
    </div>
  </div>
</div>

<script type="application/json" id="locations">${locations}</script>
<script type="application/json" id="location">${location}</script>

<%@include file="dashboardFooter1.jsp" %>