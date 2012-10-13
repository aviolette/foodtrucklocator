<%@include file="dashboardHeader.jsp" %>

<img src="${truck.iconUrl}"/>

<h2>Configuration
  <small>(<a href="/admin/trucks/${truckId}/configuration">edit</a>)</small>
</h2>

<table class="table">
  <tr>
    <td>Facebook</td>
    <td><c:choose><c:when test="${empty(truck.facebook)}">none</c:when><c:otherwise><a
        target="_blank"
        href="http://facebook.com${truck.facebook}">http://facebook.com${truck.facebook}</a></c:otherwise></c:choose>
    </td>
  </tr>
  <tr>
    <td>Foursquare</td>
    <td><c:choose><c:when test="${empty(truck.foursquareUrl)}">none</c:when><c:otherwise><a
        target="_blank" href="http://foursquare.com/v/${truck.foursquareUrl}">http://foursquare.com/v/${truck.foursquareUrl}</a></c:otherwise></c:choose>
    </td>
  </tr>
  <tr>
    <td>Twitter</td>
    <td><c:choose><c:when test="${empty(truck.twitterHandle)}">none</c:when><c:otherwise><a
        target="_blank"
        href="http://twitter.com/${truck.twitterHandle}">${truck.twitterHandle}</a></c:otherwise></c:choose>
    </td>
  </tr>
  <tr>
    <td>Website</td>
    <td><c:choose><c:when test="${empty(truck.url)}">none</c:when><c:otherwise><a target="_blank"
                                                                                  href="${truck.url}">${truck.url}</a></c:otherwise></c:choose>
    </td>
  </tr>
  <tr>
    <td>Email</td>
    <td><c:choose><c:when test="${empty(truck.email)}">none</c:when><c:otherwise><a target="_blank"
                                                                                    href="mailto:${truck.email}">${truck.email}</a></c:otherwise></c:choose>
    </td>
  </tr>
  <tr>
    <td>Phone</td>
    <td><c:choose><c:when
        test="${empty(truck.phone)}">none</c:when><c:otherwise>${truck.phone}</c:otherwise></c:choose>
    </td>
  </tr>
</table>

<h2>Schedule</h2>
<table class="table table-striped">
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
&nbsp;
<button class="btn" id="recacheButton">Reload from calendar</button>

<h2>Weekly Overview</h2>

<table class="table table-striped">
  <thead>
  <tr>
    <th>Day</th>
    <th>This Week</th>
    <th>&nbsp;</th>
    <th>Last Week</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach items="${schedule}" var="day">
    <tr>
      <td>${day.name}</td>
      <td><c:if test="${!empty(day.current)}">
        <c:forEach items="${day.current.stops}" var="stop" varStatus="stopStatus">
          <c:url value="/admin/locations"
                 var="locationUrl">
            <c:param name="q" value="${stop.location.name}"/>
          </c:url> <a
            href="${locationUrl}">${stop.location.name}</a>&nbsp;<c:if
            test="${!stopStatus.last}"><br/></c:if>
        </c:forEach>
      </c:if>&nbsp;</td>
      <td>
        <c:choose>
        <c:when test="${!empty(day.prior)}">
        <c:forEach items="${day.prior.stops}" var="stop" varStatus="stopStatus">
          <joda:format value="${stop.startTime}" style="-S"/> -
          <joda:format value="${stop.endTime}" style="-S"/><c:if
            test="${!stopStatus.last}"><br/></c:if>
        </c:forEach></td>
      <td>
        <c:forEach items="${day.prior.stops}" var="stop" varStatus="stopStatus">
        <c:url value="/admin/locations"
               var="locationUrl">
          <c:param name="q" value="${stop.location.name}"/>
        </c:url> <a
          href="${locationUrl}">${stop.location.name}</a>&nbsp;<c:if
          test="${!stopStatus.last}"><br/></c:if>
        </c:forEach>
        </c:when>
        <c:otherwise>
      <td>&nbsp;</td>
      <td>&nbsp;</c:otherwise>
      </c:choose></td>
    </tr>
  </c:forEach>


  </tbody>
</table>
<h2>Tweets</h2>
<table class="table table-striped">
  <thead>
  <tr>
    <td style="width: 100px">Time</td>
    <td>Text</td>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="tweet" items="${tweets}">
    <tr>
      <td style="width:100px !important"><joda:format value="${tweet.time}" style="-S"/></td>
      <td>${tweet.text}</td>
    </tr>
  </c:forEach>
  </tbody>
</table>

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
        <div class="clearfix">
          <div class="input">
            <input type="checkbox" id="lockStop" name="lockStop"/>&nbsp;<label style="float:none"
                                                                               for="lockStop">Prevent
            twittalyzer from changing location</label>
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
<script type="text/javascript">
  function invokeEditDialog(stop, afterwards) {
    $("#startTimeInput").attr("value", stop.startTime);
    $("#endTimeInput").attr("value", stop.endTime);
    $("#locationInput").attr("value", stop.location.name);
    $("#lockStop").attr("checked", stop.locked);
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
      stop.locked = $("#lockStop").is(":checked");
      $.ajax({
        url: "/services/stops",
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
        var now = new Date().getTime();
        var numStops = schedule["stops"].length;
        var prevHadStart = false;
        $.each(schedule["stops"], function(truckIndex, stop) {
          var lockedString = (stop.locked) ? "&nbsp;<span class=\"label important\">locked</span>" :
              "";
          var buf = "<tr><td>" + stop.startTime + "</td><td>" + stop.endTime +
              "</td><td><a href='/admin/locations?q=" + encodeURIComponent(stop.location.name) +
              "'>"
              + stop.location.name + "</a>" + lockedString + "</td><td>";
          if (!prevHadStart && now < stop.startTimeMillis) {
            prevHadStart = true;
            buf = buf + "<button  id='truckStartNow" + truckIndex +
                "' class='btn success'>Start Now</button>"
          } else if (now >= stop.startTimeMillis && now < stop.endTimeMillis) {
            buf = buf + "<button  id='truckEndNow" + truckIndex +
                "' class='btn warning'>End Now</button>";
          }
          buf += "&nbsp;</td><td>";
          scheduleTable.append(buf +
              "<button id='truckDelete" + truckIndex +
              "' class='btn danger'>Delete</button>&nbsp;<button class='btn' id='truckEdit" +
              truckIndex + "'>Edit</button></td></tr>");
          $("#truckEdit" + truckIndex).click(function(e) {
            invokeEditDialog(stop, refreshSchedule);
          });

          function timeUpdateMaker(useStart) {
            return function(e) {
              e.preventDefault();
              var now = new Date();
              var hour = now.getHours();
              var ampm = "AM";
              if (hour > 12) {
                ampm = "PM";
                hour = hour - 12;
              }
              var theTime = hour + ":" + now.getMinutes() + " " + ampm;
              if (useStart) {
                stop.startTime = theTime;
              } else {
                stop.endTime = theTime;
              }
              stop.truckId = "${truckId}";
              $.ajax({
                url: "/services/stops",
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify(stop),
                success: refreshSchedule
              });
            }
          }

          $("#truckStartNow" + truckIndex).click(timeUpdateMaker(true));
          $("#truckEndNow" + truckIndex).click(timeUpdateMaker(false));
          $("#truckDelete" + truckIndex).click(function(e) {
            e.preventDefault();
            $.ajax({
              url: "/services/stops/" + stop.id,
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
</script>
<%@include file="dashboardFooter.jsp" %>
