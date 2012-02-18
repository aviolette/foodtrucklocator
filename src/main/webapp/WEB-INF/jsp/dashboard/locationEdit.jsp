<%@include file="../common.jsp" %>
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
          <li><label><input id="invalidLoc" type="checkbox">&nbsp;Ignore in geolocation
            lookups</label></li>
        </ul>
      </div>
    </div>
    <div class="actions">
      <input id="submitButton" type="submit" class="btn primary" value="Save"/>&nbsp;
    </div>
  </fieldset>
</form>

<script type="text/javascript">
  var loc = ${location};
  function loadLocation(loc) {
    if (typeof loc == "undefined") {
      return;
    }
    $("#latitude").attr("value", loc.latitude);
    $("#longitude").attr("value", loc.longitude);
    $("#name").attr("value", loc.name);
    $("#invalidLoc").attr("checked", !loc.valid);
  }
  loadLocation(loc);
  var $submitButton = $("#submitButton");
  $submitButton.click(function(e) {
    loc.latitude = parseFloat($("#latitude").attr("value"));
    loc.longitude = parseFloat($("#longitude").attr("value"));
    loc.name = $("#name").attr("value");
    loc.valid = !$("#invalidLoc").is(":checked");
    e.preventDefault();
    $submitButton.addClass("disabled");
    $.ajax({
      context: document.body,
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(loc),
      url: "/admin/locations/" + loc.key,
      complete : function() {
        $submitButton.removeClass("disabled");
      },
      success: function() {
        flash("Successfully saved", "success");
      }
    });
  })
</script>

<%@include file="dashboardFooter.jsp" %>