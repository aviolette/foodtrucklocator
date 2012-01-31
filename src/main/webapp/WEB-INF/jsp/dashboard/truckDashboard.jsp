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
  function refreshSchedule() {
    var scheduleTable = $("#scheduleTable");
    scheduleTable.empty();
    $.ajax({
      url: '/service/schedule/${truckId}',
      type: 'GET',
      dataType: 'json',
      success : function(schedule) {
        $.each(schedule["stops"], function(truckIndex, stop) {
          scheduleTable.append("<tr><td>" + stop.startTime + "</td><td>" + stop.endTime +
              "</td><td>"
              + stop.location.name + "</td><td><button  id='truckEndNow" + truckIndex +
              "' class='btn danger'>End Now</button>&nbsp;" +
              "<button id='truckDelete" + truckIndex +
              "' class='btn danger'>Delete</button></td></tr>");
          $("#truckEndNow" + truckIndex).click(function(e) {
            e.preventDefault();
            var now = new Date();
            var hour = now.getHours();
            var ampm = "AM";
            if (hour > 12) {
              ampm = "PM";
              hour = hour - 12;
            }
            stop.endTime = hour + ":" + now.getMinutes() + " " + ampm;
            stop.truckId = "${truckId}";
            $.ajax({
              url: "/admin/service/stop/" + stop.id,
              type: 'PUT',
              contentType: 'application/json',
              data: JSON.stringify(stop),
              success: refreshSchedule
            });
          });
          $("#truckDelete" + truckIndex).click(function(e) {
            e.preventDefault();
            $.ajax({
              url: "/admin/service/stop/" + stop.id,
              type: 'DELETE',
              complete: function() {
                refreshSchedule();
              }
            })
          });
        })
      }
    })
  }
  refreshSchedule();
  var $recacheButton = $("#recacheButton");
  $recacheButton.click(function(evt) {
    $recacheButton.empty();
    $recacheButton.append("Refreshing...")
    $.ajax({
      url: "/cron/recache?truck=${truckId}",
      context: document.body,
      dataType: 'json',
      complete: function() {
        $recacheButton.empty();
        $recacheButton.append("Refresh")
      },
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
