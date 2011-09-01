var Truck = function(opts) {
  var self = this;
  var options = opts;

  function buildIconUrl(letter) {
    return "http://www.google.com/mapfiles/marker" + letter + ".png"
  }

  self.buildMenuItem = function (menuSection, letter) {
    menuSection.append("<div class='menuSection'><img src='" + buildIconUrl(letter) +
        "'/> <a href='#'>" + options.name + "</a> </div>");
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
