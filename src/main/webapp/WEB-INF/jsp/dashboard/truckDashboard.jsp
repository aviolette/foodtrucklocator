<%@include file="dashboardHeader.jsp" %>
<h2>Schedule</h2>
<table>
  <thead>
  <tr>
    <td>Start Time</td>
    <td>End Time</td>
    <td>Location</td>
  </tr>
  </thead>
  <tbody id="scheduleTable">
  </tbody>
</table>
<h2>Tweets</h2>
<table>
  <thead>
  <tr>
    <td>&nbsp;</td>
    <td>Time</td>
    <td>Text</td>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="tweet" items="${tweets}">
  <tr>
    <td><input type="button" class="ignoreButton btn primary" id="${tweet.id}"
               value="<c:choose><c:when test="${tweet.ignoreInTwittalyzer}">Unignore</c:when><c:otherwise>Ignore</c:otherwise></c:choose>"/>
    </td>
    <td>${tweet.time}</td>
    <td>${tweet.text}</td>
  </tr>
  </c:forEach>
  </tbody>
</table>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
<script type="text/javascript">
  var schedule = ${schedule};
  function refreshSchedule() {
    var scheduleTable = $("#scheduleTable");
    scheduleTable.empty();
    $.each(schedule["stops"], function(truckIndex, stop) {
      scheduleTable.append("<tr><td>" + stop.startTime + "</td><td>" + stop.endTime + "</td><td>" + stop.location.name +"</td></tr>")
    })
  }
  refreshSchedule();
  $(".ignoreButton").click(function(evt) {
    var id = $(this).attr("id");
    var value = $(this).attr("value");
    var ignore = value == "Ignore";
    var button = $(this);
    $.ajax({
      context: document.body,
      data: JSON.stringify({id: id, ignore: ignore}),
      contentType: 'application/json',
      dataType: 'json',
      type: 'POST',
      success: function() {
        button.attr("value", ignore ? "Unignore" : "Ignore")
      }
    });
  });
</script>
<%@include file="dashboardFooter.jsp" %>
