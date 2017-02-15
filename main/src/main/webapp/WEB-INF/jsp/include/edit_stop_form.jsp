<%@include file="../common.jsp" %>

<div class="row">
  <div class="col-md-8">

    <form role="form" action="" method="POST" id="stopEditForm">
      <input type="hidden" name="truckId" value="${truck.id}"/>
      <input type="hidden" name="stopId" value="${stopId}"/>
      <div class="form-group" id="startTimeInputGroup">
        <label class="control-label" for="startTimeInput">Start</label>
        <input class="timeentry form-control" id="startTimeInput" name="startTime" value="${startTime}"
               type="datetime-local" autofocus/>
        <div id="startDay"></div>
      </div>
      <div class="form-group" id="endTimeInputGroup">
        <label class="control-label" for="endTimeInput">End</label>
        <input class="timeentry form-control" name="endTime" id="endTimeInput" value="${endTime}"
               type="datetime-local"/>
        <div id="endDay"></div>
      </div>
      <div class="form-group" id="locationInputGroup">
        <label class="control-label" for="locationInput">Location</label>
        <input class="form-control" id="locationInput" name="location" value="${locationName}" type="text"
               data-provide="typeahead" data-items="4"/>
      </div>
      <div class="form-group">
        <label><input id="lockStop" name="lockStop" type="checkbox" <c:if test="${locked}">checked="checked"</c:if>>&nbsp;Prevent
          twittalyzer from changing location</label>
      </div>
      <div class="form-group">
        <label class="control-label" for="description">Description (optional)</label>
        <textarea class="form-control" id="description" rows="4">${description}</textarea>
      </div>
      <div>
        <a href="${backUrl}" id="cancelButton" class="btn btn-default">Cancel</a>
        <button class="btn btn-danger hidden" id="deleteButton">Delete</button>
        <button id="saveButton" class="btn btn-primary" autocomplete="off" data-loading-text="Saving...">
          Save
        </button>
      </div>
    </form>
  </div>
  <div class="col-md-4">
    <img
        <c:if test="${empty(imageUrl)}">class="hidden"</c:if> src="${imageUrl}" id="imageUrl" width="240"/>
    <div class="hidden" id="remove-image-button-section">
      <button id="remove-image-button" class="btn-danger btn">Remove</button>
    </div>
    <c:if test="${enableImages}">
      <form action="${baseEndPoint}/images" class="dropzone" id="upload">
      </form>
    </c:if>
  </div>

</div>
</div>

<script type="text/javascript" src="/script/truck_edit_widget.js"></script>
<script type="text/javascript">
  <c:if test="${enableImages}">
  Dropzone.options.upload = {
    headers: {"X-Dropzone-Truck": "${truck.id}"},
    maxFiles: 1,
    acceptedFiles: "image/*",
    complete: function (foo) {
      this.removeAllFiles();
    },
    success: function (foo, response) {
      $("#imageUrl").attr("src", response);
      $("#imageUrl").removeClass("hidden");
      toggleRemoveButton();
    }
  };
  </c:if>
  enhancedDateWidget("start");
  unifiedDateControls(enhancedDateWidget("end"));
  locationMatching(${locations});
  $("#cancelButton").click(function (e) {
    e.preventDefault();
    location.href = "${backUrl}";
  });
  $("#deleteButton").click(function (e) {
    e.preventDefault();
    if (confirm("Are you sure you want to delete this stop?")) {
      $.ajax({
        url: "/services/v2/stops/${stopId}",
        type: 'DELETE',
        error: function (e) {
          flash("There was a problem deleting this stop.");
        },
        success: function () {
          location.href = "${backUrl}";
        }
      });
    }
  });
  if ("${stopId}" != "new") {
    $("#deleteButton").removeClass("hidden");
  }
  $("#saveButton").click(function (e) {
    e.preventDefault();
    var $btn = $(this).button('loading'),
        $startTimeInput = $("#startTimeInput"),
        $endTimeInput = $("#endTimeInput"),
        locationName = $("#locationInput").val(),
        imageInput = $("#imageUrl").attr("src");
    stop = {
      startTime: $startTimeInput.val(),
      endTime: $endTimeInput.val(),
      imageUrl: imageInput,
      locationName: locationName,
      description: $("#description").val(),
      <c:if test="${!empty(stopId) && stopId != 'new'}">
      id: ${stopId},
      </c:if>
      truckId: "${truck.id}",
      locked: $("#lockStop").is(":checked")
    };
    if (locationName.length == 0) {
      $("#locationInputGroup").addClass("has-error");
      flash("No location specified");
      $btn.button('reset')
    } else {
      $.ajax({
        url: "/services/v2/stops",
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(stop),
        error: function (e) {
          var obj = JSON.parse(e.responseText),
              message = obj.error;
          if (message == "Location is not resolved" && locationName.length > 0) {
            message = "<p>Location is not resolved</p><a class='btn btn-default' href='${baseEndPoint}/locations/" + obj.data + "/edit?startTime=" + stop.startTime + "&endTime=" + stop.endTime + "'>Create Location</a>";
          } else if (!message) {
            message = "There are errors on the page";
          }
          flash(message);
          if (/location/i.exec(obj.error)) {
            $("#locationInputGroup").addClass("has-error");
          }
          if (/start time/i.exec(obj.error)) {
            $("#startTimeInputGroup").addClass("has-error");
          }
          if (/end time/i.exec(obj.error)) {
            $("#endTimeInputGroup").addClass("has-error");
          }
          $btn.button('reset')
        },
        success: function () {
          location.href = "${backUrl}";
        }
      });
    }

  });
  function toggleRemoveButton() {
    if ($("#imageUrl").attr("src")) {
      $("#remove-image-button-section").removeClass("hidden");
    } else {
      $("#remove-image-button-section").addClass("hidden");
    }
  }
  $("#remove-image-button").click(function () {
    $("#imageUrl").attr("src", "");
    $("#imageUrl").addClass("hidden");
    $("#remove-image-button").addClass("hidden");
  });
  toggleRemoveButton();
</script>
