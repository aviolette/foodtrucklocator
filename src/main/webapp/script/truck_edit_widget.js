/**
 * Created by andrew on 3/28/15.
 */
runEditWidget = function(truckId, locations, categories, options) {
  var lastStop, $editStop = $("#edit-stop"), locationEndpoint = '/admin/locations';
  options = options || {};

  if (options["vendorEndpoints"]) {
    locationEndpoint = '/locations';
  }
  $editStop.keypress(function(e) {
    if (e.which == 13) {
      e.preventDefault();
      $("#saveButton").click();
    }
  });

  // Type-ahead related stuff
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


  $editStop.on("shown.bs.modal", function() {
    $("#startTimeInput").focus();
  });

  function invokeEditDialog(stop, afterwards) {
    $("#startTimeInput").val(stop.startTimeH);
    $("#endTimeInput").val( stop.endTimeH);
    $("#locationInput").val(stop.location.name);
    $("#lockStop").val( stop.locked);
    $("#locationInputGroup").removeClass("has-error");
    $("#truck-schedule-alert").addClass("hidden");
    $("#edit-stop").modal({ show: true, keyboard: true, backdrop: true});
    var $saveButton = $("#saveButton");
    $saveButton.unbind('click');
    $saveButton.click(function (e) {
      e.preventDefault();
      var $btn = $(this).button('loading')
      stop.startTime =  $("#startTimeInput").val();
      stop.endTime =  $("#endTimeInput").val();
      var locationName = $("#locationInput").val();
      var oldLocation = stop.location;
      if (locationName != stop.location.name) {
        stop.locationName = locationName;
        delete stop.location;
      }
      stop.truckId = truckId;
      stop.locked = $("#lockStop").is(":checked");
      if(locationName.length == 0) {
        $("#locationInputGroup").addClass("has-error");
        $("#truck-schedule-error").html("No location specified");
        $btn.button('reset')
      } else {
        $.ajax({
          url: "/services/v2/stops",
          type: 'PUT',
          contentType: 'application/json',
          data: JSON.stringify(stop),
          complete: function () {
          },
          error: function(e) {
            var obj = JSON.parse(e.responseText);
            $("#truck-schedule-error").html(obj.error);
            $("#truck-schedule-alert").removeClass("hidden");
            if (/location/i.exec(obj.error)) {
              $("#locationInputGroup").addClass("has-error");
            }
            if (/start time/i.exec(obj.error)) {
              $("#startTimeInputGroup").addClass("has-error");
            }
            if (/end time/i.exec(obj.error)) {
              $("#endTimeInputGroup").addClass("has-error");
            }
            stop.location = oldLocation;
            $btn.button('reset')
          },
          success: function() {
            $("#edit-stop").modal('hide');
            $btn.button('reset');
            afterwards();
          }
        });
      }
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
    var d = new Date();
    d.setHours(0);
    d.setMinutes(0);
    d.setSeconds(0);
    var tomorrow = d.getTime() + 86400000;

    $.ajax({
      url: '/services/v2/stops?truck=' + truckId,
      type: 'GET',
      dataType: 'json',
      success: function (schedule) {
        var now = new Date().getTime(), numStops = schedule.length;
        var prevHadStart = false;
        $.each(schedule, function (truckIndex, stop) {
          if (stop.startMillis < tomorrow) {
            lastStop = stop;
          }
          var labels = (stop.locked) ? "&nbsp;<span class=\"label important\">locked</span>" :
              "";
          var crazyDuration = stop.durationMillis < 0 || stop.durationMillis > 43200000;
          var showControls = stop.startMillis < tomorrow || stop.origin != 'VENDORCAL';;
          labels += (stop.fromBeacon) ? "&nbsp;<span class=\"label important\">beacon</span>" : "";
          var truckCountLink = stop.totalTruckCount < 2 ? "" : "<span class='badge truck-info-badge' data-toggle='popover' data-content='" + stop.truckNames +"'>" + stop.totalTruckCount + "</span>";
          var buf = "<tr " + (crazyDuration ? " class='error'" : "") + "><td>" + stop.startDate + "</td><td>" + stop.startTime + "</td><td>" + stop.endTime +
              "</td><td>" + stop.duration + "</td><td class=\"origin\">" + stop.origin + "</td><td><a href='" + locationEndpoint + "?q=" + encodeURIComponent(stop.location.name) +
              "'>"
              + stop.location.name + "</a>" + labels + "</td><td>" + truckCountLink +"</td><td>";
          if (showControls) {
            if (!prevHadStart && now < stop.startTimeMillis) {
              prevHadStart = true;
              buf = buf + "<button class='btn btn-default' id='truckStartNow" + truckIndex +
              "' class='btn success'>Start Now</button>"
            } else if (now >= stop.startTimeMillis && now < stop.endTimeMillis) {
              buf = buf + "<button class='btn btn-default' id='truckEndNow" + truckIndex +
              "' class='btn warning'>End Now</button>";
            }
          }
          buf += "&nbsp;</td><td>";
          if (showControls) {
            buf = buf + "<div class='btn-group'><button class='btn btn-default' id='truckDelete" + truckIndex +
            "' class='btn '><span class='glyphicon glyphicon-remove'></span> Delete</button>&nbsp;<button class='btn btn-default' id='truckEdit" +
            truckIndex + "'><span class='glyphicon glyphicon-pencil'></span> Edit</button></div></td></tr>";
          }
          scheduleTable.append(buf);
          $("#truckEdit" + truckIndex).click(function (e) {
            stop["startDate"] = toDate(new Date(stop["startMillis"]));
            stop["endDate"] = toDate(new Date(stop["endMillis"]));
            invokeEditDialog(stop, refreshSchedule);
          });

          function timeUpdateMaker(useStart) {
            return function (e) {
              e.preventDefault();
              if (useStart) {
                stop.startTime = toDate(new Date());
                stop["endTime"] = toDate(new Date(stop["endMillis"]));
              } else {
                stop.startTime = toDate(new Date(stop["startMillis"]));
                stop.endTime = toDate(new Date());
              }
              stop.truckId = truckId;
              $.ajax({
                url: "/services/v2/stops",
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
              url: "/services/v2/stops/" + stop.id,
              type: 'DELETE',
              complete: function () {
                refreshSchedule();
              }
            })
          });
        });
        $(function () {
          $('[data-toggle="popover"]').popover()
        });
      }
    });
  }

  function numStops() {
    return $("#scheduleTable").children().length;
  }

  function hasCategory(category) {
    return categories.indexOf(category) >= 0;
  }

  function newStop() {
    var now = new Date();
    if ((!hasCategory("Breakfast") && numStops() == 0) && now.getHours() < 10 ) {
      now.setHours(11);
      now.setMinutes(0);
    }
    if (numStops() > 0 && lastStop != null) {
      now = new Date(lastStop["endMillis"] + 60000)
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
    invokeEditDialog({truckId: truckId, locationName: "", location: { name: ""},
          startTimeH: today, endTimeH: later },
        refreshSchedule);
  }

  $("#addButton").click(function (e) {
    newStop();
  });

  $(document).keypress(function(e) {
    if (e.which == 110 && $editStop.css("display") == 'none') {
      e.preventDefault();
      newStop();
    }
  });

  refreshSchedule();
  var $offTheRoadButton = $("#offRoadButton");
  if (options["vendorEndpoints"]) {
    $offTheRoadButton.css("display", "none");
  }
  if (!options["hasCalendar"]) {
    $("#recacheButton").addClass("hidden");
  }
  if (options["truckName"]) {
    $("#truck-widget-header").html("Schedule for " + options["truckName"]);
  }
  $offTheRoadButton.click(function (evt) {
    $.ajax({
      url: "/admin/trucks/" + truckId + "/offtheroad",
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
      url: "/cron/recache?truck=" + truckId,
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

};