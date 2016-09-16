<%@include file="dashboardHeaderBS3.jsp" %>

<div class="row">
  <div class="col-md-12">
    <div class="btn-toolbar">
      <div class="btn-group">
        <a href="/admin/messages/new" class="btn btn-primary"><span class="glyphicon glyphicon-plus"></span> New Message</a>
      </div>
    </div>


    <div>
      <label><input type="checkbox" id="show-archived" <c:if test="${oldMessages}">checked</c:if>/> Show past
        messages</label>
    </div>

    <table class="table">
      <c:forEach var="row" items="${messages}" varStatus="rowStatus">
        <tr id="row-${rowStatus.index}">
          <td><joda:format value="${row.startTime}" pattern="MM/dd/YYYY"/></td>
          <td><joda:format value="${row.endTime}" pattern="MM/dd/YYYY"/></td>
          <td>${row.message}</td>
          <td style="min-width: 100px">
            <div class="btn-group">
              <button class="btn btn-default delete-button" row-data="${row.key}" id="delete-${rowStatus.index}"><span
                  class="glyphicon glyphicon-remove"></span></button>
              <a href="/admin/messages/${row.key}" class="btn btn-default"><span
                  class="glyphicon glyphicon-pencil"></span></a></div>
          </td>
        </tr>
      </c:forEach>
    </table>

  </div>
</div>

<script type="text/javascript">
  $("#show-archived").click(function (e) {
    if ($(this).is(":checked")) {
      location.href = "/admin/messages?show=all";
    } else {
      location.href = "/admin/messages";
    }
  });
  $(".delete-button").click(function(e) {
    e.preventDefault();
    var $item = $(this), messageId = $item.attr("row-data"), id = $item.attr("id"), idx = id.substring(7);
    if (!confirm("Are you sure you want to delete this?")) {
      return;
    }
    $.ajax({
      url: '/admin/messages/' + messageId,
      type: 'DELETE',
      success: function() {
        $("#row-" + idx).remove();
      }
    })
  });
</script>

<%@include file="dashboardFooterBS3.jsp" %>
