function renderMenu(menuJSON, $menu) {

  function addSection(sectionName, description) {
    var body = (description) ? "<div class='panel-body'><p>" + description + " </p></div>" : "";
    var $panelSection = $("<div class='col-md-6'><div class='panel panel-default'><div class='panel-heading'><h3 class='panel-title'>" + sectionName + "</h3></div>" + body + "<div class='list-group'></div></div></div>")
    $menu.append($panelSection);
    var items = $menu.find("div.list-group");
    return $(items[items.length - 1]);
  }

  function addItem($dl, name, description) {
    $dl.append("<span class='list-group-item'><h4>" + name + "</h4><p>" + description + "</p></span>");
  }

  $.each(menuJSON["sections"], function (i, section) {
    var $section = addSection(section["section"], section["description"]);
    $.each(section["items"], function (j, item) {
      addItem($section, item["name"], item["description"]);
    });
  });

}