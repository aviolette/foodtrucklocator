<%@include file="dashboardHeader.jsp" %>
<h2>Schedule</h2>
<table>
  <thead>
  <tr>
    <td>Start Time</td>
    <td>End Time</td>
    <td>Location</td>
    <td>&nbsp;</td>
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
<button class="btn primary" id="recacheButton">Recache</button>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
<script type="text/javascript">
  var schedule = ${schedule};
  function refreshSchedule() {
    var scheduleTable = $("#scheduleTable");
    scheduleTable.empty();
    $.each(schedule["stops"], function(truckIndex, stop) {
      scheduleTable.append("<tr><td>" + stop.startTime + "</td><td>" + stop.endTime + "</td><td>"
          + stop.location.name +"</td><td><button class='btn danger'>End Now</button>&nbsp;" +
          "<button id='truckDelete" + truckIndex + "' class='btn danger'>Delete</button></td></tr>");
      $("#truckDelete" + truckIndex).click(function(e) {
        e.preventDefault();
        alert(stop.id);
      });
    })
  }
  refreshSchedule();
  $("#recacheButton").click(function(evt) {
    $.ajax({
      url: "/cron/recache?truck=${truckId}",
      context: document.body,
      dataType: 'json',
      success: function(data) {
        refreshSchedule();
      }});
  });
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
