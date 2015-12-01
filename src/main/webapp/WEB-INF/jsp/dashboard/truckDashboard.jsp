<%@include file="dashboardHeaderBS3.jsp" %>
<style type="text/css">
</style>
<h1>${truck.name}</h1>
<a href="/trucks/${truckId}">View Public Page</a>

<%@include file="../include/truck_schedule_widget.jsp"%>

<div class="row">
  <div class="col-md-6">
    <h2>Configuration
      <small>(<a href="/admin/trucks/${truckId}/configuration">edit</a>)</small>
    </h2>

    <table class="table">
      <tr>
        <td>Facebook</td>
        <td><c:choose><c:when test="${empty(truck.facebook)}">none</c:when><c:otherwise><a
            target="_blank"
            href="http://facebook.com${truck.facebook}">http://facebook.com${truck.facebook}</a></c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Yelp</td>
        <td><c:choose><c:when test="${empty(truck.yelpSlug)}">none</c:when><c:otherwise><a
            target="_blank"
            href="http://yelp.com/biz/${truck.yelpSlug}">http://yelp.com/biz/${truck.yelpSlug}</a></c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Foursquare</td>
        <td><c:choose><c:when test="${empty(truck.foursquareUrl)}">none</c:when><c:otherwise><a
            target="_blank"
            href="http://foursquare.com/v/${truck.foursquareUrl}">http://foursquare.com/v/${truck.foursquareUrl}</a></c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Instagram</td>
        <td><c:choose><c:when test="${empty(truck.instagramId)}">none</c:when><c:otherwise><a
            target="_blank"
            href="http://instagram.com/${truck.instagramId}">http://instagram.com/${truck.instagramId}</a></c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Twitter</td>
        <td><c:choose><c:when test="${empty(truck.twitterHandle)}">none</c:when><c:otherwise><a
            target="_blank"
            href="http://twitter.com/${truck.twitterHandle}">${truck.twitterHandle}</a></c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Website</td>
        <td><c:choose><c:when test="${empty(truck.url)}">none</c:when><c:otherwise><a target="_blank"
                                                                                      href="${truck.url}">${truck.url}</a></c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Email</td>
        <td><c:choose><c:when test="${empty(truck.email)}">none</c:when><c:otherwise><a target="_blank"
                                                                                        href="mailto:${truck.email}">${truck.email}</a></c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Phone</td>
        <td><c:choose><c:when
            test="${empty(truck.phone)}">none</c:when><c:otherwise>${truck.phone}</c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Categories</td>
        <td><c:forEach items="${truck.categories}" var="category"><span
            class="label label-info">${category}</span>&nbsp;</c:forEach></td>
      </tr>
    </table>
  </div>
  <div class="col-md-6">
    <h2>Statistics</h2>
    <table class="table">
      <tr>
        <td>Last active</td>
        <td><joda:format value="${truck.stats.lastSeen}" style="MS"/> @ <ftl:location
            location="${truck.stats.whereLastSeen}"/></td>
      </tr>
      <tr>
        <td>Stops this year</td>
        <td>${truck.stats.stopsThisYear}</td>
      </tr>
      <tr>
        <td>Total stops</td>
        <td>${truck.stats.totalStops}</td>
      </tr>
    </table>
    <h2>Images</h2>
    <table class="table">
      <tr>
        <td>Icon Image</td>
        <td><c:choose><c:when test="${empty(truck.iconUrl)}">not set</c:when><c:otherwise><a href="${truck.iconUrl}"><img src="${truck.iconUrl}" width="48" height="48"/></a></c:otherwise></c:choose></td>
      </tr>
      <tr>
        <td>Preview Image</td>
        <td><c:choose><c:when test="${empty(truck.previewIcon)}">not set</c:when><c:otherwise><a href="${truck.previewIcon}"><img src="${truck.previewIcon}" width="150" height="150"/></a></c:otherwise></c:choose></td>
      </tr>
      <tr>
        <td>Banner Image</td>
        <td><c:choose><c:when test="${empty(truck.backgroundImage)}">not set</c:when><c:otherwise><a href="${truck.backgroundImage}"><img src="${truck.backgroundImage}" width="320" height="160"/></a></c:otherwise></c:choose></td>
      </tr>
      <tr>
        <td>Large Banner Image</td>
        <td><c:choose><c:when test="${empty(truck.backgroundImageLarge)}">not set</c:when><c:otherwise><a href="${truck.backgroundImageLarge}">${fn:substring(truck.backgroundImage, 0, 47)}...</a></c:otherwise></c:choose></td>
      </tr>
    </table>
  </div>
</div>

<h2>Stories</h2>
<table class="table table-striped">
  <thead>
  <tr>
    <td style="width: 100px">Time</td>
    <td>Location</td>
    <td>Source</td>
    <td>&nbsp;</td>
    <td>Text</td>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="tweet" items="${tweets}">
    <tr>
      <td style="width:100px !important"><a target="_blank" href="http://twitter.com/${tweet.screenName}/status/${tweet.id}"><joda:format value="${tweet.time}" style="-S"/></a></td>
      <td><ftl:location location="${tweet.location}"/>&nbsp;</td>
      <td>${tweet.storyType}</td>
      <td><a class="btn btn-default retweet-button" id="retweet-${tweet.id}" href="#"><span class="glyphicon glyphicon-retweet"></span> Retweet</a></td>
      <td><ftl:tweetformat>${tweet.text}</ftl:tweetformat></td>
    </tr>
  </c:forEach>
  </tbody>
</table>

&nbsp;



<script src="/script/truck_edit_widget.js"></script>

<script type="text/javascript">

  runEditWidget("${truckId}", ${locations},[<c:forEach var="category" varStatus="categoryIndex" items="${truck.categories}">"${category}"<c:if test="${!categoryIndex.last}">,</c:if></c:forEach>], {hasCalendar : ${not empty(truck.calendarUrl)}});
  $(".retweet-button").click(function(e) {
    e.preventDefault();
    var id = $(e.target).attr('id').substring(8);
    var account = prompt("With what account?");
    if (account) {
      var payload = {
        tweetId : id,
        account: account
      };
      $.ajax({
        url: "/services/tweets/retweets",
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(payload),
        complete: function () {
        },
        success: function() {

        }
      });
    }
  });

</script>
<%@include file="dashboardFooterBS3.jsp" %>
