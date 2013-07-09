<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>

<div>
  <button class="btn btn-primary" data-loading-text="Saving..." id="scriptSaveButton">Save</button>
  <div id="editor"></div>
  <script src="/ace/ace.js" type="text/javascript" charset="utf-8"></script>
  <br/>
</div>


<script type="text/javascript">
  var editor = ace.edit("editor");
  editor.setTheme("ace/theme/twilight");
  editor.getSession().setMode("ace/mode/javascript");

  function refreshAddressRules() {
    $.ajax({
      url: '/services/addressRules',
      success: function (data) {
        editor.setValue(data["script"]);
        editor.gotoLine(0);
      }
    });
  }
  $(document).ready(function () {
    $("#scriptSaveButton").click(function (e) {
      e.preventDefault();
      $("#scriptSaveButton").button("loading");
      var stop = {script: editor.getValue()}
      $.ajax({
        url: "/services/addressRules",
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(stop),
        complete: function (e) {
          $("#scriptSaveButton").button("reset");
          $('#addressRuleModal').modal('hide');
        },
        error: function (e) {
          flash(e.responseText, "alert-error");
        },
        success: function (e) {
          flash("Successfully saved script");
          dissolveFlash();
        }
      });
    });
    refreshAddressRules();
  });
</script>

<%@include file="dashboardFooter.jsp" %>