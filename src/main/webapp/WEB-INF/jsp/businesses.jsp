<%@ include file="header.jsp" %>

<p class="lead">These are businesses owned by past and present food truck owners.</p>
<div class="row">
<div class="col-md-12">
    <div id="map_wrapper">
        <div class="section" style="min-height:400px" id="map_canvas"></div>
    </div>
</div>
</div>

<div class="row">
<ol>
<c:forEach var="venue" items="${locations}">
    <li style="padding-bottom:20px"><div><a href="/locations/${venue.location.key}">${venue.location.name}</a></div>
        <div>Associated with truck <a href="/trucks/${venue.truck.id}">${venue.truck.name}</a></div>
        <div>${venue.location.description}</div>
        <c:if test="${!empty(venue.location.url)}">
            <div><a href='${venue.location.url}'>${venue.location.url}</a></div>
        </c:if>
        <c:if test="${!empty(venue.location.twitterHandle)}">
            <div><a href="http://twitter.com/${location.location.twitterHandle}">@${venue.location.twitterHandle}</a></div>
        </c:if>
    </li>
</c:forEach>
</ol>
</div>

<%@include file="include/core_js.jsp" %>

<script type="text/javascript"
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>

<script type="text/javascript">
    (function() {
        var map = new google.maps.Map(document.getElementById("map_canvas"), {
            zoom: 11,
            center: new google.maps.LatLng(41.880187, -87.63083499999999),
            mapTypeId: google.maps.MapTypeId.ROADMAP
        });

        function buildInfoWindow(map, marker, tnl) {
            var contentString = "<div><a href='/locations/" + tnl.locationId + "'>" + tnl.locationName + "</a></div>";
            contentString += "<div>Associated with truck: <a href='/trucks/" + tnl.truckId + "'>" + tnl.truckName + "</a></div>";
            contentString += "<p>" + tnl.description + "</p>";
            var infoWindow = new google.maps.InfoWindow({
                content: contentString
            });
            google.maps.event.addListener(marker, 'click', function () {
                infoWindow.open(map, marker);
            });
        }
        var position;
<c:forEach var="venue" items="${locations}">
        position = new google.maps.LatLng(${venue.location.latitude}, ${venue.location.longitude});
        buildInfoWindow(map, new google.maps.Marker({
            map: map,
            icon: "http://www.google.com/mapfiles/marker_green.png",
            position: position
        }),  {locationId: "${venue.location.key}",
            locationName: "${venue.location.name}", truckId: "${venue.truck.id}",
            truckName: "${venue.truck.name}", description: "${venue.location.description}"});
</c:forEach>
})();
</script>
<%@ include file="footer.jsp" %>
