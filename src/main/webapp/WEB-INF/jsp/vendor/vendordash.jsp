<%@ include file="vendorheader.jsp" %>
<style>
td.origin {
  display:none;
}
</style>

<h1>${truck.name}</h1>
<div class="row">
  <div class="col-md-3">

  </div>
  <div class="col-md-9">
    <button type="button" class="btn btn-default" aria-label="Edit">
      <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
    </button>

    <dl>
      <dt>Description</dt>
      <dd>${truck.description}</dd>
      <dt>Website</dt>

    </dl>
  </div>
</div>
<div class="row">
  <div class="col-md-12">
    <p>It may take up to <em>8 minutes</em> before the changes made here are reflected on the website.</p>
    <%@ include file="../include/truck_schedule_widget.jsp" %>
  </div>
</div>
<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript" src="/script/truck_edit_widget.js"></script>
<script src="/script/lib/typeahead.bundle.js"></script>
<script type="text/javascript">
  runEditWidget("${truck.id}", ${locations}, ${categories}, {vendorEndpoints: true, hasCalendar: ${not empty(truck.calendarUrl)}});
</script>
<%@ include file="vendorfooter.jsp" %>
