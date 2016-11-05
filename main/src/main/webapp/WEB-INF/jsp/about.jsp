<%@ include file="header.jsp" %>
<div class="row">
  <div class="col-md-12">

    <p class="lead">The ${title} is used to find food trucks in realtime on the streets of ${city}. Food trucks are put
      on the map by automatic aggregation of social media feeds, published food truck schedules, and from dedicated GPS
      devices in trucks. Not only does this website publish food truck locations, it also
      notifies you via regional twitter accounts and via browser notifications from Chrome.</p>

    <a name="notify"></a>
    <h2>How can I receive alerts for food trucks near me?</h2>

    <p>I am currently working on some new ways to accomplish notifications, but these are your best bets:</p>
    <h3>Chrome Extension</h3>
    <p>The <a
        href="https://chrome.google.com/webstore/detail/food-truck-finder-notifie/hapnieohgibnoaldifflafpcflcicdlc">Food
      Truck Finder Notifier</a> chrome extension is in the Chrome store. This puts an icon on your
      Chrome toolbar that indicates how many food trucks are nearby.</p>

    <h3>Location-specific Twitter Accounts</h3>
    <p>I have regional twitter accounts which retweets any truck that mentions a specific location, and sends out a
      daily lunch-time summary tweet. Below is a map that lists them all.</p>
    <div id="location_wrapper">
      <div class="section" style="min-height:400px" id="location_canvas"></div>
    </div>

    <a name="booking"></a>
    <h2>"I need food trucks for my event, can you help me?"</h2>

    <p>This is not something I can currently do effectively, so your best bet is to use to peruse <a href="/trucks">the
      selection of trucks on this website</a> and contact them individually.</p>

  </div>
</div>

<%@include file="include/core_js.jsp" %>
<script type="text/javascript"
        src="//maps.google.com/maps/api/js?key=${googleApiKey}&libraries=geometry"></script>

<script>
  (function () {
    var MILES_TO_METERS = 1609.34;

    var map = new google.maps.Map(document.getElementById("location_canvas"), {
      zoom: 11,
      center: new google.maps.LatLng(${accountCenter.latitude}, ${accountCenter.longitude}),
      scrollwheel: false,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    });

    function buildInfoWindow(marker, name, twitterHandle, locationId) {
      var contentString = "<h3><a href='http://twitter.com/" + twitterHandle + "'>@" + twitterHandle + "</a></h3>";
      contentString += "<div><a href='/locations/" + locationId + "'>" + name + "</a>";
      var infoWindow = new google.maps.InfoWindow({
        content: contentString
      });
      google.maps.event.addListener(marker, 'click', function () {
        infoWindow.open(map, marker);
      });
    }

    function drawItem(twitterHandle, name, lat, lng, radius, locationId) {
      var markerLat = new google.maps.LatLng(lat, lng);

      var marker = new google.maps.Marker({
        draggable: true,
        position: markerLat,
        map: map
      });

      circle = new google.maps.Circle({
        radius: radius * MILES_TO_METERS,
        center: markerLat,
        map: map
      });

      buildInfoWindow(marker, name, twitterHandle, locationId);

    }

    <c:forEach var="account" items="${accounts}">
    drawItem("${account.twitterHandle}", "${account.location.name}", ${account.location.latitude}, ${account.location.longitude}, ${account.location.radius}, ${account.location.key});
    </c:forEach>

  })();


</script>
<%@ include file="footer.jsp" %>
