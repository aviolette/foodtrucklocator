<%@include file="dashboardHeader.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a href="#" class="btn" id="newLookout">New Lookout</a>&nbsp;
  </div>
</div>

<h3>Lookouts</h3>
<table class="table table-striped">
  <thead>
  <tr>
    <th style="min-width:200px">&nbsp;</th>
    <th style="min-width:150px">Twitter Handle</th>
    <th style="min-width:300px">Location</th>
    <th style="min-width:100px">Keywords</th>
  </tr>
  </thead>
  <tbody id="lookouts">
  </tbody>
</table>

<div id="edit-lookout" class="modal hide fade">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>

    <h3>Edit Lookout</h3>
  </div>
  <div class="modal-body">
    <form>
      <fieldset>
        <div class="control-group">
          <label class="control-label" for="twitterHandle">Twitter ID</label>

          <div class="controls">
            <input id="twitterHandle" class="span6" type="text"/>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="location">Location</label>

          <div class="controls">
            <input id="location" class="span6" type="text"/>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="location">Keywords</label>

          <div class="controls">
            <input id="keywords" class="span6" type="text"/>
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
      url: "/services/lookouts/" + item.id,
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify(item),
      complete: function () {
      },
      success: function (e) {
        refreshList();
      }
    });
  }
  function refreshList() {
    $("#lookouts").empty();
    $.ajax({
      url: "/services/lookouts",
      type: 'GET',
      complete: function () {
      },
      success: function (data) {
        $.each(data, function (i, datum) {
          var btnClass = datum["active"] ? "btn-success" : "btn-danger";
          var btnLabel = datum["active"] ? "Active" : "Inactive";
          var buttons = "<td><button id='deleteBtn-" + i +
              "' class='btn'>Delete</button></td>";
          $("#lookouts").append("<tr>" + buttons + "<td><a target='_blank' href='http://twitter.com/" + datum['twitterHandle']
              + "'>@" + datum["twitterHandle"] + "</a></td><td><a href='/admin/locations?q=" +
              encodeURIComponent(datum["location"]["name"]) + "'>" + datum["location"]["name"] + "</a></td><td>" +
              datum["keywords"] + "</td></tr>");
          var $activeBtn = $("#activeBtn-" + i);
          if (datum["active"]) {
            $activeBtn.addClass("active").addClass("btn-success");
          }
          $activeBtn.click(function (e) {
            $activeBtn.button("toggle");
            datum["active"] = $activeBtn.hasClass("active");
            saveItem(datum);
          });
          var $deleteBtn = $("#deleteBtn-" + i);
          $deleteBtn.click(function (e) {
            if (confirm("Are you sure you want to delete '" + datum["twitterHandle"] + "'?")) {
              deleteItem(datum["twitterHandle"]);
            }
          });
        });
      }
    });
  }

  function deleteItem(id) {
    $.ajax({
      url: "/services/lookouts/" + id,
      type: 'DELETE',
      contentType: 'application/json',
      success: function (e) {
        refreshList();
      }
    });
  }

  function invokeEditDialog(lookout) {
    $("#twitterHandle").attr("value", lookout.twitterHandle);
    $("#location").attr("value", lookout.location);
    $("#keywords").attr("value", lookout.keywords);
    $("#edit-lookout").modal({ show: true, keyboard: true, backdrop: true});
    var $saveButton = $("#saveButton");
    $saveButton.unbind('click');
    $saveButton.click(function (e) {
      e.preventDefault();
      delete lookout["location"];
      lookout.twitterHandle = $("#twitterHandle").attr("value");
      lookout.locationName = $("#location").attr("value");
      lookout.keywords = $("#keywords").attr("value");
      $.ajax({
        url: "/services/lookouts",
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(lookout),
        complete: function () {
          $("#edit-lookout").modal('hide');
        },
        success: function (e) {
          refreshList();
        }
      });

    });
  }
  $("#newLookout").click(function (e) {
    e.preventDefault();
    invokeEditDialog({twitterHandle: "", location: "", keywords: ""});
  });
  refreshList();
</script>
<%@include file="dashboardFooter.jsp" %>
