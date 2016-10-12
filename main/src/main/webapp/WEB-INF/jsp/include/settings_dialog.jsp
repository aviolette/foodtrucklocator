<%@include file="../common.jsp" %>
<div id="settingsDialog" class="modal hide fade">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">&times;</button>

    <h3>Settings</h3>
  </div>
  <div class="modal-body">

    <form>
      <fieldset>
        <label class="checkbox" id="useGPSLabel">
          <input type="checkbox" id="useGPS"> Use my browser's location to determine map center
        </label>
        <label>Enter your location</label>
        <input id="locationName" type="text" class="span4" placeholder="Enter an intersection or address"/>
        </fieldset>
        <button class="btn btn-primary" id="saveSettingsButton">Save</button>
    </form>

  </div>
</div>
