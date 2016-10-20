<%@ include file="vendorheader.jsp" %>

<form id="settingsForm" method="POST" action="">
  <div class="form-group">
    <label for="name">Name</label>
    <input class="form-control" id="name" name="name" type="text" value="${truck.name}" placeholder="The display name"/>
  </div>
  <div class="form-group">
    <label for="phone">Phone</label>
    <input class="form-control" id="phone" name="phone" type="text" value="${truck.phone}" placeholder="XXX-XXX-XXXX"/>
  </div>
  <div class="form-group">
    <label for="email">Email</label>
    <input class="form-control" id="email" name="email" type="text" value="${truck.email}" placeholder="foo@bar.com"/>
  </div>
  <div class="form-group">
    <label for="url">URL</label>
    <input class="form-control" id="url" name="url" type="text" value="${truck.url}" placeholder="The URL to your website"/>
  </div>
  <div class="form-group">
    <label for="description">Description</label>
    <textarea class="form-control" id="description" name="description">${truck.description}</textarea>
  </div>
  <button id="settingsButton" type="button" class="btn btn-primary">Save</button>
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
          location.href = "/vendor";
        }
      });
    });
  })();
</script>
<%@ include file="vendorfooter.jsp" %>
