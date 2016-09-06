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
        <c:forEach var="alias" items="${aliases}">
          <li><a href="/admin/locations/${alias.key}">${alias.name}</a></li>
        </c:forEach>
      </ul>
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
<script type="text/javascript">
  $(document).ready(function() {
    var MILES_TO_METERS = 1609.34;

    locationMatching(${locations}, "alias");

    $("#viewAlias").click(function(e) {
      e.preventDefault();
      location.href = '/admin/locations?q=' + encodeURIComponent($("#alias").val());
    });

    $("#viewUrl").click(function(e) {
      e.preventDefault();
      window.open($("#url").val(), "_blank");
    });

    $("#viewTwitter").click(function(e) {
      e.preventDefault();
      window.open("http://twitter.com/" + $("#twitterHandle").val(), "_blank");
    });

    var loc = ${location};

    function loadLocation(loc) {
      if (typeof loc == "undefined") {
        return;
      }
      $("#latitude").val(loc.latitude);
      $("#longitude").val(loc.longitude);
      $("#radius").val(loc.radius);
      $("#radiateTo").val(loc.radiateTo);
      $("#name").val(loc.name);
      $("#alias").val(loc.alias);
      $("#twitterHandle").val(loc.twitterHandle);
      $("#invalidLoc").attr("checked", !loc.valid);
      $("#description").val(loc.description);
      $("#popular").attr("checked", loc.popular);
      $("#hasBooze").attr("checked", loc.hasBooze);
      $("#closed").attr("checked", loc.closed);
      $("#designatedStop").attr("checked", loc.designatedStop);
      $("#autocomplete").attr("checked", loc.autocomplete);
      $("#url").val(loc.url);
      $("#ownedBy").val(loc.ownedBy);
      $("#facebookUri").val(loc.facebookUri);
      $("#imageUrl").val(loc.imageUrl);
      $("#alexaProvided").attr("checked", loc.alexaProvided);
      $("#eventUrl").val(loc.eventUrl);
      $("#managerEmails").val(loc.managerEmails);
      $("#email").val(loc.email);
      $("#phone").val(loc.phone);
    }

    loadLocation(loc);
    var $submitButton = $("#submitButton");

    var lat = loc.latitude, lng = loc.longitude;
    if (!loc.valid) {
      lat = 41.8807438;
      lng = -87.6293867;
    }
    var circle = null;
    if (!(typeof google == "undefined")) {
      var markerLat = new google.maps.LatLng(lat, lng);
      var myOptions = {
        center: markerLat,
        zoom: 14,
        scrollwheel: false,
        mapTypeId: google.maps.MapTypeId.ROADMAP
      };
      var map = new google.maps.Map(document.getElementById("map_canvas"),
          myOptions);

      var marker = new google.maps.Marker({
        draggable: true,
        position: markerLat,
        map: map
      });

      circle = new google.maps.Circle({
        radius: loc.radius * MILES_TO_METERS,
        center: markerLat,
        map: map
      });

      google.maps.event.addListener(marker, 'dragend', function(evt) {
        $("#latitude").val(marker.position.lat());
        $("#longitude").val(marker.position.lng());
        circle.setCenter(marker.position);
      });

      var geocoder = new google.maps.Geocoder();
      var bounds = new google.maps.LatLngBounds();
      bounds.extend(markerLat);
      var $locationSearchButton = $("#locationSearchButton");
      $locationSearchButton.click(function() {

        var addr = prompt("Please enter an address or intersection", $("#name").val());
        if (!addr) {
          return;
        }
        if (!addr.match(/,/)) {
          addr = addr + ", Chicago, IL";
        }
        var $searchLocations = $("#searchLocations");
        $searchLocations.empty();
        geocoder.geocode({ 'address': addr }, function(results, status) {
          if (status == google.maps.GeocoderStatus.OK) {
            for (var i = 0; i < results.length; i++) {
              var auxMarker = new google.maps.Marker({
                draggable: false,
                icon: 'http://maps.google.com/mapfiles/marker_green.png',
                position: results[i].geometry.location,
                map: map
              });
              var buf = "<li><a class='address' lat='"
                  + results[i].geometry.location.lat() + "' lng='" +
                  + results[i].geometry.location.lng() + "' "
                  + " href='#'>" + results[i].formatted_address + "</a></li>";
              $searchLocations.append(buf);
              bounds.extend(results[i].geometry.location);
            }
            map.fitBounds(bounds);
            $("a.address").click(function(e) {
              e.preventDefault();
              var target = e.target;
              $("#invalidLoc").prop('checked', false);
              var lat = $(e.target).attr("lat"),
                  lng = $(e.target).attr("lng");
              var newPos = new google.maps.LatLng(parseFloat(lat),
                  parseFloat(lng));
              $("#latitude").val(lat);
              $("#longitude").val(lng);
              marker.setPosition(newPos);

            });
          } else {
            alert("Unable to geocode your address");
          }
        });

      });
    }
    $submitButton.click(function(e) {
      loc.latitude = parseFloat($("#latitude").val());
      loc.longitude = parseFloat($("#longitude").val());
      loc.name = $("#name").val();
      loc.alias = $("#alias").val();
      loc.twitterHandle = $("#twitterHandle").val();
      loc.radius = parseFloat($("#radius").val());
      loc.valid = !$("#invalidLoc").is(":checked");
      loc.description = $("#description").val();
      loc.url = $("#url").val();
      loc.popular = $("#popular").is(":checked");
      loc.hasBooze = $("#hasBooze").is(":checked");
      loc.closed = $("#closed").is(":checked");
      loc.alexaProvided = $("#alexaProvided").is(":checked");
      loc.designatedStop = $("#designatedStop").is(":checked");
      loc.autocomplete = $("#autocomplete").is(":checked");
      loc.ownedBy = $("#ownedBy").val();
      loc.radiateTo = parseInt($("#radiateTo").val());
      loc.email = $("#email").val();
      loc.phone = $("#phone").val();
      loc.facebookUri = $("#facebookUri").val();
      loc.imageUrl = $("#imageUrl").val();
      loc.eventUrl = $("#eventUrl").val();
      loc.managerEmails = $("#managerEmails").val();
      e.preventDefault();
      $submitButton.addClass("disabled");
      $.ajax({
        context: document.body,
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(loc),
        url: "/admin/locations/" + loc.key,
        complete : function() {
          $submitButton.removeClass("disabled");
        },
        success: function() {
          if (circle) {
            circle.setRadius(loc.radius * MILES_TO_METERS);
          }
          flash("Successfully saved", "success");
        }
      });
    });
  });
</script>

<%@include file="dashboardFooterBS3.jsp" %>