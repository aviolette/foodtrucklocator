<%@include file="../dashboardHeader1.jsp" %>

<link rel="stylesheet" href="/css/truck_edit_widget.css">

<script type="application/json" id="truck-info">
  {"locations":  ${locations},
   "categories": [<c:forEach var="category" varStatus="categoryIndex" items="${truck.categories}">"${category}"<c:if test="${!categoryIndex.last}">, </c:if></c:forEach>],
   "truckId": "${truckId}"
  }
</script>

<%@include file="truckNav.jsp" %>

<%@include file="../../include/truck_schedule_widget.jsp" %>

<div class="mt-3">
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
</div>

<%@include file="../dashboardFooter1.jsp" %>
