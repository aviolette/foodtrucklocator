(function () {

  function locationEdit(loc, vendor, saveCallback, errorCallback) {
    var MILES_TO_METERS = 1609.34, locationName = loc.name;
    // stupid
    var useVendorEndpoints = vendor ? true : false;
    $("#viewAlias").click(function (e) {
      e.preventDefault();
      location.href = '/admin/locations?q=' + encodeURIComponent($("#alias").val());
    });

    $("#viewUrl").click(function (e) {
      e.preventDefault();
      window.open($("#url").val(), "_blank");
    });

    $("#viewTwitter").click(function (e) {
      e.preventDefault();
      window.open("http://twitter.com/" + $("#twitterHandle").val(), "_blank");
    });

    function loadLocation(loc) {
      if (typeof loc == "undefined") {
        return;
      }
      $("#latitude").val(loc.latitude);
      $("#longitude").val(loc.longitude);
      $("#twitterHandle").val(loc.twitterHandle);
      $("#description").val(loc.description);
      $("#hasBooze").attr("checked", loc.hasBooze);
      $("#radius").val(loc.radius);
      $("#radiateTo").val(loc.radiateTo);
      $("#name").val(loc.name);
      $("#alias").val(loc.alias);
      $("#invalidLoc").attr("checked", !loc.valid);
      $("#popular").attr("checked", loc.popular);
      $("#closed").attr("checked", loc.closed);
      $("#designatedStop").attr("checked", loc.designatedStop);
      $("#autocomplete").attr("checked", loc.autocomplete);
      $("#url").val(loc.url);
      $("#ownedBy").val(loc.ownedBy);
      $("#facebookUri").val(loc.facebookUri);
      $("#imageUrl").val(loc.imageUrl);
      $("#alexaProvided").attr("checked", loc.alexaProvided);
      $("#blacklisted").attr("checked", loc.blacklisted);
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

      google.maps.event.addListener(marker, 'dragend', function (evt) {
        $("#latitude").val(marker.position.lat());
        $("#longitude").val(marker.position.lng());
        circle.setCenter(marker.position);
      });

      var geocoder = new google.maps.Geocoder();
      var bounds = new google.maps.LatLngBounds();
      bounds.extend(markerLat);
      var $locationSearchButton = $("#locationSearchButton");
      $locationSearchButton.click(function () {

        var addr = prompt("Please enter an address or intersection", $("#name").val());
        if (!addr) {
          return;
        }
        if (!addr.match(/,/)) {
          addr = addr + ", Chicago, IL";
        }
        $("#markerMessage").removeClass("hidden");
        var $searchLocations = $("#searchLocations");
        $searchLocations.empty();
        geocoder.geocode({'address': addr}, function (results, status) {
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
                  +results[i].geometry.location.lng() + "' "
                  + " href='#'>" + results[i].formatted_address + "</a></li>";
              $searchLocations.append(buf);
              bounds.extend(results[i].geometry.location);
            }
            map.fitBounds(bounds);
            $("a.address").click(function (e) {
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
    $submitButton.click(function (e) {
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
      loc.blacklisted = $("#blacklisted").is(":checked");
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
      var endpoint = "/admin/locations/" + loc.key;
      if (useVendorEndpoints) {
        endpoint = "/vendor/locations/" + loc.key + "/edit";
        loc.name = locationName;
      }
      $.ajax({
        context: document.body,
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(loc),
        url: endpoint,
        complete: function () {
          $submitButton.removeClass("disabled");
        },
        error: function (jqXHR, textStatus, errorThrown) {
          if (errorCallback) {
            errorCallback(errorThrown);
          }
        },
        success: function () {
          if (circle) {
            circle.setRadius(loc.radius * MILES_TO_METERS);
          }
          if (saveCallback) {
            saveCallback();
          } else {
            flash("Successfully saved", "success");
          }
        }
      });
    });
  }

  $(document).ready(function () {
    locationMatching(JSON.parse($("#locations").text()), "alias");

    locationEdit(JSON.parse($("#location").text()));

    $("#readmore").click(function () {
      $("#readmore").addClass("d-none");
      $(".extraalias").removeClass("d-none");
    });
  });


})();


