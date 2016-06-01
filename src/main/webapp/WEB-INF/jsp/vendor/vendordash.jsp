<%@ include file="vendorheader.jsp" %>
<h1>${truck.name}</h1>
<div class="row">
  <div class="col-md-3">
    <img src="${truck.previewIcon}" width="180" height="180"/>
  </div>
  <div class="col-md-4">
    <a type="button" class="btn btn-default" aria-label="Edit" href="/vendor/settings/${truck.id}">
      <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
    </a>

    <dl>
      <dt>Description</dt>
      <dd><p class="lead">${truck.description}</p></dd>
      <dt>Website</dt>
      <dd><c:if test="${!empty(truck.url)}"><a href="${truck.url}">${truck.url}</a></c:if></dd>
      <dt>Phone</dt>
      <dd>${truck.phone}</dd>
      <dt>Email</dt>
      <dd>${truck.email}</dd>
    </dl>
  </div>
  <div class="col-md-4">
    <c:if test="${!empty(truck.facebook)}"><a target="_blank" href="http://facebook.com${truck.facebook}"><img
        alt="Facebook" src="http://storage.googleapis.com/ftf_static/img/facebook32x32.png"></a></c:if>
    <c:if test="${!empty(truck.twitterHandle)}"><a target="_blank"
                                                   href="http://twitter.com/${truck.twitterHandle}"><img
        alt="@${truck.twitterHandle} on twitter" src="http://storage.googleapis.com/ftf_static/img/twitter32x32.png"></a></c:if>
    <c:if test="${!empty(truck.foursquareUrl)}"><a target="_blank"
                                                   href="http://foursquare.com/venue/${truck.foursquareUrl}"><img
        alt="Check in on foursquare" src="http://storage.googleapis.com/ftf_static/img/foursquare32x32.png"></a></c:if>
    <c:if test="${!empty(truck.instagramId)}"><a target="_blank"
                                                 href="http://instagram.com/${truck.instagramId}"><img
        alt="View on instagram" src="http://storage.googleapis.com/ftf_static/img/instagram32x32.png"></a></c:if>
    <c:if test="${!empty(truck.yelpSlug)}"><a target="_blank"
                                              href="http://yelp.com/biz/${truck.yelpSlug}"><img alt="Yelp"
                                                                                                src="http://storage.googleapis.com/ftf_static/img/yelp32x32.png"></a></c:if>
  </div>
</div>
<div class="row">
  <div class="col-md-12">
    <%@ include file="../include/truck_schedule_widget.jsp" %>
  </div>
</div>
<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript" src="/script/truck_edit_widget.js"></script>
<script src="/script/lib/typeahead.bundle.js"></script>
<script type="text/javascript">
  runEditWidget("${truck.id}", ${locations}, ${categories}, {vendorEndpoints: true, hasCalendar: ${not empty(truck.calendarUrl)}});
</script>
<%@ include file="vendorfooter.jsp" %>
