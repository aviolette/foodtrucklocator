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
<button class="btn primary" id="addButton">New Event</button>
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
&nbsp;

<div id="edit-stop" class="modal hide fade">
  <div class="modal-header">
    <a href="#" class="close">&times;</a>

    <h3>Edit Stop</h3>
  </div>
  <div class="modal-body">
    <form>
      <fieldset>
        <div class="clearfix">
          <label for="startTimeInput">Start time</label>

          <div class="input">
            <input id="startTimeInput" type="text"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="endTimeInput">End time</label>

          <div class="input">
            <input id="endTimeInput" type="text"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="locationInput">Location</label>

          <div class="input">
            <input id="locationInput" type="text"/>
          </div>
        </div>
      </fieldset>
    </form>
  </div>
  <div class="modal-footer">
    <a id="saveButton" href="#" class="btn primary">Save</a>
    <a id="cancelButton" href="#" class="btn secondary">Cancel</a>
  </div>
</div>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
<script type="text/javascript" src="/bootstrap/js/bootstrap-modal.js"></script>
<script type="text/javascript">
  function invokeEditDialog(stop, afterwards) {
    $("#startTimeInput").attr("value", stop.startTime);
    $("#endTimeInput").attr("value", stop.endTime);
    $("#locationInput").attr("value", stop.location.name);
    $("#edit-stop").modal({ show: true, keyboard : true, backdrop: true});
    var $saveButton = $("#saveButton");
    $saveButton.unbind('click');
    $saveButton.click(function(e) {
      e.preventDefault();
      stop.startTime = $("#startTimeInput").attr("value");
      stop.endTime = $("#endTimeInput").attr("value");
      var locationName = $("#locationInput").attr("value");
      if (locationName != stop.location.name) {
        stop.locationName = locationName;
        delete stop.location;
      }
      stop.truckId = "${truckId}";
      $.ajax({
        url: "/admin/service/stop/" + stop.id,
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(stop),
        complete : function() {
          $("#edit-stop").modal('hide');
        },
        success: afterwards
      });
    });
    var $cancelButton = $("#cancelButton");
    $cancelButton.unbind("click");
    $cancelButton.click(function(e) {
      e.preventDefault();
      $("#edit-stop").modal('hide');
    });
  }
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
              "</td><td><a href='/admin/locations?q=" + encodeURIComponent(stop.location.name) +
              "'>"
              + stop.location.name + "</a></td><td><button  id='truckEndNow" + truckIndex +
              "' class='btn danger'>End Now</button>&nbsp;" +
              "<button id='truckDelete" + truckIndex +
              "' class='btn danger'>Delete</button>&nbsp;<button class='btn' id='truckEdit" +
              truckIndex + "'>Edit</button></td></tr>");
          $("#truckEdit" + truckIndex).click(function(e) {
            invokeEditDialog(stop, refreshSchedule);
          });
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
  $("#addButton").click(function(e) {
    invokeEditDialog({truckId : "${truckId}", locationName : "", location : { name : ""}, startTime : "", endTime : ""},
        refreshSchedule);
  });
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
