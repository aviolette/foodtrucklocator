<%@ include file="vendorheader.jsp" %>

<h2>Today's Schedule for ${truck.name}</h2>
<div>
<c:choose>
  <c:when test="${hasStops}">
    <table>
      <tbody>
      <c:forEach var="schedItem" items="${schedule.stops}">
        <tr>
          <td style="padding-right:10px"><joda:format value="${schedItem.startTime}" pattern="hh:mm a"/> - <joda:format value="${schedItem.endTime}" pattern="hh:mm a"/></td>
          <td><ftl:location location="${schedItem.location}"/></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </c:when>
  <c:otherwise>
    <em>There are no stops scheduled.  You can add them by adding them to your calendar and resyncing it.</em>
  </c:otherwise>
</c:choose>
</div>
<br/>

<p><em>Press this button to sync your google calendar with the Chicago Food Truck Finder</em></p>
<button id="resyncCalendar" class="btn">Resync </button>
<p><em>Press this button to remove all the stops from the Chicago Food Truck Finder for today.</em></p>
<button id="offTheRoad" class="btn">Off The Road!</button>

<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript">
  (function() {
    $("#offTheRoad").click(function(e) {
      e.preventDefault();
      $.ajax({
        url: "/vendor/offtheroad/${truck.id}",
        type: 'post',
        context: document.body,
        dataType: 'json',
        complete: function () {
        },
        success: function (data) {
          location.reload();
        }});
    });
    $("#resyncCalendar").click(function(e) {
      e.preventDefault();
      $.ajax({
        url: "/vendor/recache/${truck.id}",
        type: 'post',
        context: document.body,
        dataType: 'json',
        complete: function () {
        },
        success: function (data) {
          location.reload();
        }});
    });
  })();
</script>

<%@ include file="vendorfooter.jsp" %>
