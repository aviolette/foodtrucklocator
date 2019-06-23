<%@include file="../dashboardHeader1.jsp" %>

<%@include file="truckNav.jsp" %>


<div class="row statcards mt-3 mb-3 text-xs-center text-md-left">

  <div class="statcard col-12 col-md-3 mb-3">
    <h3 class="statcard-number text-success">${truck.stats.stopsThisYear}</h3>
    <span class="statcard-desc">Stops This Year</span>
  </div>

  <div class="statcard col-12 col-md-3 mb-3">
    <h3 class="statcard-number text-success">${truck.stats.totalStops}</h3>
    <span class="statcard-desc">Total Stops</span>
  </div>

  <div class="statcard col-12 col-md-3 mb-3">
    <h3 class="statcard-number text-success"><joda:format value="${truck.stats.firstSeen}" style="MS"/> @ <ftl:location
        location="${truck.stats.whereFirstSeen}"/></h3>
    <span class="statcard-desc">First Active</span>
  </div>

  <div class="statcard col-12 col-md-3 mb-3">
    <h3 class="statcard-number text-success"><joda:format value="${truck.stats.lastSeen}" style="MS"/> @ <ftl:location
        location="${truck.stats.whereLastSeen}"/></h3>
    <span class="statcard-desc">Last Active</span>
  </div>

</div>

<%@include file="../dashboardFooter1.jsp" %>
