<%@include file="dashboardHeader.jsp" %>

<form action="/admin/trucks/${truckStop.truck.id}/events/edit" method="POST">
  <fieldset>
    <div class="clearfix">
      <label for="startTimeInput">Start time</label>

      <div class="input">
        <input id="startTimeInput" type="text" name="startTime" value="<joda:format value="${truckStop.startTime}" pattern="hh:mm a"/>"/>
      </div>
    </div>
    <div class="clearfix">
      <label for="endTimeInput">End time</label>

      <div class="input">
        <input id="endTimeInput" type="text" name="endTime" value="<joda:format value="${truckStop.endTime}" pattern="hh:mm a"/>"/>
      </div>
    </div>
    <div class="clearfix">
      <label for="locationInput">Location</label>

      <div class="input">
        <input id="locationInput" name="location" type="text" value="${truckStop.location.name}"/>
      </div>
    </div>
    <div class="clearfix">
      <div class="input">
        <input type="checkbox" id="lockStop" name="lockStop"/>&nbsp;<label style="float:none"
                                                                           for="lockStop">Prevent
        twittalyzer from changing location</label>
      </div>
    </div>
  </fieldset>

  <input type="submit" class="btn primary" value="Save"/>


</form>

<%@include file="dashboardFooter.jsp" %>