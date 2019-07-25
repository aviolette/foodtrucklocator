<%@ include file="header.jsp" %>


<div class="row" id="truckList" >
</div>

<%@include file="include/core_js.jsp" %>
<script type="text/javascript">
  (function() {
    var truckData = [], $truckList = $("#truckList");
    var filteredBy = "${filteredBy}";

    function makeRelative(url) {
      return /^http:/.exec(url) ? url.substr(5) : url;
    }

    function refreshTruckList(dataSet) {
      $truckList.empty();
      $.each(dataSet, function(i, datum) {
        if (filteredBy && !datum["categories"].includes(filteredBy)) {
          return;
        }
        var icon = makeRelative(datum["previewIcon"]);
        var $section = $("<div class='col-xs-3 col-md-3'></div>");
        var thumbnailId ="thumbnail-" + i;
        var $thumbnail = $("<div class='thumbnail' id='" + thumbnailId + "'></div>");
        var $a = $("<a href='/trucks/" + datum["id"] + "'></a>");
        $section.append($a);
        $a.append($thumbnail);
        $truckList.append($section);
        if (!icon) {
          icon = "//storage.googleapis.com/truckpreviews/truck_holder.svg";
        }
        $("<img width='180' height='180' class='img-thumbnail' src='" + icon + "'/>").appendTo($thumbnail);
        $thumbnail.append("<p><strong>" + datum['name']+"</strong></p>")
      });
    }

    function loadTruckList() {
      $.ajax({
        url: '/services/v3/trucks',
        success: function(data) {
          truckData = data;
          refreshTruckList(truckData);
        }
      })
    }
    loadTruckList();
  })();
</script>
<%@ include file="footer.jsp" %>
