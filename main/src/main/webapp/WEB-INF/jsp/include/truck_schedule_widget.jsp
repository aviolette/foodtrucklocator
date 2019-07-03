<%@include file="../common.jsp" %>
<h2 id="truck-widget-header">Schedule</h2>
<div class="alert alert-info" role="alert" id="delay-warning">
  <p><span class="glyphicon glyphicon-warning-sign"></span> It may take up to <em>8 minutes</em> before the changes made
    here are reflected on the website.</p>
</div>

<div class="btn-group" style="margin-bottom:20px">
  <button class="btn btn-outline-primary" id="scheduleListButton"><span class="glyphicon glyphicon-list icon icon-list"></span></button>
  <button class="btn btn-outline-secondary" id="scheduleCalendarButton"><span class="glyphicon glyphicon-calendar icon icon-calendar"></span>
  </button>

</div>
<div style="position:relative" style="right: 100px" id="truck-schedule-spinner"></div>

<table class="table table-striped" id="calendarListTable">
  <thead>
  <tr>
    <td>When</td>
    <td class="origin large-screen-only">Origin</td>
    <td>Location</td>
    <td class="large-screen-only"># Trucks</td>
    <td>&nbsp;</td>
    <td class="edit-button-bar">&nbsp;</td>
  </tr>
  </thead>
  <tbody id="scheduleTable">
  </tbody>
</table>

<table id="calendarTable" class="table table-bordered hidden d-none">
  <thead>
  <tr>
    <td id="month-header" colspan="7" class="lead text-center" style="font-weight:bold">Month</td>
  </tr>
  <tr>
    <th>Sunday</th>
    <th>Monday</th>
    <th>Tuesday</th>
    <th>Wednesday</th>
    <th>Thursday</th>
    <th>Friday</th>
    <th>Saturday</th>
  </tr>
  </thead>
  <tbody id="calendarTableBody">

  </tbody>
</table>

<div class="btn-toolbar">
  <div class="btn-group">
    <button class="btn btn-outline-primary" id="addButton"><span class="glyphicon glyphicon-plus icon icon-plus"></span>&nbsp;New Event
    </button>
  </div>
  <div class="btn-group d-none">
    <button class="btn btn-default" id="offRoadButton"><span class="glyphicon glyphicon-road"></span> Off the Road
    </button>
  </div>
  <div class="btn-group d-none">
    <button class="btn btn-default" id="recacheButton"><span class="glyphicon glyphicon-refresh"></span>&nbsp;Reload
      from calendar
    </button>
  </div>
</div>
<div id="edit-stop" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Edit Stop</h3>
      </div>
      <div class="modal-body">
        <div id="truck-schedule-alert" class="alert alert-danger hidden d-none" role="alert">
          <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
          <span class="sr-only">Error:</span><span id="truck-schedule-error"></span>
        </div>
        <form role="form" class="form-horizontal">
          <div class="form-group" id="startTimeInputGroup">
            <label class="control-label" for="startTimeInput">Start</label>
            <input class="timeentry form-control" id="startTimeInput" type="datetime-local" autofocus/>
            <div id="startDay"></div>
          </div>
          <div class="form-group" id="endTimeInputGroup">
            <label class="control-label" for="endTimeInput">End</label>
            <input class="timeentry form-control" id="endTimeInput" type="datetime-local"/>
            <div id="endDay"></div>
          </div>
          <div class="form-group" id="locationInputGroup">
            <label class="control-label" for="locationInput">Location</label>
            <input class="form-control" id="locationInput" type="text" data-provide="typeahead" data-items="4"/>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <div class="btn-toolbar pull-right">
          <div class="btn-group">
            <button id="advancedOptionsButton" class="btn btn-default">Advanced options...</button>
          </div>
          <div class="btn-group">
            <button id="cancelButton" class="btn btn-default">Cancel</button>
          </div>
          <div class="btn-group">
            <button id="saveButton" class="btn btn-primary" type="button" autocomplete="off"
                    data-loading-text="Saving..."></span> Save
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
