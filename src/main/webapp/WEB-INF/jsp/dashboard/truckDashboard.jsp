<%@include file="dashboardHeaderBS3.jsp" %>

<h1>${truck.name}</h1>
<a href="/trucks/${truckId}">View Public Page</a>

<h2>Schedule</h2>
<table class="table table-striped">
  <thead>
  <tr>
    <td>Start Time</td>
    <td>End Time</td>
    <td>Duration</td>
    <td>Origin</td>
    <td>Location</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  </thead>
  <tbody id="scheduleTable">
  </tbody>
</table>
<div class="btn-group">
  <button class="btn btn-default" id="addButton"><span class="glyphicon glyphicon-calendar"></span>&nbsp;New Event</button>
  <button class="btn btn-default" id="offRoadButton"><span class="glyphicon glyphicon-trash"></span> Off the Road</button>
  <button class="btn btn-default" id="recacheButton"><span class="glyphicon glyphicon-refresh"></span>&nbsp;Reload from calendar</button>
</div>

<div class="row">
  <div class="col-md-6">
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
        <td>Yelp</td>
        <td><c:choose><c:when test="${empty(truck.yelpSlug)}">none</c:when><c:otherwise><a
            target="_blank"
            href="http://yelp.com/biz/${truck.yelpSlug}">http://yelp.com/biz/${truck.yelpSlug}</a></c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Foursquare</td>
        <td><c:choose><c:when test="${empty(truck.foursquareUrl)}">none</c:when><c:otherwise><a
            target="_blank"
            href="http://foursquare.com/v/${truck.foursquareUrl}">http://foursquare.com/v/${truck.foursquareUrl}</a></c:otherwise></c:choose>
        </td>
      </tr>
      <tr>
        <td>Instagram</td>
        <td><c:choose><c:when test="${empty(truck.instagramId)}">none</c:when><c:otherwise><a
            target="_blank"
            href="http://instagram.com/${truck.instagramId}">http://instagram.com/${truck.instagramId}</a></c:otherwise></c:choose>
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
      <tr>
        <td>Categories</td>
        <td><c:forEach items="${truck.categories}" var="category"><span
            class="label label-info">${category}</span>&nbsp;</c:forEach></td>
      </tr>
    </table>
  </div>
  <div class="col-md-6">
    <h2>Statistics</h2>
    <table class="table">
      <tr>
        <td>Last active</td>
        <td><joda:format value="${truck.stats.lastSeen}" style="MS"/> @ <ftl:location
            location="${truck.stats.whereLastSeen}"/></td>
      </tr>
      <tr>
        <td>Stops this year</td>
        <td>${truck.stats.stopsThisYear}</td>
      </tr>
      <tr>
        <td>Total stops</td>
        <td>${truck.stats.totalStops}</td>
      </tr>
    </table>
  </div>
</div>


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
    <td>Location</td>
    <td>&nbsp;</td>
    <td>Text</td>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="tweet" items="${tweets}">
    <tr>
      <td style="width:100px !important"><joda:format value="${tweet.time}" style="-S"/></td>
      <td><ftl:location location="${tweet.location}"/>&nbsp;</td>
      <td><a class="btn btn-default retweet-button" id="retweet-${tweet.id}" href="#"><span class="glyphicon glyphicon-retweet"></span> Retweet</a></td>
      <td><ftl:tweetformat>${tweet.text}</ftl:tweetformat></td>
    </tr>
  </c:forEach>
  </tbody>
</table>

&nbsp;

<div id="edit-stop" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h3>Edit Stop</h3>
      </div>
      <div class="modal-body">
        <form role="form" class="form-horizontal">
          <div class="form-group">
            <label class="control-label" for="startTimeInput">Start</label>
            <input class="timeentry form-control" id="startTimeInput" type="datetime-local"/>
          </div>
          <div class="form-group">
            <label class="control-label" for="endTimeInput">End</label>
            <input class="timeentry form-control" id="endTimeInput" type="datetime-local"/>
          </div>
          <div class="form-group">
            <label class="control-label" for="locationInput">Location</label>
            <input class="form-control" id="locationInput" type="text" data-provide="typeahead" data-items="4"/>
          </div>
          <div class="form-group">
            <label><input id="lockStop" name="lockStop" type="checkbox">&nbsp;Prevent
              twittalyzer from changing location</label>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <a id="cancelButton" href="#" class="btn btn-default">Cancel</a>
        <a id="saveButton" href="#" class="btn btn-primary">Save</a>
      </div>
    </div>
  </div>
