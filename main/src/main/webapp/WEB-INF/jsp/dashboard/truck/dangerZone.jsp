<%@ include file="truckHeader.jsp" %>

<h2>DANGER ZONE!</h2>
<p>
  This button deletes the truck. There is no going back.
</p>
<button id="deleteTruck" class="btn btn-danger btn-lg">DELETE THIS TRUCK</button>

<script type="text/javascript">
  $("#deleteTruck").click(function () {
    if (confirm("ARE YOU SURE?")) {
      $.ajax({
        url: "/services/trucks/${truck.id}",
        type: "DELETE",
        success: function () {
          location.href = '/admin/trucks';
        }
      });
    }
  });
</script>

<%@ include file="truckFooter.jsp" %>