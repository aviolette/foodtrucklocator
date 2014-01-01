<%@ include file="header.jsp" %>
<script src="/script/holder.js"></script>
<%--
<c:if test="${foodTruckRequestOn}">
  <a href="/request" class="btn btn-primary" role="button">Request a Truck</a>
</c:if>
--%>

<div class="row" style="margin-top:20px" id="truckList">
</div>

<%@include file="include/core_js.jsp" %>
<script type="text/javascript">
  (function() {
    var $truckList = $("#truckList");
    $.ajax({
      url: '/services/trucks',
      success: function(data) {
        $truckList.empty();
        $.each(data, function(i, datum) {
          var icon = datum["previewIcon"];
          var $section = $("<div class='col-xs-6 col-md-3'></div>");
          var $thumbnail = $("<div class='thumbnail' id='thumbnail-" + i + "'></div>");
          $section.append($thumbnail);
          $truckList.append($section);
          if (icon) {
            $thumbnail.append("<img src='" + icon + "'/>")
          } else {
            Holder.add_image("/script/holder.js/180x180/sky/text:Truck Image", "#thumbnail-" + i).run();
          }
          $thumbnail.append("<p class='text-center'><a href='/trucks/" + datum["id"] + "'>" + datum['name']+"</a></p>")
        });
      }
    })
  })();
</script>
<%@ include file="footer.jsp" %>
