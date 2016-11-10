<%@ include file="vendorheader.jsp" %>

<div class="row">
  <div class="col-md-12">

    <div class="panel panel-info">
      <div class="panel-heading">
        <h3 class="panel-title">Preview</h3>
      </div>
      <div class="panel-body">
        <p class="lead">${message.fullScheduleAsHtml}</p>
      </div>
    </div>

    <form action="" method="post">
      <div class="btn-toolbar">
        <div class="btn-group">
          <a href="/vendor" class="btn btn-default">Cancel</a>
        </div>
        <div class="btn-group">
          <button class="btn btn-primary" type="submit">Submit</button>
        </div>
      </div>
    </form>
  </div>
</div>

<%@ include file="../include/core_js.jsp" %>
<%@ include file="vendorfooter.jsp" %>
