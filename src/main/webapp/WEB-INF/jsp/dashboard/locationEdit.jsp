<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>
<script type="text/javascript"
        src="http://maps.googleapis.com/maps/api/js?sensor=false">
</script>

<div id="map_canvas" style="width:500px; height:300px; padding-bottom:20px;"></div>

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
      <label>&nbsp;</label>

      <div class="input">
        <ul class="inputs-list">
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
      $("#name").attr("value", loc.name);
      $("#invalidLoc").attr("checked", !loc.valid);
    }

    loadLocation(loc);
    var $submitButton = $("#submitButton");
    $submitButton.click(function(e) {
      loc.latitude = parseFloat($("#latitude").attr("value"));
      loc.longitude = parseFloat($("#longitude").attr("value"));
      loc.name = $("#name").attr("value");
      loc.valid = !$("#invalidLoc").is(":checked");
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
          flash("Successfully saved", "success");
        }
      });
    });

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
      geocoder.geocode({ 'address': addr }, function(results, status) {
        if (status == google.maps.GeocoderStatus.OK) {
          for (var i = 0; i < results.length; i++) {
            var marker = new google.maps.Marker({
              draggable: false,
              icon: 'http://maps.google.com/mapfiles/marker_green.png',
              position: results[i].geometry.location,
              map: map
            });
            bounds.extend(results[i].geometry.location);
          }
          map.fitBounds(bounds);
        } else {
          alert("Unable to geocode your address");
        }
      });

    });
  });
</script>

<%@include file="dashboardFooter.jsp" %>