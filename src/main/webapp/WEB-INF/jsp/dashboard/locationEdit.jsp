<%@include file="../common.jsp"%>
<%@include file="dashboardHeader.jsp" %>

<form>
  <fieldset>
    <legend>Location</legend>
    <div class="clearfix">
      <label>Name</label>
      <div class="input">
        <input id="name" type="text"/>
      </div>
    </div>
    <div class="clearfix">
      <label>Latitude</label>
      <div class="input">
        <input id="latitude" type="text"/>
      </div>
    </div>
    <div class="clearfix">
      <label>Longitude</label>
      <div class="input">
        <input id="longitude" type="text"/>
      </div>
    </div>
    <div class="clearfix">
      <label>&nbsp;</label>
      <div class="input">
        <ul class="inputs-list">
        <li><label><input type="checkbox">&nbsp;Ignore in geolocation lookups</label></li>
        </ul>
      </div>
    </div>
    <div class="actions">
      <input id="submitButton" type="submit" class="btn primary" value="Save"/>&nbsp;
      <input type="reset" class="btn" value="Cancel"/>
    </div>
  </fieldset>
</form>

<script type="text/javascript">
  var loc = ${location};
  function loadLocation(location) {
    if (typeof location == "undefined") {
      return;
    }
    $("#latitude").attr("value", location.latitude);
    $("#longitude").attr("value", location.longitude);
    $("#name").attr("value", location.name);
  }
  loadLocation(loc);
  alert(JSON.stringify(loc));
  $("#submitButton").click(function(e) {
    loc.latitude = parseFloat($("#latitude").attr("value"));
    loc.longitude = parseFloat($("#longitude").attr("value"));
    loc.name = $("#name").attr("value");
    e.preventDefault();
    $.ajax({
      context: document.body,
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(loc),
      url: "/admin/locations/" + loc.key,
      complete : function() {
      },
      success: function() {
        alert("Success:" + JSON.stringify(loc));
      }
    });
  })
</script>

<%@include file="dashboardFooter.jsp" %>