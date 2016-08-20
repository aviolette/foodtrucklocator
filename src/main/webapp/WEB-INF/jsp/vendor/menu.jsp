<%@ include file="vendorheader.jsp" %>
<style>
  .form-group {
    padding-top: 20px;
  }
</style>
<div class="row">
  <div class="col-md-12">
    <p class="lead">
      This page allows you to build a menu that will be displayed on <a href="/trucks/${truck.id}">your truck's page</a>
      on
      The Chicago Food Truck Finder.
    </p>

    <div class="btn-toolbar">
      <div class="btn-group">
        <button id="add-section-button" class="btn"><span class="glyphicon glyphicon-plus"></span> Menu Section</button>
      </div>
      <div class="btn-group">
        <button id="save-button" class="btn"><span class="glyphicon glyphicon-save"></span> Save</button>
      </div>
    </div>
  </div>
</div>

<div class="row">
  <div class="col-md-12">
    <form id="menu" class="form-horizontal">
    </form>
  </div>
</div>

<%@include file="/WEB-INF/jsp/include/core_js.jsp" %>
<script>
  (function (truckId, loadedMenu) {
    var sectionId = 1;

    function addItem($parentSection, name, description) {
      if (typeof(description == "undefined")) {
        description = "";
      }
      $parentSection.append("<div class='form-group item-form-group'><label class='col-sm-2 control-label'/><div class='col-sm-2'> <input class='form-control item-name' type='text' placeholder='Item Name' value='" + name + "'/></div><div class='col-sm-2'> <input class='form-control' type='text' placeholder='Description' value='" + description + "'/></div></div>");
    }

    function addSection(name) {
      sectionId++;
      var secId = sectionId;
      $("#menu").append($("<div class='menu-section' id='menu-section-" + secId + "'><div class='form-group form-group-lg'>"
          + "<div class='col-sm-10'><div class='input-group'><input class='form-control section-name' type='text' value='" + name + "'/><span class='input-group-btn'><button class='btn btn-default' type='button'>Go!</button></span></div></div></div>"
          + "<button id='add-menu-item-button-" + sectionId + "' class='btn btn-default'><span class='glyphicon glyphicon-plus'></span> Menu Item</button>"));
      var $section = $("#menu-section-" + secId);
      $("#add-menu-item-button-" + sectionId).click(function (e) {
        e.preventDefault();
        addItem($section);
      });
      return $section;
    }

    $("#add-section-button").click(function () {
      addSection("Section #" + sectionId);
    });

    $("#save-button").click(function () {
      var menuJson = {};
      var sections = [];
      $(".menu-section").each(function (i, s) {
        var $s = $(s);
        var section = {"section": $($s.find($(".section-name"))[0]).val()};
        var items = [];
        $s.find($(".item-name")).each(function (j, itemNameControl) {
          var item = {"name": $(itemNameControl).val()};
          items.push(item);
        });
        section["items"] = items;
        sections.push(section);
      })
      menuJson = {"sections": sections};
      $.ajax({
        url: "/vendor/menu/" + truckId,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(menuJson),
        complete: function () {
        },
        success: function (e) {
        }
      });
    });

    (function () {
      $.each(loadedMenu["sections"], function (i, section) {
        var $section = addSection(section["section"]);
        $.each(section["items"], function (j, item) {
          addItem($section, item["name"], item["description"]);
        });
      });
    })();

  })("${truck.id}", <c:choose><c:when test="${empty(menu)}">{"sections": []}</c:when><c:otherwise>${menu.payload}</c:otherwise></c:choose>);
</script>

<%@ include file="vendorfooter.jsp" %>
