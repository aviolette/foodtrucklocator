<%@include file="dashboardHeaderBS3.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a href="#" class="btn btn-primary" id="newNotification"><span class="glyphicon glyphicon-plus"></span> New
      Notification Group</a>&nbsp;
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
  </tr>
  </thead>
  <tbody id="notificationGroups">
  </tbody>
</table>

<div id="edit-notification" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <div class="media">
          <div class="text-center">
            <img id="truckIcon" class="center-block img-thumbnail" src=""/>
          </div>
          <div class="media-body">
            <h3 id="truckTitle" class=" text-center"></h3>
            <p class="text-center hidden" id="truck-url"></p>
          </div>
        </div>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label for="name">Name</label>
          <input class="form-control" id="name" type="text"/>
        </div>
        <div class="form-group">
          <label for="twitterHandle">Twitter Handle</label>
          <input class="form-control" id="twitterHandle" type="text"/>
        </div>
        <div class="form-group">
          <label for="location">Location</label>
          <input id="location" class="form-control" type="text"/>
        </div>
        <div class="form-group">
          <label for="token">Token</label>
          <input class="form-control" id="token" type="text"/>
        </div>
        <div class="form-group">
          <label for="tokenSecret">Secret</label>
          <input class="form-control" id="tokenSecret" type="text"/>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-default" data-dismiss="modal" aria-hidden="true">Close</button>
        <button id="saveButton" class="btn btn-primary">Save</button>
      </div>
    </div>
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
              "' class='btn btn-default'>Delete</button>&nbsp;<button id='activeBtn-" + i + "' class='btn " + btnClass +
              "' data-toggle='button' type='button'>" + btnLabel + "</button></td>";
          $("#notificationGroups").append("<tr>" + buttons + "<td>" + datum["name"] + "</td><td><a target='_blank' href='http://twitter.com/" + datum['twitterHandle']
              +"'>@" + datum["twitterHandle"] + "</a></td><td><a href='/admin/locations?q=" +
              encodeURIComponent(datum["location"]) +"'>" + datum["location"]+"</a></td></tr>");
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
    $("#name").val(notification.name);
    $("#twitterHandle").val(notification.twitterHandle);
    $("#location").val(notification.location);
    $("#token").val(notification.token);
    $("#tokenSecret").val(notification.tokenSecret);
    $("#edit-notification").modal({ show: true, keyboard : true, backdrop: true});
    var $saveButton = $("#saveButton");
    $saveButton.unbind('click');
    $saveButton.click(function(e) {
      e.preventDefault();
      notification.name = $("#name").val();
      notification.twitterHandle = $("#twitterHandle").val();
      notification.location = $("#location").val();
      notification.token = $("#token").val();
      notification.tokenSecret = $("#tokenSecret").val();
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


<%@include file="dashboardFooterBS3.jsp" %>
