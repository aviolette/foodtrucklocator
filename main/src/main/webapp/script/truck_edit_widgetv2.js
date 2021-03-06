var TruckScheduleWidget = function () {
  var _lastStop = null,
      _baseEndpoint,
      _truckId,
      _options,
      _spinner = new Spinner(),
      _calcStartDay,
      _calcEndDay,
      _hasFullSchedule = false,
      _counts = [],
      _categories = null,
      $editStop = $("#edit-stop"),
      $startTimeInput = $("#startTimeInput"),
      $endTimeInput = $("#endTimeInput"),
      _locationEndpoint = '/admin/locations',
      _useFormSubmitOnTouch = true;

  function padTime(t) {
    t = String(t);
    if (t.length == 1) {
      return "0" + t;
    }
    return t;
  }

  function fromDate(dateString) {
    var year = parseInt(dateString.substring(0, 4)),
        month = parseInt(dateString.substring(5, 7)) - 1,
        day = parseInt(dateString.substring(8, 10)),
        hour = parseInt(dateString.substring(11, 13)),
        min = parseInt(dateString.substring(14, 16));
    return new Date(year, month, day, hour, min, 0, 0);
  }

  function toDate(d) {
    return (d.getFullYear()) + "-" + padTime(d.getMonth() + 1) + "-" + padTime(d.getDate()) + "T" + padTime(d.getHours()) + ":" + padTime(d.getMinutes());
  }

  function enhancedDateWidget(widgetPrefix) {
    function figureOutDay($input, $output) {
      return function () {
        var time = fromDate($input.val()), day;
        switch (time.getDay()) {
          case 0:
            day = "Sunday";
            break;
          case 1:
            day = "Monday";
            break;
          case 2:
            day = "Tuesday";
            break;
          case 3:
            day = "Wednesday";
            break;
          case 4:
            day = "Thursday";
            break;
          case 5:
            day = "Friday";
            break;
          case 6:
            day = "Saturday";
            break;
        }
        $output.html(day);
      }
    }

    var $timeWidget = $("#" + widgetPrefix + "TimeInput");
    var calcDay = figureOutDay($timeWidget, $("#" + widgetPrefix + "Day"));
    $timeWidget.change(calcDay);
    return calcDay;
  }

  function unifiedDateControls(calcEndDay) {
    $startTimeInput.blur(function (e) {
      var startTime = fromDate($startTimeInput.val()),
          endTime = fromDate($endTimeInput.val());
      if (isNaN(endTime.getTime()) || startTime.getTime() >= endTime.getTime()) {
        var diff = startTime.getTime() + (60 * 60 * 2000);
        $endTimeInput.val(toDate(new Date(diff)));
        _calcEndDay();
      }
    });
  }

  function locationMatching(locations) {
    // Type-ahead related stuff
    var substringMatcher = function (strs) {
      return function findMatches(q, cb) {
        var matches, substrRegex;
        matches = [];
        substrRegex = new RegExp(q, 'i');
        $.each(strs, function (i, str) {
          if (substrRegex.test(str)) {
            matches.push({value: str});
          }
        });
        cb(matches);
      };
    };

    $("#locationInput").typeahead({
      hint: true,
      highlight: true,
      minLength: 1
    }, {name: 'locations', displayKey: 'value', source: substringMatcher(locations)});
  }

  function numStops() {
    return $("#scheduleTable").children().length;
  }

  function hasCategory(category) {
    return _categories.indexOf(category) >= 0;
  }


  function populateDialog(stop) {
    $startTimeInput.val(stop.startTimeH);
    _calcStartDay();
    $endTimeInput.val(stop.endTimeH);
    _calcEndDay();
    $("#locationInput").val(stop.location.name);
    $("#lockStop").val(stop.locked);
  }

  function invokeEditDialog(stop, afterwards) {
    populateDialog(stop);
    $("#locationInputGroup").removeClass("has-error");
    $("#truck-schedule-alert").addClass("hidden");
    $("#edit-stop").modal({show: true, keyboard: true, backdrop: true});
    var $saveButton = $("#saveButton");
    $saveButton.unbind('click');
    $saveButton.click(function (e) {
      e.preventDefault();
      var $btn = $(this).button('loading')
      stop.startTime = $startTimeInput.val();
      stop.endTime = $endTimeInput.val();
      var locationName = $("#locationInput").val();
      var oldLocation = stop.location;
      if (locationName != stop.location.name) {
        stop.locationName = locationName;
        delete stop.location;
      }
      stop.truckId = _truckId;
      stop.locked = $("#lockStop").is(":checked");
      if (locationName.length == 0) {
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
          error: function (e) {
            var obj = JSON.parse(e.responseText),
                message = obj.error;

            if (message == "Location is not resolved" && _options["vendorEndpoints"] && locationName.length > 0) {
              message = "<p>Location is not resolved</p><a class='btn btn-default' href='" + _baseEndpoint + "/locations/" + obj.data + "/edit?startTime=" + stop.startTime + "&endTime=" + stop.endTime + "'>Create Location</a>";
            }
            $("#truck-schedule-error").html(message);
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
          success: function () {
            $("#edit-stop").modal('hide');
            $btn.button('reset');
            afterwards();
          }
        });
      }
    });
    var $advancedOptionsButton = $("#advancedOptionsButton");
    $advancedOptionsButton.unbind("click");
    $advancedOptionsButton.click(function (e) {
      e.preventDefault();
      var startTime = fromDate($startTimeInput.val()),
          endTime = fromDate($endTimeInput.val()),
          stopId = (typeof stop.id) == "undefined" ? "new" : stop.id;
      location.href = _baseEndpoint + '/stops/' + stopId + "?location=" + encodeURIComponent($("#locationInput").val()) + "&locked=" + $("#lockStop").is(":checked") + "&startTime=" + startTime.getTime() + "&endTime=" + endTime.getTime();
    });
    var $cancelButton = $("#cancelButton");
    $cancelButton.unbind("click");
    $cancelButton.click(function (e) {
      e.preventDefault();
      $("#edit-stop").modal('hide');
    });
  }

  function newStop() {
    var now = new Date();
    if ((!hasCategory("Breakfast") && numStops() == 0) && now.getHours() < 10) {
      now.setHours(11);
      now.setMinutes(0);
    }
    if (numStops() > 0 && _lastStop != null) {
      now = new Date(_lastStop["endMillis"] + 60000)
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
    var today = toDate(now), later = toDate(new Date(now.getTime() + (2 * 60 * 60 * 1000)));
    invokeEditDialog({
          truckId: _truckId, locationName: "", location: {name: ""},
          startTimeH: today, endTimeH: later
        },
        refreshSchedule);
  }

  function refreshSchedule() {
    var scheduleTable = $("#scheduleTable");
    scheduleTable.empty();
    var d = new Date();
    d.setDate(1);
    var year = d.getFullYear(), month = padTime(d.getMonth() + 1), timeFormat = year + month + "01-0000";
    var timeQuery = "";
    var calendarQuery = $("#calendarListTable").hasClass("hidden");
    if (calendarQuery) {
      timeQuery = "&time=" + timeFormat;
    } else {
      timeQuery = "&includeCounts=true";
    }
    _spinner.spin($("#truck-schedule-spinner").get(0));
    $.ajax({
      url: '/services/v2/stops?truck=' + _truckId + timeQuery,
      type: 'GET',
      dataType: 'json',
      complete: function () {
        _spinner.stop();
      },
      success: function (schedule) {
        if (calendarQuery) {
          if (typeof _counts == "object") {
            schedule = mergeCounts(schedule);
          }
          _hasFullSchedule = true;
        } else {
          saveCounts(schedule);
        }
        drawScheduleList(schedule);
        drawCalendar(schedule);
      }
    });
  }

  function mergeCounts(schedule) {
    $.each(schedule, function (truckIndex, stop) {
      var count = _counts[String(stop.id)];
      if (count) {
        stop.totalTruckCount = count.count;
        stop.truckNames = count.truckNames;
      }
    });
    return schedule;
  }

  function saveCounts(schedule) {
    _counts = [];
    $.each(schedule, function (truckIndex, stop) {
      _counts[String(stop.id)] = {count: stop.totalTruckCount, truckNames: stop.truckNames};
    });
  }

  function drawScheduleList(schedule) {
    var d = new Date();
    d.setHours(0);
    d.setMinutes(0);
    d.setSeconds(0);
    var tomorrow = d.getTime() + 86400000;
    var today = d.getTime();
    var scheduleTable = $("#scheduleTable");
    scheduleTable.empty();

    var now = new Date().getTime();
    var prevHadStart = false;
    if (_options["refreshCallback"]) {
      _options["refreshCallback"]();
    }
    $.each(schedule, function (truckIndex, stop) {
      if (stop.endMillis < today) {
        return;
      }
      if (stop.startMillis < tomorrow) {
        _lastStop = stop;
      }
      var labels = (stop.locked) ? "&nbsp;&nbsp;<span class=\"glyphicon glyphicon-lock\"> </span>" :
          "";
      var crazyDuration = stop.durationMillis < 0 || stop.durationMillis > 43200000;
      var showControls = stop.startMillis < tomorrow || stop.origin != 'VENDORCAL';
      var truckNames = stop.truckNames.replace(/\'/g, "");
      labels += (stop.fromBeacon) ? "&nbsp;<span class=\"label important\">beacon</span>" : "";
      var truckCountLink = stop.totalTruckCount < 2 ? "" : "<span class='badge truck-info-badge' data-toggle='popover' data-content='" + truckNames + "'>" + stop.totalTruckCount + "</span>";
      var notes = stop.notes ? stop.notes.join('. ').replace(/\'/g, "") : "";
      var buf = "<tr " + (crazyDuration ? " class='error'" : "") + "><td>" + stop.startDate + "<br/>" + stop.startTime + " - " + stop.endTime +
          "<br/>" + stop.duration + " hours</td><td class=\"origin large-screen-only\"><a href='#' data-toggle='popover' data-content='" + notes + "'>" + stop.origin + "</a></td><td><a href='" + _locationEndpoint + "?q=" + encodeURIComponent(stop.location.name) +
          "'>"
          + stop.location.shortenedName + "</a>" + labels + "</td><td class='large-screen-only'>" + truckCountLink + "</td><td>";
      if (showControls) {
        if (!prevHadStart && now < stop.startMillis) {
          prevHadStart = true;
          buf = buf + "<button class='btn btn-default' id='truckStartNow" + truckIndex +
              "' class='btn success'>Start</button>"
        } else if (now >= stop.startMillis && now < stop.endMillis) {
          buf = buf + "<button class='btn btn-default' id='truckEndNow" + truckIndex +
              "' class='btn warning'>End</button>";
        }
      }
      buf += "&nbsp;</td><td>";
      if (showControls) {
        buf = buf + "<div class='btn-group'><button class='btn btn-default' id='truckDelete" + truckIndex +
            "' class='btn '><span class='glyphicon glyphicon-remove'></span> </button>&nbsp;<button class='btn btn-default' id='truckEdit" +
            truckIndex + "'><span class='glyphicon glyphicon-pencil'></span> </button></div></td></tr>";
      }
      scheduleTable.append(buf);
      $("#truckEdit" + truckIndex).click(function (e) {
        stop["startDate"] = toDate(new Date(stop["startMillis"]));
        stop["endDate"] = toDate(new Date(stop["endMillis"]));
        if (_useFormSubmitOnTouch) {
          location.href = _baseEndpoint + '/stops/' + stop.id;
        } else {
          invokeEditDialog(stop, refreshSchedule);
        }
      });

      if (_options["addCallback"]) {
        _options["addCallback"](stop);
      }

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
          stop.truckId = _truckId;
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
        if (confirm("Are you sure you want to delete this stop?")) {
          $.ajax({
            url: "/services/v2/stops/" + stop.id,
            type: 'DELETE',
            complete: function () {
              refreshSchedule();
            }
          })
        }
      });
    });
    $(function () {
      $('[data-toggle="popover"]').popover()
    });
  }

  function monthName(mon) {
    switch (mon) {
      case 0:
        return "January";
      case 1:
        return "February";
      case 2:
        return "March";
      case 3:
        return "April";
      case 4:
        return "May";
      case 5:
        return "June";
      case 6:
        return "July";
      case 7:
        return "August";
      case 8:
        return "September";
      case 9:
        return "October";
      case 10:
        return "November";
      default:
        return "December";
    }
  }

  function drawCalendar(schedule) {
    var d = new Date(), mon = d.getMonth(), $cal = $("#calendarTableBody"), day = 1, started = false;
    d.setDate(day);
    $("#month-header").empty().append(monthName(mon) + " " + d.getFullYear());
    $cal.empty();
    while (d.getMonth() == mon) {
      var $tr = $("<tr></tr>");
      for (var i = 0; i < 7; i++) {
        if (d.getMonth() != mon) {
          $tr.append("<td class='calendar-cell calendar-cell-other-month'>&nbsp;</td>");
          continue;
        }
        if (!started && i == d.getDay()) {
          started = true;
        }
        if (started) {
          $tr.append("<td class='calendar-cell'><span class='calendar-day'>" + d.getDate() + "</span><div  id='calendar-day-" + mon + "-" + day + "'></div></td>");
          day++;
          d.setDate(day);
        } else {
          $tr.append("<td class='calendar-cell calendar-cell-other-month'>&nbsp;</td>");
        }
      }
      $cal.append($tr);
    }
    $.each(schedule, function (i, stop) {
      var d = new Date(stop.startMillis), link = "<a href='" + _baseEndpoint + "/stops/" + stop.id +
          "'>" + stop.location.shortenedName + "</a><br/>";
      $("#calendar-day-" + d.getMonth() + "-" + d.getDate()).append(link);
    });
  }

  return {
    refresh: function () {
      refreshSchedule();
    },
    init: function (truckId, locations, categories, options) {
      _baseEndpoint = '/admin/trucks/' + truckId;
      _categories = categories;
      _truckId = truckId;
      _options = options || {};

      if (_options["vendorEndpoints"]) {
        _locationEndpoint = '/locations';
        _baseEndpoint = '/vendor';
      } else {
        $("#delay-warning").addClass("hidden");
        $("#truck-widget-header").addClass("hidden");
      }
      _useFormSubmitOnTouch = (window.innerWidth < 600) && _useFormSubmitOnTouch;
      _calcStartDay = enhancedDateWidget("start");
      _calcEndDay = enhancedDateWidget("end");

      unifiedDateControls(_calcEndDay);

      $editStop.keypress(function (e) {
        if (e.which == 13) {
          e.preventDefault();
          $("#saveButton").click();
        }
      });

      $("#scheduleCalendarButton").click(function () {
        $("#calendarListTable").addClass("hidden");
        $("#calendarTable").removeClass("hidden");
        $(this).removeClass("btn-default");
        $("#scheduleListButton").addClass("btn-default");
        if (!_hasFullSchedule) {
          refreshSchedule();
        }
      });

      $("#scheduleListButton").click(function () {
        $("#calendarListTable").removeClass("hidden");
        $("#calendarTable").addClass("hidden");
        $(this).removeClass("btn-default");
        $("#scheduleCalendarButton").addClass("btn-default");
      });

      locationMatching(locations);

      $editStop.on("shown.bs.modal", function () {
        $startTimeInput.focus();
      });

      $("#addButton").click(function (e) {
        if (_useFormSubmitOnTouch) {
          location.href = _baseEndpoint + '/stops/new';
        } else {
          newStop();
        }
      });

      $(document).keypress(function (e) {
        if (e.which == 110 && $editStop.css("display") == 'none') {
          e.preventDefault();
          newStop();
        }
      });

      this.refresh();

      var $offTheRoadButton = $("#offRoadButton");
      if (_options["vendorEndpoints"]) {
        $offTheRoadButton.css("display", "none");
      }
      if (!_options["hasCalendar"]) {
        $("#recacheButton").addClass("hidden");
      }
      if (_options["truckName"]) {
        $("#truck-widget-header").html("Schedule for " + options["truckName"]);
      }
      $offTheRoadButton.click(function (evt) {
        if (confirm("Are you sure you want to remove all remaining stops?")) {
          $.ajax({
            url: "/admin/trucks/" + truckId + "/offtheroad",
            type: 'POST',
            context: document.body,
            dataType: 'json',
            complete: function (data) {
              refreshSchedule();
            }
          });
        }
      });

      var $recacheButton = $("#recacheButton");
      $recacheButton.click(function (evt) {
        $recacheButton.empty();
        $recacheButton.append("Refreshing...")
        $.ajax({
          url: "/cron/recache?truck=" + _truckId,
          context: document.body,
          dataType: 'json',
          complete: function () {
            $recacheButton.empty();
            $recacheButton.append("Refresh")
          },
          success: function (data) {
            refreshSchedule();
          }
        });
      });
    }
  };
}();