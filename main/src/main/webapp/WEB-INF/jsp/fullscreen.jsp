<%@ include file="header.jsp" %>

<div class="row" id="main_content">
    <div id="map_wrapper">
        <div class="section" id="map_canvas"></div>
    </div>
</div>
<c:if test="${not mobile}">
    <%@include file="include/truck_dialog.jsp" %>
</c:if>
<%@include file="include/core_js.jsp" %>
<script type="text/javascript"
        src="http://maps.google.com/maps/api/js?sensor=false&libraries=geometry"></script>
<script type="text/javascript" src="/script/foodtruckfinder-2.0.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
        FoodTruckLocator.run("${mode}", new google.maps.LatLng(${center.latitude}, ${center.longitude}),
                new Date(${requestTimeInMillis}), ${payload}, "${appKey}", "${defaultCity}");
    });
</script>
<%@ include file="footer.jsp" %>
