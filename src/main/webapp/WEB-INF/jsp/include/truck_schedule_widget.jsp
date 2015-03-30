<%@include file="../common.jsp" %>
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

<div id="edit-stop" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Edit Stop</h3>
            </div>
            <div class="modal-body">
                <form role="form" class="form-horizontal">
                    <div class="form-group">
                        <label class="control-label" for="startTimeInput">Start</label>
                        <input class="timeentry form-control" id="startTimeInput" type="datetime-local" autofocus/>
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
