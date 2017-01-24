<style>
  .form-group, .btn-group {
    padding-top: 20px;
  }
</style>
<div class="row">
  <div class="col-md-12">
    <p class="lead">
      Build a custom menu organized by menu sections to display on <a href="/trucks/${truck.id}">your truck's page</a>
      on
      The Chicago Food Truck Finder. Start by clicking the "Menu Section" button.
    </p>

    <div class="btn-group">
      <button id="add-section-button" class="btn btn-default"><span class="glyphicon glyphicon-plus"></span> Menu
        Section
      </button>
    </div>
  </div>
</div>

<div class="row">
  <div class="col-md-12">
    <form id="menu" class="form-horizontal">
    </form>
    <div class="btn-group">
      <button id="save-button" class="btn btn-primary btn-lg"> Save
      </button>
    </div>

  </div>
</div>

<%@include file="/WEB-INF/jsp/include/core_js.jsp" %>
<script>
  (function (truckId, loadedMenu, endpoint) {
    var sectionId = 1;


    function stale() {
      $(window).off('beforeunload');
      $(window).on('beforeunload', function () {
        return "You have unsaved changes.  Are you sure you want to leave?";
      })
    }

    function fresh() {
      $(window).off('beforeunload');
    }


    function addItem($parentSection, name, description) {
      if (typeof(name) == "undefined") {
        name = "";
      }
      if (typeof(description) == "undefined") {
        description = "";
      }
      var $item = $("<div class='form-group item-form-group'><label class='col-sm-2 control-label'/><div class='col-sm-3'> <input class='form-control item-name' type='text' placeholder='Item Name' value=''/></div><div class='col-sm-7'> <div class='input-group'><input class='form-control item-description" +
          "' type='text' placeholder='Description' value=''/><span class='input-group-btn'><button class='delete-button btn btn-default'><span class='glyphicon glyphicon-minus'></span></button></span></div></div></div>");
      $parentSection.append($item);
      $item.find("input").blur(function (e) {
        stale();
      });
      var $nameField = $item.find("input.item-name");
      $nameField.val(name);
      $item.find("input.item-description").val(description);
      $item.find('.delete-button').click(function (e) {
        e.preventDefault();
        if ($nameField.val().length == 0 || confirm("Are you sure you want to delete this item?")) {
          stale();
          $item.remove();
        }
      });
      return $nameField;
    }


    function addSection(name, description) {
      sectionId++;
      if (typeof(description) == "undefined") {
        description = "";
      }
      var secId = sectionId;
      $("#menu").append($("<div class='menu-section' id='menu-section-" + secId + "'><div class='form-group form-group-lg'>"
          + "<div class='col-sm-6'><div class='input-group input-group-lg'><input id='section-name-" + secId + "' class='form-control section-name' placeholder='Section name' type='text' value=''/><span class='input-group-btn'><button id='menu-section-delete-" + secId + "' class='btn btn-default' type='button'><span class='glyphicon glyphicon-minus'></span></button></span></div></div>"
          + "<div class='col-sm-6'><input id='section-description-" + secId + "' type='text' class='form-control section-description input-lg' placeholder='Section Description' value=''/></div>"
          + "</div>"
          + "<button id='add-menu-item-button-" + sectionId + "' class='btn btn-default'><span class='glyphicon glyphicon-plus'></span> Menu Item</button></div>"));
      var $section = $("#menu-section-" + secId), $nameControl = $("#section-name-" + secId);
      $nameControl.val(name);
      $("#section-description-" + secId).val(description);
      $section.find("input").blur(function (e) {
        stale();
      });
      $("#add-menu-item-button-" + sectionId).click(function (e) {
        e.preventDefault();
        stale();
        addItem($section).focus();
      });
      $("#menu-section-delete-" + sectionId).click(function (e) {
        e.preventDefault();
        if ($nameControl.val().length == 0 || confirm("Are you sure you want to delete this section and all it's menu items?")) {
          stale();
          $section.remove();
        }
      });
      return $section;
    }

    $("#add-section-button").click(function () {
      stale();
      addSection("", "");
      $("#section-name-" + sectionId).focus();
    });

    $("#save-button").click(function () {
      var menuJson = {};
      var sections = [];
      $(".menu-section").each(function (i, s) {
        var $s = $(s);
        var section = {
          "section": $($s.find($(".section-name"))[0]).val(),
          "description": $($s.find($(".section-description"))[0]).val()
        };
        var items = [];
        $s.find($(".item-name")).each(function (j, itemNameControl) {
          var item = {"name": $(itemNameControl).val()};
          items.push(item);
        });
        $s.find($(".item-description")).each(function (j, itemDescriptionControl) {
          items[j]["description"] = $(itemDescriptionControl).val();
        });
        section["items"] = items;
        sections.push(section);
      });
      menuJson = {"sections": sections};
      $.ajax({
        url: endpoint,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(menuJson),
        complete: function () {
          fresh();
        },
        success: function (e) {
        }
      });
    });

    (function () {
      $.each(loadedMenu["sections"], function (i, section) {
        var $section = addSection(section["section"], section["description"]);
        $.each(section["items"], function (j, item) {
          addItem($section, item["name"], item["description"]);
        });
      });
    })();

  })("${truck.id}", <c:choose><c:when test="${empty(menu)}">{"sections": []}</c:when><c:otherwise>${menu.payload}</c:otherwise></c:choose>, "<c:choose><c:when test="${empty(endpoint)}">/vendor/menu/${truck.id}</c:when><c:otherwise>${endpoint}</c:otherwise></c:choose>");
</script>
