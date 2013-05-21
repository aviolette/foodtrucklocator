<%@ include file="nextgenheader.jsp" %>
<div class="row">
  <div class="span3">
    <ol>
      <c:forEach var="truck" items="${trucks}">
        <li ><a class="trucklink" truck-id="${truck.id}" href="#">${truck.name}</a></li>
      </c:forEach>
    </ol>
  </div>
  <div class="content span9">
    <table>
      <tbody>
        <tr>
          <td>Facebook</td>
          <td id="facebook"></td>
        </tr>
      </tbody>
    </table>
  </div>
</div>

<%@include file="include/core_js.jsp" %>
<script type="text/javascript">
  var trucks = ${trucksJson};
  $(".trucklink").click(function(e) {
    var $target = $(e.target), truckId = $target.attr("truck-id");
  });
</script>

<%@ include file="footer.jsp" %>
