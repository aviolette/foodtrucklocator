<%@ include file="header.jsp" %>
<style type="text/css">
  #categories {
    max-height: 300px;
    overflow-y:scroll;
  }

  @media (max-width: 1000px) {
    .category-selection {
      display: none;
    }

    body {
      background-color: white;
    }
  }

</style>

<div class="row">
  <div class="col-md-2 category-selection">
    <h4>Active</h4>
    <div class="checkbox">
      <label>
        <input id="active-cb" checked class="filter-cb" type="checkbox"/>
          Active Trucks
      </label>
    </div>
    <div class="checkbox">
      <label>
        <input id="inactive-cb" class="filter-cb" type="checkbox"/>
          Inactive Trucks
      </label>
    </div>
    <h4>Categories</h4>
    <a href="#" id="select-all">Select all</a> <a href="#" id="deselect-all">Deselect all</a>
    <div id="categories">
    </div>
  </div>
  <div class="col-md-10">
    <div id="truckList" class="tab-pane active">
    </div>
  </div>
</div>

<%@include file="include/core_js.jsp" %>
<script type="text/javascript">
  (function() {
    var truckData = [], $truckList = $("#truckList");

    function refreshTruckList(dataSet) {
      $truckList.empty();
      $.each(dataSet, function(i, datum) {
        var icon = datum["previewIcon"];
        var $section = $("<div class='col-xs-6 col-md-3'></div>");
        var thumbnailId ="thumbnail-" + i;
        var $thumbnail = $("<div class='thumbnail' id='" + thumbnailId + "'></div>");
        $section.append($thumbnail);
        $truckList.append($section);
        if (!icon) {
          icon = "//storage.googleapis.com/truckpreviews/truck_holder.svg";
        }
        $("<img width='180' height='180' src='" + icon + "'/>").appendTo($thumbnail);
        $thumbnail.append("<p class='text-center'><a href='/trucks/" + datum["id"] + "'>" + datum['name']+"</a></p>")
      });
    }

    function buildCategories() {
      var tmpSet = {}, $categories = $("#categories"), categoryList = [];
      $.each(truckData, function(i, truck) {
        $.each(truck.categories, function(j, category) {
          tmpSet[category] = 1;
        });
      });
      Object.keys(tmpSet).forEach(function(key) {
        categoryList.push(key);
      });
      categoryList.sort();
      $.each(categoryList, function(i, category) {
        $("<div class='checkbox'><label><input category-name='" + category + "' class='category-cb filter-cb' checked type='checkbox'/>" + category + "</label></div>").appendTo($categories);
      });
    }

    function parseSelectedCategories() {
      var categories = {};
      $(".category-cb:checked").each(function(i, target) {
        var item = $(target).attr("category-name");
        categories[item] = 1;
      });
      return categories;

    }

    function applyFilters() {
      data = [];
      var includeActive = $("#active-cb").is(":checked"),
          includeInactive = $("#inactive-cb").is(":checked"),
          selectedCategories = parseSelectedCategories();
      $.each(truckData, function(i, truck) {
        if ((truck.inactive && !includeInactive) || (!truck.inactive && !includeActive)) {
          return;
        }
        var hasCategory = false;
        for (var category=0; category < truck.categories.length; category++) {
          if (truck.categories[category] in selectedCategories) {
            hasCategory = true;
            break;
          }
        }
        if (hasCategory) {
          data.push(truck);
        }
      });
      return data;
    }

    function loadTruckList() {
      $.ajax({
        url: '/services/trucks?active=all',
        success: function(data) {
          truckData = data;
          buildCategories();
          refreshTruckList(applyFilters());
          $(".filter-cb").change(function() {
            refreshTruckList(applyFilters());
          });
        }
      })
    }
    loadTruckList();
    $("#select-all").click(function() {
      $(".category-cb").prop("checked", true);
      refreshTruckList(applyFilters());
    });
    $("#deselect-all").click(function() {
      $(".category-cb").prop("checked", false);
      refreshTruckList(applyFilters());
    });
  })();
</script>
<%@ include file="footer.jsp" %>
