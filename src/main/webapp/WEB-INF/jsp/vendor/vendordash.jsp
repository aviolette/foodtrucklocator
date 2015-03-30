<%@ include file="vendorheader.jsp" %>
<%@ include file="../include/truck_schedule_widget.jsp" %>
<%@ include file="../include/core_js.jsp" %>
<script type="text/javascript" src="/script/truck_edit_widget.js"></script>
<script src="/script/lib/typeahead.bundle.js"></script>
<script type="text/javascript">
  runEditWidget("${truck.id}", ${locations}, ${categories});
</script>
<%@ include file="vendorfooter.jsp" %>
