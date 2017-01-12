<%@ include file="vendorheader.jsp" %>
<%@ include file="../include/core_js.jsp" %>

<h2>Notifications</h2>
<p>Notifications will be sent as emails to the email specified on the <a href="/vendor/settings/${truck.id}">General
  Settings Page</a></p>
<form id="settingsForm" method="POST" action="">
  <div class="checkbox">
    <label>
      <input type="checkbox" name="options" value="notifyOfLocationChanges"
             data-toggle="toggle" ${truck.notifyOfLocationChanges ? "checked='checked'" : ""}>
      Notify when truck stops at location
    </label>
  </div>
  <%--
    <div class="checkbox">
      <label>
        <input type="checkbox" data-toggle="toggle">
        Notify when truck leaves a location
      </label>
    </div>
    <div class="checkbox">
      <label>
        <input type="checkbox" data-toggle="toggle">
        Notify when there is a problem with a device
      </label>
    </div>
    --%>
  <button id="settingsButton" type="button" class="btn btn-primary">Save</button>
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
