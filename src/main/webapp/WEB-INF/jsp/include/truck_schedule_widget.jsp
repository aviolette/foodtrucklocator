<%@include file="../common.jsp" %>
<h2 id="truck-widget-header">Schedule</h2>
<p>It may take up to <em>8 minutes</em> before the changes made here are reflected on the website.</p>
<table class="table table-striped">
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
<div class="btn-toolbar">
    <div class="btn-group">
        <button class="btn btn-default" id="addButton"><span class="glyphicon glyphicon-plus"></span>&nbsp;New Event
        </button>
    </div>
    <div class="btn-group">
        <button class="btn btn-default" id="offRoadButton"><span class="glyphicon glyphicon-trash"></span> Off the Road
        </button>
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
                <div id="truck-schedule-alert" class="alert alert-danger hidden" role="alert">
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
                    <div class="form-group">
                        <label><input id="lockStop" name="lockStop" type="checkbox">&nbsp;Prevent
                            twittalyzer from changing location</label>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="cancelButton" class="btn btn-default">Cancel</button>
                <button id="saveButton"  class="btn btn-primary" type="button" autocomplete="off" data-loading-text="Saving...">Save</button>
            </div>
        </div>
    </div>
</div>
