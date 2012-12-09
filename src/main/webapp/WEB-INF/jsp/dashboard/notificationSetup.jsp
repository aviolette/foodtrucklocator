<%@include file="dashboardHeader.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a href="#" class="btn" id="newNotification">New Notification Group</a>&nbsp;
  </div>
</div>

<h3>Notification Groups</h3>
<table class="table table-striped">
  <thead>
  <tr>
    <th style="min-width:200px">&nbsp;</th>
    <th style="min-width:100px">Name</th>
    <th style="min-width:150px">Twitter Handle</th>
    <th style="min-width:300px">Location</th>
    <th>Token</th>
    <th>Secret</th>
  </tr>
  </thead>
  <tbody id="notificationGroups">
  </tbody>
</table>

<div id="edit-notification" class="modal hide fade">
  <div class="modal-header">
    <a href="#" class="close">&times;</a>

    <h3>Edit Notification Group</h3>
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
          <label for="twitterHandle">Twitter Handle</label>

          <div class="input">
            <input id="twitterHandle" type="text"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="location">Location</label>

          <div class="input">
            <input id="location" type="text"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="token">Token</label>

          <div class="input">
            <input id="token" type="text"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="tokenSecret">Secret</label>

          <div class="input">
            <input id="tokenSecret" type="text"/>
          </div>
        </div>
      </fieldset>
    </form>
  </div>
  <div class="modal-footer">
    <a id="saveButton" href="#" class="btn primary">Save</a>
    <a id="cancelButton" href="#" class="btn secondary">Cancel</a>
  </div>
</div>

<script type="text/javascript">
  function saveItem(item) {
    $.ajax({
      url: "/services/notifications/" + item.id,
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
    $("#notificationGroups").empty();
    $.ajax({
      url: "/services/notifications",
      type: 'GET',
      complete : function() {
      },
      success: function(data) {
        $.each(data, function(i, datum) {
          var btnClass = datum["active"] ? "btn-success" : "btn-danger";
          var btnLabel = datum["active"] ? "Active" : "Inactive";
          var buttons = "<td><button id='deleteBtn-" + i +
              "' class='btn'>Delete</button>&nbsp;<button id='activeBtn-" + i +"' class='btn " + btnClass +
              "' data-toggle='button' type='button'>" + btnLabel + "</button></td>";
          $("#notificationGroups").append("<tr>" + buttons + "<td>" + datum["name"] + "</td><td>" + datum["twitterHandle"] + "</td><td>" + datum["location"]+"</td><td>" + datum["token"] + "</td><td>" + datum["tokenSecret"] + "</td></tr>");
          var $activeBtn = $("#activeBtn-" + i);
          if (datum["active"]) {
            $activeBtn.addClass("active").addClass("btn-success");
          }
          $activeBtn.click(function(e) {
            $activeBtn.button("toggle");
            datum["active"] = $activeBtn.hasClass("active");
            saveItem(datum);
          });
          var $deleteBtn = $("#deleteBtn-" + i);
          $deleteBtn.click(function(e) {
            if(confirm("Are you sure you want to delete '" + datum["name"] + "'?")) {
              deleteItem(datum["id"]);
            }
          });
        });
      }
    });
  }

  function deleteItem(id) {
    $.ajax({
      url: "/services/notifications/" + id,
      type: 'DELETE',
      contentType: 'application/json',
      success: function(e) {
        refreshList();
      }
    });
  }

  function invokeEditDialog(notification) {
    $("#name").attr("value", notification.name);
    $("#twitterHandle").attr("value", notification.twitterHandle);
    $("#location").attr("value", notification.location);
    $("#token").attr("value", notification.token);
    $("#tokenSecret").attr("value", notification.tokenSecret);
    $("#edit-notification").modal({ show: true, keyboard : true, backdrop: true});
    var $saveButton = $("#saveButton");
    $saveButton.unbind('click');
    $saveButton.click(function(e) {
      e.preventDefault();
      notification.name = $("#name").attr("value");
      notification.twitterHandle = $("#twitterHandle").attr("value");
      notification.location = $("#location").attr("value");
      notification.token = $("#token").attr("value");
      notification.tokenSecret = $("#tokenSecret").attr("value");
      $.ajax({
        url: "/services/notifications",
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(notification),
        complete : function() {
          $("#edit-notification").modal('hide');
        },
        success: function(e) {
          refreshList();
        }
      });

    });
  }
  $("#newNotification").click(function(e) {
    e.preventDefault();
    invokeEditDialog({name : "", twitterHandle : "", location : "", token: "", tokenSecret: ""});
  });
  refreshList();
</script>


<%@include file="dashboardFooter.jsp" %>
