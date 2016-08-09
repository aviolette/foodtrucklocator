var TruckScheduleWidget = function() {
  var _lastStop = null,
      _baseEndpoint,
      _truckId,
      _options,
      _calcStartDay,
      _calcEndDay,
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
        month = parseInt(dateString.substring(5, 7))-1,
        day = parseInt(dateString.substring(8, 10)),
        hour = parseInt(dateString.substring(11, 13)),
        min = parseInt(dateString.substring(14, 16));
    return new Date(year, month, day, hour, min, 0, 0);
  }

  function toDate(d) {
    return (d.getFullYear()) + "-" + padTime(d.getMonth()+1) + "-" + padTime(d.getDate()) + "T" + padTime(d.getHours()) + ":" + padTime(d.getMinutes());
  }

  function enhancedDateWidget(widgetPrefix) {
    function figureOutDay($input, $output) {
      return function() {
        var time = fromDate($input.val()), day;
        switch (time.getDay()) {
          case 0:
            day = "Sunday"; break;
          case 1:
            day = "Monday"; break;
          case 2:
            day = "Tuesday"; break;
          case 3:
            day = "Wednesday"; break;
          case 4:
            day = "Thursday"; break;
          case 5:
            day = "Friday"; break;
          case 6:
            day = "Saturday"; break;
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
    var substringMatcher = function(strs) {
      return function findMatches(q, cb) {
        var matches, substrRegex;
        matches = [];
        substrRegex = new RegExp(q, 'i');
        $.each(strs, function(i, str) {
          if (substrRegex.test(str)) {
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
    $endTimeInput.val( stop.endTimeH);
    _calcEndDay();
    $("#locationInput").val(stop.location.name);
    $("#lockStop").val( stop.locked);
  }

  function invokeEditDialog(stop, afterwards) {
    populateDialog(stop);
    $("#locationInputGroup").removeClass("has-error");
    $("#truck-schedule-alert").addClass("hidden");
    $("#edit-stop").modal({ show: true, keyboard: true, backdrop: true});
    var $saveButton = $("#saveButton");
    $saveButton.unbind('click');
    $saveButton.click(function (e) {
      e.preventDefault();
      var $btn = $(this).button('loading')
      stop.startTime =  $startTimeInput.val();
      stop.endTime =  $endTimeInput.val();
      var locationName = $("#locationInput").val();
      var oldLocation = stop.location;
      if (locationName != stop.location.name) {
        stop.locationName = locationName;
        delete stop.location;
      }
      stop.truckId = _truckId;
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

  function newStop() {
    var now = new Date();
    if ((!hasCategory("Breakfast") && numStops() == 0) && now.getHours() < 10 ) {
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
    var today = toDate(now), later = toDate(new Date(now.getTime() + (2*60*60*1000)));
    invokeEditDialog({truckId: _truckId, locationName: "", location: { name: ""},
          startTimeH: today, endTimeH: later },
        refreshSchedule);
  }

  function refreshSchedule() {
    var scheduleTable = $("#scheduleTable");
    scheduleTable.empty();
    _lastStop = null;
    var d = new Date();
    d.setHours(0);
    d.setMinutes(0);
    d.setSeconds(0);
    var tomorrow = d.getTime() + 86400000;
    $.ajax({
      url: '/services/v2/stops?truck=' + _truckId,
      type: 'GET',
      dataType: 'json',
      success: function (schedule) {
        var now = new Date().getTime(), numStops = schedule.length;
        var prevHadStart = false;
        if (_options["refreshCallback"]) {
          _options["refreshCallback"]();
        }
        $.each(schedule, function (truckIndex, stop) {
          if (stop.startMillis < tomorrow) {
            _lastStop = stop;
          }
          var labels = (stop.locked) ? "&nbsp;&nbsp;<span class=\"glyphicon glyphicon-lock\"> </span>" :
              "";
          var crazyDuration = stop.durationMillis < 0 || stop.durationMillis > 43200000;
          var showControls = stop.startMillis < tomorrow || stop.origin != 'VENDORCAL';
          var truckNames = stop.truckNames.replace(/\'/g, "");
          labels += (stop.fromBeacon) ? "&nbsp;<span class=\"label important\">beacon</span>" : "";
          var truckCountLink = stop.totalTruckCount < 2 ? "" : "<span class='badge truck-info-badge' data-toggle='popover' data-content='" + truckNames +"'>" + stop.totalTruckCount + "</span>";
          var notes = stop.notes ? stop.notes.join('. ').replace(/\'/g, "") : "";
          var buf = "<tr " + (crazyDuration ? " class='error'" : "") + "><td>" + stop.startDate + "</td><td>" + stop.startTime + "</td><td>" + stop.endTime +
              "</td><td>" + stop.duration + "</td><td class=\"origin\"><a href='#' data-toggle='popover' data-content='" + notes + "'>" + stop.origin + "</a></td><td><a href='" + _locationEndpoint + "?q=" + encodeURIComponent(stop.location.name) +
              "'>"
              + stop.location.name + "</a>" + labels + "</td><td>" + truckCountLink +"</td><td>";
          if (showControls) {
            if (!prevHadStart && now < stop.startMillis) {
              prevHadStart = true;
              buf = buf + "<button class='btn btn-default' id='truckStartNow" + truckIndex +
                  "' class='btn success'>Start Now</button>"
            } else if (now >= stop.startMillis && now < stop.endMillis) {
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
            if (_useFormSubmitOnTouch) {
              location.href = _baseEndpoint + '/stops/'+ stop.id;
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

  return {
    refresh : function() {
      refreshSchedule();
    },
    init : function(truckId, locations, categories, options)  {
      _baseEndpoint = '/admin/trucks/' + truckId;
      _categories = categories;
      _truckId = truckId;
      _options = options || {};

      if (_options["vendorEndpoints"]) {
        _locationEndpoint = '/locations';
        _useFormSubmitOnTouch = false;
        _baseEndpoint = '/vendor';
      }
      _useFormSubmitOnTouch = (window.innerWidth < 600) && _useFormSubmitOnTouch;
      _calcStartDay = enhancedDateWidget("start");
      _calcEndDay = enhancedDateWidget("end");

      unifiedDateControls(_calcEndDay);

      $editStop.keypress(function(e) {
        if (e.which == 13) {
          e.preventDefault();
          $("#saveButton").click();
        }
      });

      locationMatching(locations);

      $editStop.on("shown.bs.modal", function() {
        $startTimeInput.focus();
      });

      $("#addButton").click(function (e) {
        if (_useFormSubmitOnTouch) {
          location.href = _baseEndpoint + '/stops/new';
        } else {
          newStop();
        }
      });

      $(document).keypress(function(e) {
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
          url: "/cron/recache?truck=" + _truckId,
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
    }
  };
}();