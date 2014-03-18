<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>
<script type="text/javascript"
        src="http://maps.googleapis.com/maps/api/js?sensor=false">
</script>

<div class="row" style="margin-bottom:20px">
  <div class="span8">
    <div id="map_canvas" style="width:100%; height:300px; padding-bottom:20px;"></div>
  </div>
  <div class="span4">
    <div>
      <a id="locationSearchButton" href="#" class="btn">Search for a location</a>
      <c:if test="${!empty(locationId)}"><a id="locationViewButton" href="/locations/${locationId}" class="btn">View</a> </c:if>
    </div>
    <ul id="searchLocations">

    </ul>
  </div>
</div>


<form class="form-horizontal">
  <fieldset>
    <div class="control-group">
      <label class="control-label" for="name">Name</label>
      <div class="controls">
        <input id="name" class="span6" type="text"/>
      </div>
    </div>
    <div class="control-group">
      <label class="control-label" for="latitude">Lat / Long</label>
      <div class="controls">
        <input id="latitude" class="span2" type="text"/>
        <input id="longitude" type="text"/>
      </div>
    </div>
    <div class="control-group">
      <label class="control-label" for="radius">Radius</label>
      <div class="controls">
        <input id="radius" type="text"/>
      </div>
    </div>
    <div class="control-group">
      <label class="control-label" for="alias">Alias for</label>
      <div class="controls">
        <input id="alias" class="span6" type="text"/>
      </div>
    </div>
    <div class="control-group">
      <label for="description" class="control-label">Description</label>
      <div class="controls">
        <textarea class="span6" id="description" rows="5" cols="80"></textarea>
      </div>
    </div>
    <div class="control-group">
      <label for="url" class="control-label">URL</label>
      <div class="controls">
        <input id="url" class="span6" type="text"/>
      </div>
    </div>
    <div class="control-group">
      <label for="twitterHandle" class="control-label">Twitter Handle</label>
      <div class="controls">
        <input id="twitterHandle" class="span6" type="text"/>
      </div>
    </div>
    <div class="control-group">
      <div class="controls">
        <label><input id="invalidLoc" type="checkbox">&nbsp;Ignore in geolocation lookups</label>
        <label><input id="popular" type="checkbox">&nbsp;Popular?</label>
        <label><input id="autocomplete" type="checkbox">&nbsp;Autocomplete?</label>
        <input id="submitButton" type="submit" class="btn primary" value="Save"/>&nbsp;
      </div>
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
      $("#alias").attr("value", loc.alias);
      $("#twitterHandle").attr("value", loc.twitterHandle);
      $("#invalidLoc").attr("checked", !loc.valid);
      $("#description").attr("value", loc.description);
      $("#popular").attr("checked", loc.popular);
      $("#autocomplete").attr("checked", loc.autocomplete);
      $("#url").attr("value", loc.url);
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
        mapTypeId: google.maps.MapTypeId.ROADMAP
      };
      var map = new google.maps.Map(document.getElementById("map_canvas"),
          myOptions);

      var marker = new google.maps.Marker({
        draggable: true,
        position: markerLat,
        map: map
      });

      const MILES_TO_METERS = 1609.34;
      circle = new google.maps.Circle({
        radius: loc.radius * MILES_TO_METERS,
        center: markerLat,
        map: map
      });

      google.maps.event.addListener(marker, 'dragend', function(evt) {
        $("#latitude").attr("value", marker.position.lat());
        $("#longitude").attr("value", marker.position.lng());
        circle.setCenter(marker.position);
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
    }
    $submitButton.click(function(e) {
      loc.latitude = parseFloat($("#latitude").attr("value"));
      loc.longitude = parseFloat($("#longitude").attr("value"));
      loc.name = $("#name").attr("value");
      loc.alias = $("#alias").attr("value");
      loc.twitterHandle = $("#twitterHandle").attr("value");
      loc.radius = parseFloat($("#radius").attr("value"));
      loc.valid = !$("#invalidLoc").is(":checked");
      loc.description = $("#description").attr("value");
      loc.url = $("#url").attr("value");
      loc.popular = $("#popular").is(":checked");
      loc.autocomplete = $("#autocomplete").is(":checked");
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

<%@include file="dashboardFooter.jsp" %>