</div>

<script type="text/javascript">
  var locations = ${locations}, lastStop;

  $("#edit-stop").keypress(function(e) {
    if (e.which == 13) {
      e.preventDefault();
      $("#saveButton").click();
    }
  });

  $(".retweet-button").click(function(e) {
    e.preventDefault();
    var id = $(e.target).attr('id').substring(8);
    var account = prompt("With what account?");
    if (account) {
      var payload = {
        tweetId : id,
        account: account
      };
      $.ajax({
        url: "/services/tweets/retweets",
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(payload),
        complete: function () {
        },
        success: function() {

        }
      });
    }
  });

  var substringMatcher = function(strs) {
    return function findMatches(q, cb) {
      var matches, substrRegex;

      // an array that will be populated with substring matches
      matches = [];

      // regex used to determine if a string contains the substring `q`
      substrRegex = new RegExp(q, 'i');

      // iterate through the pool of strings and for any string that
      // contains the substring `q`, add it to the `matches` array
      $.each(strs, function(i, str) {
        if (substrRegex.test(str)) {
          // the typeahead jQuery plugin expects suggestions to a
          // JavaScript object, refer to typeahead docs for more info
          matches.push({ value: str });
        }
      });

      cb(matches);
    };
  };

  $("#locationInput").typeahead({
    hint: true,
    highlight: true,
    minLength: 1
  },{ name: 'locations', displayKey: 'value', source: substringMatcher(locations)});

  $("#startTimeInput").change(function() {
    var $endTime = $("#endTimeInput");
    var startTimeVal = $("#startTimeInput").val();
    var end = new Date(new Date(startTimeVal).getTime() + 7200000 ).toISOString();
    $endTime.attr("value", end.substring(0, end.lastIndexOf(":")));
  });
  $("#edit-stop").on("shown", function() {
    $("#startTimeInput").focus();
  });
  function invokeEditDialog(stop, afterwards) {
    $("#startTimeInput").val(stop.startTimeH);
    $("#endTimeInput").val( stop.endTimeH);
    $("#locationInput").val(stop.location.name);
    $("#lockStop").val( stop.locked);
    $("#edit-stop").modal({ show: true, keyboard: true, backdrop: true});
    var $saveButton = $("#saveButton");
    $saveButton.unbind('click');
    $saveButton.click(function (e) {
      e.preventDefault();
      stop.startTime =  $("#startTimeInput").val();
      stop.endTime =  $("#endTimeInput").val();
      var locationName = $("#locationInput").val();
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
        complete: function () {
          $("#edit-stop").modal('hide');
        },
        success: afterwards
      });
    });
    var $cancelButton = $("#cancelButton");
    $cancelButton.unbind("click");
    $cancelButton.click(function (e) {
      e.preventDefault();
      $("#edit-stop").modal('hide');
    });
  }


  function pad(t) {
    t = String(t)
    if (t.length == 1) {
      return "0" + t;
    }
    return t;
  }

  function toDate(d) {
    return (d.getFullYear()) + "-" + pad(d.getMonth()+1) + "-" + pad(d.getDate()) + "T" + pad(d.getHours()) + ":" + pad(d.getMinutes());
  }

  function refreshSchedule() {
    var scheduleTable = $("#scheduleTable");
    scheduleTable.empty();
    lastStop = null;
    $.ajax({
      url: '/services/schedule/${truckId}',
      type: 'GET',
      dataType: 'json',
      success: function (schedule) {
        var now = new Date().getTime();
        var numStops = schedule["stops"].length;
        var prevHadStart = false;
        $.each(schedule["stops"], function (truckIndex, stop) {
          lastStop = stop;
          var labels = (stop.locked) ? "&nbsp;<span class=\"label important\">locked</span>" :
              "";
          var crazyDuration = stop.durationMillis < 0 || stop.durationMillis > 43200000;
          labels += (stop.fromBeacon) ? "&nbsp;<span class=\"label important\">beacon</span>" : "";
          var buf = "<tr " + (crazyDuration ? " class='error'" : "") + "><td>" + stop.startTime + "</td><td>" + stop.endTime +
              "</td><td>" + stop.duration + "</td><td>" + stop.origin + "</td><td><a href='/admin/locations?q=" + encodeURIComponent(stop.location.name) +
              "'>"
              + stop.location.name + "</a>" + labels + "</td><td>";
          if (!prevHadStart && now < stop.startTimeMillis) {
            prevHadStart = true;
            buf = buf + "<button class='btn btn-default' id='truckStartNow" + truckIndex +
                "' class='btn success'>Start Now</button>"
          } else if (now >= stop.startTimeMillis && now < stop.endTimeMillis) {
            buf = buf + "<button class='btn btn-default' id='truckEndNow" + truckIndex +
                "' class='btn warning'>End Now</button>";
          }
          buf += "&nbsp;</td><td>";
          scheduleTable.append(buf +
              "<div class='btn-group'><button class='btn btn-default' id='truckDelete" + truckIndex +
              "' class='btn '><span class='glyphicon glyphicon-remove'></span> Delete</button>&nbsp;<button class='btn btn-default' id='truckEdit" +
              truckIndex + "'><span class='glyphicon glyphicon-pencil'></span> Edit</button></div></td></tr>");
          $("#truckEdit" + truckIndex).click(function (e) {
            stop["startDate"] = toDate(new Date(stop["startTimeMillis"]));
            stop["endDate"] = toDate(new Date(stop["endTimeMillis"]));
            invokeEditDialog(stop, refreshSchedule);
          });

          function timeUpdateMaker(useStart) {
            return function (e) {
              e.preventDefault();
              if (useStart) {
                stop.startTime = toDate(new Date());
                stop["endTime"] = toDate(new Date(stop["endTimeMillis"]));
              } else {
                stop.startTime = toDate(new Date(stop["startTimeMillis"]));
                stop.endTime = toDate(new Date());
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
          $("#truckDelete" + truckIndex).click(function (e) {
            e.preventDefault();
            $.ajax({
              url: "/services/stops/" + stop.id,
              type: 'DELETE',
              complete: function () {
                refreshSchedule();
              }
            })
          });
        })
      }
    })
  }

  function numStops() {
    return $("#scheduleTable").children().length;
  }

  function hasCategory(category) {
    var categories = [<c:forEach var="category" varStatus="categoryIndex" items="${truck.categories}">"${category}"<c:if test="${!categoryIndex.last}">,</c:if></c:forEach>];
    return categories.indexOf(category) >= 0;
  }
  $("#addButton").click(function (e) {
    var now = new Date();
    if ((!hasCategory("Breakfast") && numStops() == 0) && now.getHours() < 10 ) {
      now.setHours(11);
      now.setMinutes(0);
    }
    if (numStops() > 0 && lastStop != null) {
      now = new Date(lastStop["endTimeMillis"] + 60000)
    }
    var minutes = now.getMinutes();
    if (minutes != 0) {
      minutes = Math.ceil(minutes / 15) * 15;
      if (minutes <= 45) {
        now.setMinutes(minutes);
      } else {
        now = new Date(now.getTime() + (60 * 60 * 1000));
        now.setMinutes(0);
      }
    }
    var today = toDate(now), later = toDate(new Date(now.getTime() + (2*60*60*1000)));
    invokeEditDialog({truckId: "${truckId}", locationName: "", location: { name: ""},
            startTimeH: today, endTimeH: later },
          refreshSchedule);
  });
  refreshSchedule();
  var $offTheRoadButton = $("#offRoadButton");
  $offTheRoadButton.click(function (evt) {
    $.ajax({
      url: "/admin/trucks/${truckId}/offtheroad",
      type: 'POST',
      context: document.body,
      dataType: 'json',
      complete: function (data) {
        refreshSchedule();
      }});
  });

  var $recacheButton = $("#recacheButton");
  $recacheButton.click(function (evt) {
    $recacheButton.empty();
    $recacheButton.append("Refreshing...")
    $.ajax({
      url: "/cron/recache?truck=${truckId}",
      context: document.body,
      dataType: 'json',
      complete: function () {
        $recacheButton.empty();
        $recacheButton.append("Refresh")
      },
      success: function (data) {
        refreshSchedule();
      }});
  });
</script>
<%@include file="dashboardFooterBS3.jsp" %>
