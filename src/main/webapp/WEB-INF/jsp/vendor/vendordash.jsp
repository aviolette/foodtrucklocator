<%@ include file="vendorheader.jsp" %>
<style>
td.origin {
  display:none;
}
</style>
<%@ include file="../include/truck_schedule_widget.jsp" %>
<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript" src="/script/truck_edit_widget.js"></script>
<script src="/script/lib/typeahead.bundle.js"></script>
<script type="text/javascript">
  runEditWidget("${truck.id}", ${locations}, ${categories}, {vendorEndpoints: true, hasCalendar: ${not empty(truck.calendarUrl)}});
</script>
<%@ include file="vendorfooter.jsp" %>
