var Truck = function(opts) {
  var self = this;
  var options = opts;

  function buildIconUrl(letter) {
    return "http://www.google.com/mapfiles/marker" + letter + ".png"
  }

  self.buildMenuItem = function (menuSection, letter) {
    menuSection.append("<div class='menuSection' id='" + options.id + "'/>");
    var section = $('#' + options.id)
    section.append("<div class='iconSection'><img src='" + buildIconUrl(letter) + "'/> <img src='" +
        options.iconUrl + "'/></div>");
    section.append("<div class='menuContent' id='" + options.id +
        "Section' class='contentSection'></div>");
    var div = $('#' + options.id + 'Section');
    div.append("<a class='activationLink' href='#'>" + options.name + "</a><br/> ")
    if (options.url) {
      div.append("Website: <a href='" + options.url + "'>" + options.url + "</a><br/>")
    }
    if (options.twitter) {
      div.append("Twitter: <a href='http://twitter.com/#!/" + options.twitter + "'>@" +
          options.twitter + "</a> ")
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
