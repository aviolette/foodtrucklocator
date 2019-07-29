<%@include file="../dashboardHeader1.jsp" %>

<%@include file="truckNav.jsp" %>


<div class="row statcards mt-3 mb-3 text-xs-center text-md-left">

  <div class="statcard col-12 col-md-6">
    <h3 class="statcard-desc">First Active</h3>
    <div class="media">
      <img class="location-image mr-3" src="<c:choose><c:when test="${!empty(truck.stats.whereFirstSeen.imageUrl)}">${truck.stats.whereFirstSeen.imageUrl.protocolRelative}</c:when><c:otherwise>/img/no-image400x400.png</c:otherwise></c:choose>" width="150"  title="${truck.stats.whereFirstSeen.name}">
      <div class="media-body">
        <h3 class="statcard-number text-success"><joda:format value="${truck.stats.firstSeen}" style="MS"/> <br/> <ftl:location
            location="${truck.stats.whereFirstSeen}"/></h3>
      </div>
    </div>
  </div>

  <div class="statcard col-12 col-md-6">
    <h3 class="statcard-desc">Last Active</h3>
    <div class="media">
      <img class="location-image mr-3" src="<c:choose><c:when test="${!empty(truck.stats.whereLastSeen.imageUrl)}">${truck.stats.whereLastSeen.imageUrl.protocolRelative}</c:when><c:otherwise>/img/no-image400x400.png</c:otherwise></c:choose>" width="150"  title="${truck.stats.whereLastSeen.name}">
      <h3 class="statcard-number text-success"><joda:format value="${truck.stats.lastSeen}" style="MS"/> <br/> <ftl:location
          location="${truck.stats.whereLastSeen}"/></h3>

    </div>
  </div>

</div>

<%@include file="../dashboardFooter1.jsp" %>
