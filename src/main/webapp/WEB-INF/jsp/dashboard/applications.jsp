<%@include file="dashboardHeaderBS3.jsp" %>
<%@include file="../include/rickshaw_css.jsp" %>
<%@include file="../include/graph_libraries.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a href="#" class="btn btn-primary" id="newApplication"> <span class="glyphicon glyphicon-plus"></span> New Application</a>&nbsp;
  </div>
</div>

<h3>Applications</h3>

<div id="appGraph"></div>

<table class="table table-striped">
  <thead>
  <tr>
    <th style="min-width:200px">&nbsp;</th>
    <th style="min-width:100px">Name</th>
    <th style="min-width:150px">App Key</th>
    <th style="min-width:100px">Count</th>
    <th style="min-width:200px">Description</th>
  </tr>
  </thead>
  <tbody id="applications">
  </tbody>
</table>

<div id="edit-application" class="modal hide fade">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <h3>Edit Application</h3>
  </div>
  <div class="modal-body">
    <form>
      <fieldset>
        <div class="clearfix">
          <label for="name">Name</label>
          <div class="input">
            <input id="name" type="text"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="description">Description</label>
          <div class="input">
            <input id="description" type="text"/>
          </div>
        </div>
      </fieldset>
    </form>
  </div>
  <div class="modal-footer">
    <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
    <button id="saveButton" class="btn primary">Save</button>
  </div>
</div>

<script type="text/javascript">
  function saveItem(item) {
    $.ajax({
      url: "/services/applications/" + item["appKey"],
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(item),
      complete : function() {
      },
      success: function(e) {
        refreshList();
      }
    });
  }
  function refreshList() {
    $("#applications").empty();
    $.ajax({
      url: "/services/applications",
      type: 'GET',
      complete : function() {
      },
      success: function(data) {
        var graphItems = [];
        $.each(data, function(i, datum) {
          var btnClass = datum["enabled"] ? "btn-danger" : "btn-success";
          var btnLabel = datum["enabled"] ? "Disable" : "Enable";
          graphItems.push("service.count.daily." + datum["appKey"]);
          var buttons = "<td><button id='deleteBtn-" + i +
              "' class='btn'>Delete</button>&nbsp;<button id='activeBtn-" + i +"' class='btn " + btnClass +
              "' data-toggle='button' type='button'>" + btnLabel + "</button></td>";
          $("#applications").append("<tr>" + buttons + "<td>" + datum["name"] + "</td><td><a href='/admin/applications/" + datum["appKey"] + "'>" + datum['appKey']
              + "</a></td><td><span class='badge'>" + datum["dailyCounts"] + "</span></td><td>" + datum["description"]+"</td></tr>");
          var $activeBtn = $("#activeBtn-" + i);
          if (!datum["enabled"]) {
            $activeBtn.addClass("active").addClass("btn-success");
          }
          $activeBtn.click(function(e) {
            $activeBtn.button("toggle");
            datum["enabled"] = !$activeBtn.hasClass("active");
            saveItem(datum);
          });
          var $deleteBtn = $("#deleteBtn-" + i);
          $deleteBtn.click(function(e) {
            if(confirm("Are you sure you want to delete '" + datum["name"] + "'?")) {
              deleteItem(datum["appKey"]);
            }
          });
        });
        var end = new Date(), start = new Date(end.getTime() - (30 *86400000));
        drawGraphs(graphItems, "appGraph", 86400000, start, end, true);
      }
    });
  }

  function deleteItem(id) {
    $.ajax({
      url: "/services/applications/" + id,
      type: 'DELETE',
      contentType: 'application/json',
      success: function(e) {
        refreshList();
      }
    });
  }

  function invokeEditDialog(notification) {
    $("#name").attr("value", notification.name);
    $("#appKey").attr("value", notification.appKey);
    $("#description").attr("value", notification.description);
    $("#edit-application").modal({ show: true, keyboard : true, backdrop: true});
    var $saveButton = $("#saveButton");
    $saveButton.unbind('click');
    $saveButton.click(function(e) {
      e.preventDefault();
      notification.name = $("#name").attr("value");
      notification.appKey = $("#appKey").attr("value");
      notification.description = $("#description").attr("value");
      $.ajax({
        url: "/services/applications",
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(notification),
        complete : function() {
          $("#edit-application").modal('hide');
        },
        success: function(e) {
          refreshList();
        }
      });

    });
  }
  $("#newApplication").click(function(e) {
    e.preventDefault();
    invokeEditDialog({name : "", description : "", enabled: true, appKey : ""});
  });
  refreshList();
  </script>


<%@include file="dashboardFooterBS3.jsp" %>
