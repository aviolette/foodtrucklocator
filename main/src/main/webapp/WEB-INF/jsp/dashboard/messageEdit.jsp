<%@include file="dashboardHeaderBS3.jsp" %>

<form class="" action="" method="POST">

  <div class="form-group">
    <label for="description" class="control-label">Description</label>
    <textarea name="description" class="form-control" id="description" rows="5" cols="80">${message.message}</textarea>
  </div>
  <div class="form-group">
    <label for="startTime" class="control-label">Start</label>
    <input id="startTime" name="startTime" value="<joda:format value="${message.startTime}" pattern="YYYY-MM-dd"/>"
           class="form-control" type="date"/>
  </div>
  <div class="form-group">
    <label for="endTime" class="control-label">End</label>
    <input id="endTime" name="endTime" value="<joda:format value="${message.endTime}" pattern="YYYY-MM-dd"/>"
           class="form-control" type="date"/>
  </div>
  <div>
    <a href="/admin/messages" class="btn btn-default">Cancel</a>
    <button type="submit" class="btn btn-primary" name="action" value="Save"><span
        class="glyphicon glyphicon-save"></span> Save
    </button>
  </div>
</form>

<%@include file="dashboardFooterBS3.jsp" %>
