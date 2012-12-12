<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>
<script type="text/javascript"
        src="http://maps.googleapis.com/maps/api/js?sensor=false">
</script>

<div class="row">
  <div class="span8">
    <div id="map_canvas" style="width:450px; height:300px; padding-bottom:20px;"></div>
  </div>
  <div class="span6">
    <ul id="searchLocations">

    </ul>
  </div>
</div>

<div>
  <a id="locationSearchButton" href="#">Search for a location</a>
</div>

<form>
  <fieldset>
    <div class="clearfix">
      <label>Name</label>

      <div class="input">
        <input id="name" type="text"/>
      </div>
    </div>
    <div class="clearfix">
      <label>Latitude</label>

      <div class="input">
        <input id="latitude" type="text"/>
      </div>
    </div>
    <div class="clearfix">
      <label>Longitude</label>

      <div class="input">
        <input id="longitude" type="text"/>
      </div>
    </div>
    <div class="clearfix">
      <label>Radius</label>

      <div class="input">
        <input id="radius" type="text"/>
      </div>
    </div>
    <div class="clearfix">
      <label for="description">Description</label>

      <div class="input">
        <textarea id="description" rows="5" cols="80"></textarea>
      </div>
    </div>
    <div class="clearfix">
      <label for="url">URL</label>

      <div class="input">
        <input id="url" type="text"/>
      </div>
    </div>
    <div class="clearfix">
      <label>&nbsp;</label>

      <div class="input">
        <ul class="unstyled">
          <li><label><input id="invalidLoc" type="checkbox">&nbsp;Ignore in geolocation
            lookups</label></li>
        </ul>
      </div>
    </div>
    <div class="actions">
      <input id="submitButton" type="submit" class="btn primary" value="Save"/>&nbsp;
    </div>
  </fieldset>
</form>

<script type="text/javascript">
  $(document).ready(function() {

    var loc = ${location};

    function loadLocation(loc) {
      if (typeof loc == "undefined") {
        return;
      }
      $("#latitude").attr("value", loc.latitude);
      $("#longitude").attr("value", loc.longitude);
      $("#radius").attr("value", loc.radius);
      $("#name").attr("value", loc.name);
      $("#invalidLoc").attr("checked", !loc.valid);
      $("#description").attr("value", loc.description);
      $("#url").attr("value", loc.url);
    }

    loadLocation(loc);
    var $submitButton = $("#submitButton");

    var lat = loc.latitude, lng = loc.longitude;
    if (!loc.valid) {
      lat = 41.8807438;
      lng = -87.6293867;
    }

    var markerLat = new google.maps.LatLng(lat, lng);
    var myOptions = {
      center: markerLat,
      zoom: 14,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var map = new google.maps.Map(document.getElementById("map_canvas"),
        myOptions);

    var marker = new google.maps.Marker({
      draggable: true,
      position: markerLat,
      map: map
    });

    var circle = new google.maps.Circle({
      radius: loc.radius * 1609.34,
      center: markerLat,
      map: map
    });
    $submitButton.click(function(e) {
      loc.latitude = parseFloat($("#latitude").attr("value"));
      loc.longitude = parseFloat($("#longitude").attr("value"));
      loc.name = $("#name").attr("value");
      loc.radius = parseFloat($("#radius").attr("value"));
      loc.valid = !$("#invalidLoc").is(":checked");
      loc.description = $("#description").attr("value");
      loc.url = $("#url").attr("value");
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
          circle.setRadius(loc.radius);
          flash("Successfully saved", "success");
        }
      });
    });

    google.maps.event.addListener(marker, 'dragend', function(evt) {
      $("#latitude").attr("value", marker.position.lat());
      $("#longitude").attr("value", marker.position.lng());
    });

    var geocoder = new google.maps.Geocoder();
    var bounds = new google.maps.LatLngBounds();
    bounds.extend(markerLat);
    var $locationSearchButton = $("#locationSearchButton");
    $locationSearchButton.click(function() {

      var addr = prompt("Please enter an address or intersection", null);
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
            var lat = $(e.target).attr("lat"),
                lng = $(e.target).attr("lng");
            var newPos = new google.maps.LatLng(parseFloat(lat),
                parseFloat(lng));
            $("#latitude").attr("value", lat);
            $("#longitude").attr("value", lng);
            marker.setPosition(newPos);

          });
        } else {
          alert("Unable to geocode your address");
        }
      });

    });
  });
</script>

<%@include file="dashboardFooter.jsp" %>