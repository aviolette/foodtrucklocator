<%@ include file="header.jsp" %>
<script src="/script/holder.js"></script>

<c:if test="${foodTruckRequestOn}">
  <a href="/request" class="btn btn-primary" role="button">Request a Truck</a>
</c:if>

<ul class="nav nav-tabs<c:if test="${!empty(filteredBy)}"> hidden</c:if>" id="navTabs" style="margin-top:20px">
  <li class="active"><a href="#truckList" data-toggle="tab">Active Trucks</a></li>
  <li><a href="#inactiveTrucks" data-toggle="tab">Inactive Trucks</a></li>
</ul>
<div class="tab-content" style="margin-top:20px">
  <div id="truckList" class="tab-pane active">
  </div>
  <div id="inactiveTrucks" class="tab-pane">
    <em>Loading list...</em>
  </div>
</div>

<%@include file="include/core_js.jsp" %>
<script type="text/javascript">
  (function() {
    function loadTruckList(active, $truckList, prefix) {
      $.ajax({
        url: '/services/trucks?active=' + active<c:if test="${!empty(filteredBy)}"> + "&tag=${filteredBy}"</c:if>,
        success: function(data) {
          $truckList.empty();
          $.each(data, function(i, datum) {
            var icon = datum["previewIcon"];
            var $section = $("<div class='col-xs-6 col-md-3'></div>");
            var thumbnailId ="thumbnail-" + prefix + "-" + i;
            var $thumbnail = $("<div class='thumbnail' id='" + thumbnailId + "'></div>");
            $section.append($thumbnail);
            $truckList.append($section);
            if (icon) {
              $thumbnail.append("<img width='180' height='180' src='" + icon + "'/>")
            } else {
              Holder.add_image("/script/holder.js/180x180/sky/text:Truck Image", "#" + thumbnailId).run();
            }
            $thumbnail.append("<p class='text-center'><a href='/trucks/" + datum["id"] + "'>" + datum['name']+"</a></p>")
          });
        }
      })

    }
    loadTruckList(true, $("#truckList"), 'active');
    var inactiveLoaded = false;
    $('a[href="#inactiveTrucks"]').on('shown.bs.tab', function (e) {
      if (inactiveLoaded) {
        return;
      }
      loadTruckList(false, $("#inactiveTrucks"), 'inactive');
      inactiveLoaded = true;
    })
  })();
</script>
<%@ include file="footer.jsp" %>
