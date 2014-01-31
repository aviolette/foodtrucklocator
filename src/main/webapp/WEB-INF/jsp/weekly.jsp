<%@ include file="header.jsp" %>

<h1 class="text-center">The Week of <joda:format style="M-" value="${weekOf}"/></h1>

<div class="row">
  <div style="margin-top: 20px;" class="alert text-center span4 offset4">This schedule represents the current week's
    truck schedule for <strong>popular</strong>
    food truck spots.
  </div>
</div>

<div class="row" style="padding-bottom:20px">
  <div class="col-md-5 col-md-offset-5 btn-toolbar ">
    <div class="btn-group">
      <button id="prevButton" type="button" class="btn btn-default <c:if test="${empty(prev)}">disabled</c:if>"><span
          class="glyphicon glyphicon-arrow-left"></span></button>
      <button id="todayButton" type="button" class="btn btn-default">Today</button>
      <button id="nextButton" type="button" class="btn btn-default <c:if test="${empty(next)}">disabled</c:if>"><i
          class="glyphicon glyphicon-arrow-right"></i></button>
    </div>
  </div>
</div>

<div class="row">
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
</div>
<%@include file="include/core_js.jsp" %>
<script type="text/javascript">
  $("#prevButton").click(function(e) {
    location.href = "/weekly-schedule?date=${prev}";
  });
  $("#nextButton").click(function(e) {
    <c:if test="${!empty(next)}">
      location.href="/weekly-schedule?date=${next}";
    </c:if>
  });
  $("#todayButton").click(function(e) {
    location.href= "/weekly-schedule";
  });
</script>
<%@ include file="footer.jsp" %>