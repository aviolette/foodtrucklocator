<%@include file="../common.jsp" %>
<%@include file="dashboardHeaderBS3.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <button class="btn btn-primary" data-loading-text="Saving..." id="scriptSaveButton"><span class="glyphicon glyphicon-upload"></span> Save</button>
  </div>
</div>
<div class="row" >
  <div class="col-md-12" id="edit-div">
    <div style="margin-top:10px;min-width:500px;min-height:500px" id="editor"></div>
  </div>
  <br/>
</div>

<script src="/ace/ace.js" type="text/javascript" charset="utf-8"></script>
<script type="text/javascript">
  (function() {
    function resize() {
      $("#editor").width($("#edit-div").width());
    }
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
      resize();
      $("#edit-div").height($("#editor").height() + 30);
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
  })();

</script>

<%@include file="dashboardFooterBS3.jsp" %>