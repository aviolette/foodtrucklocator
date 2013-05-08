<%@ include file="nextgenheader.jsp" %>

<p>This schedule represents the current week's truck schedule for <strong>popular</strong> food truck spots.</p>

<table class="table">
  <thead>
    <tr><th>&nbsp;</th><th>Sunday</th><th>Monday</th><th>Tuesday</th><th>Wednesday</th><th>Thursday</th><th>Friday</th><th>Saturday</th></tr>
  </thead>
  <c:forEach var="row" varStatus="rowStatus" items="${weeklySchedule.stops}">
    <tr>
      <td>${row.location.name}</td>
      <c:forEach var="dayOfWeek" items="${row.stopsForWeek}">
        <td><c:forEach var="stop" items="${dayOfWeek}">
            <img src="${stop.truck.iconUrl}" alt="" title="<joda:format pattern="hh:mm a" value="${stop.startTime}"/> - <joda:format pattern="hh:mm a" value="${stop.endTime}"/> ${stop.truck.name}"/>
        </c:forEach></td>
      </c:forEach>
    </tr>
  </c:forEach>
</table>

<%@ include file="footer.jsp" %>