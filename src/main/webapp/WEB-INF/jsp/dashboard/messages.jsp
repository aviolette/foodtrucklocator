<%@include file="dashboardHeaderBS3.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a href="/admin/messages/new" class="btn btn-primary"><span class="glyphicon glyphicon-plus"></span> New Message</a>
  </div>
</div>

<table class="table">
  <c:forEach var="row" items="${messages}" varStatus="rowStatus">
    <tr id="row-${rowStatus.index}">
      <td><joda:format value="${row.startTime}" pattern="MM/dd/YYYY"/></td>
      <td><joda:format value="${row.endTime}" pattern="MM/dd/YYYY"/></td>
      <td>${row.message}</td>
      <td><button class="btn delete-button" row-data="${row.key}" id="delete-${rowStatus.index}">Delete</button></td>
    </tr>
  </c:forEach>
</table>

<script type="text/javascript">
  $(".delete-button").click(function(e) {
    var $item = $(e.target), messageId = $item.attr("row-data"), idx = $item.attr("id").substring(7);
    $.ajax({
      url: '/admin/messages/' + messageId,
      type: 'DELETE',
      success: function() {
        $("row-"+idx).remove();
      }
    })
  });
</script>

<%@include file="dashboardFooterBS3.jsp" %>
