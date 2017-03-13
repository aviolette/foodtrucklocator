<%@include file="dashboardHeaderBS3.jsp" %>
<div id="app"></div>
<script src="/script/location.js"></script>
<ul class="list-unstyled">
  <c:forEach var="loc" items="${allLocations}">
    <li><ftl:location admin="true" location="${loc}"/></li>
  </c:forEach>
</ul>

<%@include file="dashboardFooterBS3.jsp" %>