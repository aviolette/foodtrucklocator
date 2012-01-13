<%@include file="dashboardHeader.jsp" %>
<h2>Schedule</h2>
<table>
  <tbody>
    <c:forEach var="stop" items="${schedule.stops}">
      <tr><td>${stop.startTime}</td><td>${stop.endTime}</td><td>${stop.location.name}</td></tr>
    </c:forEach>
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
