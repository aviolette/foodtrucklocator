<%@ include file="vendorheader.jsp" %>
<%@ include file="../include/core_js.jsp" %>


<div class="row">
  <div class="col-md-12">
    <h2>Notifications</h2>
    <p class="lead">
      If you have tracking devices enabled in your truck, you can configure email notifications to be sent to your email
      based on truck events. Emails will be sent to the email configured on <a href="/vendor/settings/${truck.id}">General
      Settings Page</a>.
    </p>
  </div>
</div>


<form id="settingsForm" method="POST" action="">
  <div class="checkbox">
    <label>
      <input type="checkbox" name="options" value="notifyOfLocationChanges"
             data-toggle="toggle" ${truck.notifyOfLocationChanges ? "checked='checked'" : ""}>
      Send email when truck arrives at a location. This email includes location, gas level, and battery charge.
    </label>
  </div>
  <div class="checkbox">
    <label>
      <input type="checkbox" data-toggle="toggle" name="options"
             value="notifyWhenLeaving" ${truck.notifyWhenLeaving ? "checked='checked'" : ""}>
      Send email when truck leaves a location
    </label>
  </div>
    <div class="checkbox">
      <label>
        <input type="checkbox" data-toggle="toggle" name="options" value="notifyWhenDeviceIssues" ${truck.notifyWhenDeviceIssues ? "checked='checked'" : ""}>
        Notify when there is a problem with a device
      </label>
    </div>
  <div class="btn-toolbar" style="padding-top:20px">
    <div class="btn-group">
      <button id="settingsButton" type="button" class="btn btn-primary">Save</button>
    </div>
  </div>
</form>
<script type="text/javascript">
  (function () {
    $("#settingsButton").click(function (e) {
      e.preventDefault();
      $.ajax({
        type: "POST",
        url: "/vendor/notifications/${truck.id}",
        data: $("#settingsForm").serialize(),
        error: function (resp, textStatus, errorThrown) {
          $("#flash").css("display", "block");
          $("#flash").removeClass("alert-info");
          $("#flash").addClass("alert-error");
          $("#flash").html(resp.responseText);
        },
        success: function () {
          location.href = "/vendor";
        }
      });
    });
  })();
</script>

<%@ include file="vendorfooter.jsp" %>
