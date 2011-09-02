var Truck = function(opts) {
  var self = this;
  var options = opts;

  function buildIconUrl(letter) {
    return "http://www.google.com/mapfiles/marker" + letter + ".png"
  }

  self.buildMenuItem = function (menuSection, letter, letterUsed) {
    menuSection.append("<div class='menuSection' id='" + options.id + "'/>");
    var section = $('#' + options.id)
    var markerText = "&nbsp;";
    if (!letterUsed) {
      markerText = "<img src='" + buildIconUrl(letter) + "'/>";
    }
    section.append("<div class='markerSection'>" + markerText + "</div>");
    section.append("<div class='iconSection'> <img src='" + options.iconUrl + "'/></div>")
    section.append("<div class='menuContent' id='" + options.id +
        "Section' class='contentSection'></div>");
    var div = $('#' + options.id + 'Section');
    div.append("<a class='activationLink' href='#'>" + options.name + "</a><br/> ")
    if (options.locationName) {
      div.append("<span class='locationName'>" + options.locationName + "</span></br>");
    }
    if (options.url) {
      div.append("Website: <a target='_blank' href='" + options.url + "'>" + options.url +
          "</a><br/>")
    }
    if (options.twitter) {
      div.append("Twitter: <a target='_blank' href='http://twitter.com/" + options.twitter + "'>@" +
          options.twitter + "</a><br/>")
    }
  },
      self.buildMarker = function (map, letter) {
        var marker = new google.maps.Marker({
          map: map,
          icon: buildIconUrl(letter),
          position: options.latLng
        });
        var contentString = '<div id="content">' +
            '<img src="' + options.iconUrl + '"/>&nbsp;' + options.name

        '</div>';
        var infowindow = new google.maps.InfoWindow({
          content: contentString
        });
        google.maps.event.addListener(marker, 'click', function() {
          infowindow.open(map, marker);
        });
      };
};
