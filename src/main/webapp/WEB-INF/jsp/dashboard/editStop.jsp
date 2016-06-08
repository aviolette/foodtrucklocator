<%@include file="dashboardHeaderBS3.jsp" %>

<div id="truck-schedule-alert" class="alert alert-danger hidden" role="alert">
  <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
  <span class="sr-only">Error:</span><span id="truck-schedule-error"></span>
</div>

<form role="form" action="" method="POST" >
  <input type="hidden" name="truckId" value="${truck.id}"/>
  <input type="hidden" name="stopId" value="${stopId}"/>
  <div class="form-group" id="startTimeInputGroup">
    <label class="control-label" for="startTimeInput">Start</label>
    <input class="timeentry form-control" id="startTimeInput" name="startTime" value="${startTime}" type="datetime-local" autofocus/>
    <div id="startDay"></div>
  </div>
  <div class="form-group" id="endTimeInputGroup">
    <label class="control-label" for="endTimeInput">End</label>
    <input class="timeentry form-control" name="endTime" id="endTimeInput" value="${endTime}" type="datetime-local"/>
    <div id="endDay"></div>
  </div>
  <div class="form-group" id="locationInputGroup">
    <label class="control-label" for="locationInput">Location</label>
    <input class="form-control" id="locationInput" name="location" value="${locationName}" type="text" data-provide="typeahead" data-items="4"/>
  </div>
  <div class="form-group">
    <label><input id="lockStop" name="lockStop" type="checkbox" <c:if test="${locked}">checked="checked"</c:if>>&nbsp;Prevent
      twittalyzer from changing location</label>
  </div>
  <div>
    <a href="${backUrl}" id="cancelButton" class="btn btn-default">Cancel</a>
    <button id="saveButton"  class="btn btn-primary" type="submit" autocomplete="off" data-loading-text="Saving...">Save</button>
  </div>
</form>
<script type="text/javascript" src="/script/truck_edit_widget.js"></script>
<script type="text/javascript">
  enhancedDateWidget("start");
  unifiedDateControls(enhancedDateWidget("end"));
  locationMatching(${locations});
  $("#cancelButton").click(function(e) {
    e.preventDefault();
    location.href="${backUrl}";
  })
</script>
<%@include file="dashboardFooterBS3.jsp" %>