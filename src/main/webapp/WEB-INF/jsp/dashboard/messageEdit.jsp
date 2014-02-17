<%@include file="dashboardHeader.jsp" %>

<form class="form-horizontal" action="" method="POST">
  <fieldset>
    <div class="control-group">
      <label for="description" class="control-label">Description</label>

      <div class="controls">
        <textarea name="description" class="span6" id="description" rows="5" cols="80">${event.description}</textarea>
      </div>
    </div>
    <div class="control-group">
      <label for="startTime" class="control-label">Start</label>

      <div class="controls">
        <input id="startTime" name="startTime" placeholder="MM/dd/YYYY" value="<joda:format value="${event.startTime}" pattern="YYYYMMdd-HHmm"/>"
               class="span6" type="text"/>
      </div>
    </div>
    <div class="control-group">
      <label for="endTime" class="control-label">End</label>

      <div class="controls">
        <input id="endTime" name="endTime" placeholder="MM/dd/YYYY" value="<joda:format value="${event.endTime}" pattern="YYYYMMdd-HHmm"/>"
               class="span6" type="text"/>
      </div>
    </div>
    <div class="control-group">
      <div class="controls">
        <input id="submitButton" type="submit" class="btn btn-primary" name="action" value="Save"/>&nbsp;
        <c:if test="${!event.new}">
          <input id="delete" type="submit" class="btn btn-danger" name="action" value="Delete"/>&nbsp;
        </c:if>
      </div>
    </div>
  </fieldset>
</form>

<%@include file="dashboardFooter.jsp" %>
