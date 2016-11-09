<%@ include file="vendorheader.jsp" %>

<h2>Linked Social Media Accounts</h2>
<div class="row">
  <div class="col-md-3">
    <div class="panel panel-info">
      <div class="panel-heading">
        <h3 class="panel-title">Twitter</h3>
      </div>
      <div class="panel-body">
        <c:choose>
          <c:when test="${truck.hasTwitterCredentials}">
            <a href="/vendor/socialmedia/${truck.id}/unlink?account=twitter"
               class="btn btn-danger">Unlink @${truck.twitterHandle}</a>
          </c:when>
          <c:otherwise>
            <a href="/vendor/twitter?nologon=true" class="btn btn-default"><span
                class="glyphicon glyphicon-link"></span> Link</a>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</div>
<h2>Automated Posting</h2>

<div class="row">
  <div class="col-md-12">
    <form id="settingsForm" action="">
      <%--
      <div class="checkbox">
        <label><input type="checkbox" name="options"
                      value="dailySchedule" ${truck.postDailySchedule ? "checked='checked'" : ""}/>
          Post daily schedule at 8am</label>
      </div>
      <div class="checkbox">
        <label><input type="checkbox" name="options"
                      value="weeklySchedule" ${truck.postWeeklySchedule ? "checked='checked'" : ""}/>
          Post weekly schedule at 9am Monday</label>
      </div>
      --%>
      <div class="checkbox">
        <label><input type="checkbox" name="options"
                      value="postAtNewStop" ${truck.postAtNewStop ? "checked='checked'" : ""}/>
          Post at at non-blacklisted location</label>
      </div>

      <button id="settingsButton" type="button" class="btn btn-primary">Save</button>
    </form>

  </div>
</div>

<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript">
  (function () {
    $("#settingsButton").click(function (e) {
      e.preventDefault();
      $.ajax({
        type: "POST",
        url: "/vendor/socialmedia/${truck.id}",
        data: $("#settingsForm").serialize(),
        error: function (resp, textStatus, errorThrown) {
          var $flash = $("#flash");
          $flash.css("display", "block");
          $flash.removeClass("alert-info");
          $flash.addClass("alert-error");
          $flash.html(resp.responseText);
        },
        success: function () {
          location.href = "/vendor";
        }
      });
    });
  })();
</script>

<%@ include file="vendorfooter.jsp" %>
