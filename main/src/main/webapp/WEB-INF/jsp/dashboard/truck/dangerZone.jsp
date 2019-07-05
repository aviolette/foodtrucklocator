<%@include file="../dashboardHeader1.jsp" %>
<%@include file="truckNav.jsp" %>

<div class="jumbotron">
  <h2>DANGER ZONE!</h2>
  <p class="lead mb-2">
    This button deletes the truck. There is no going back.
  </p>
  <p class="lead">
    <button id="deleteTruck" class="btn btn-outline-danger btn-lg">DELETE THIS TRUCK</button>
  </p>
</div>

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

<%@include file="../dashboardFooter1.jsp" %>
