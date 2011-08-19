function buildMarker(iconUrl, map, lat, lng, name) {
  var latLng = new google.maps.LatLng(lat, lng);
  var marker = new google.maps.Marker({
    map: map,
    position: latLng
  });
  var contentString = '<div id="content">' +
      '<img src="' + iconUrl + '"/>&nbsp;' + name

  '</div>';
  var infowindow = new google.maps.InfoWindow({
    content: contentString
  });
  google.maps.event.addListener(marker, 'click', function() {
    infowindow.open(map, marker);
  });
}
