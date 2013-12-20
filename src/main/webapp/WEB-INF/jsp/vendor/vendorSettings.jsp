<%@ include file="vendorheader.jsp" %>

<form id="settingsForm" method="POST" action="">
  <fieldset>
    <legend>Truck Settings</legend>
    <label for="name">Name</label>
    <input class="span3" id="name" name="name" type="text" value="${truck.name}" placeholder="The display name"/>
    <label for="phone">Phone</label>
    <input class="span3" id="phone" name="phone" type="text" value="${truck.phone}" placeholder="XXX-XXX-XXXX"/>
    <label for="email">Email</label>
    <input class="span3" id="email" name="email" type="text" value="${truck.email}" placeholder="foo@bar.com"/>
    <label for="url">URL</label>
    <input class="span4" id="url" name="url" type="text" value="${truck.url}" placeholder="The URL to your website"/>
    <label for="description">Description</label>
    <textarea class="span6" id="description" name="description">${truck.description}</textarea>
    <div>
      <button id="settingsButton" type="button" class="btn btn-primary">Submit</button>
    </div>
  </fieldset>
</form>
<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript">
  (function() {
    $("#settingsButton").click(function(e) {
      e.preventDefault();
      $.ajax({
        type: "POST",
        url: "/vendor/settings/${truck.id}",
        data: $("#settingsForm").serialize(),
        error: function(resp, textStatus, errorThrown) {
          $("#flash").css("display", "block");
          $("#flash").removeClass("alert-info");
          $("#flash").addClass("alert-error");
          $("#flash").html(resp.responseText);
        },
        success : function() {
          $("#flash").css("display", "block");
          $("#flash").removeClass("alert-error");
          $("#flash").addClass("alert-info");
          $("#flash").html("Successfully saved");
        }
      });
    });
  })();
</script>
<%@ include file="vendorfooter.jsp" %>
