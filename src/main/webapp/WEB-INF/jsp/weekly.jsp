<%@ include file="nextgenheader.jsp" %>

<h1 class="text-center">The Week of <joda:format style="M-" value="${weekOf}"/></h1>

<div class="row-fluid">
  <div style="margin-top: 20px;" class="alert text-center span4 offset4">This schedule represents the current week's
    truck schedule for <strong>popular</strong>
    food truck spots.
  </div>
</div>

<div class="row-fluid">
  <div class="btn-toolbar span2 offset5 text-center">
    <div class="btn-group">
      <a class="btn <c:if test="${empty(prev)}">disabled</c:if>" href="/weekly-schedule?date=${prev}"><i
          class="icon-arrow-left"></i></a>
      <a class="btn" href="/weekly-schedule">Today</a>
      <a class="btn <c:if test="${empty(next)}">disabled</c:if>"
         href="<c:choose><c:when test="${empty(next)}">#</c:when><c:otherwise>/weekly-schedule?date=${next}</c:otherwise></c:choose>"><i
          class="icon-arrow-right"></i></a>
    </div>
  </div>
</div>

<table class="table table-bordered">
  <thead>
  <tr>
    <th>&nbsp;</th>
    <th>Sunday</th>
    <th>Monday</th>
    <th>Tuesday</th>
    <th>Wednesday</th>
    <th>Thursday</th>
    <th>Friday</th>
    <th>Saturday</th>
  </tr>
  </thead>
  <c:forEach var="row" varStatus="rowStatus" items="${weeklySchedule.stops}">
    <tr>
      <td style="width:13%">${row.location.name}</td>
      <c:forEach var="dayOfWeek" items="${row.stopsForWeek}">
        <td style="width:12%"><c:forEach var="stop" items="${dayOfWeek}">
          <img src="${stop.truck.iconUrl}" alt=""
               title="<joda:format pattern="hh:mm a" value="${stop.startTime}"/> - <joda:format pattern="hh:mm a" value="${stop.endTime}"/> ${stop.truck.name}"/>
        </c:forEach>&nbsp;</td>
      </c:forEach>
    </tr>
  </c:forEach>
</table>

<%@ include file="footer.jsp" %>