/**
 * Created by andrew on 3/28/15.
 */
runEditWidget = function(truckId, locations, categories, options) {
  var lastStop, $editStop = $("#edit-stop");
  options = options || {};

  $editStop.keypress(function(e) {
    if (e.which == 13) {
      e.preventDefault();
      $("#saveButton").click();
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


  $editStop.on("shown.bs.modal", function() {
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
      stop.truckId = truckId;
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
      url: '/services/schedule/' + truckId,
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
              stop.truckId = truckId;
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
    return categories.indexOf(category) >= 0;
  }

  function newStop() {
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