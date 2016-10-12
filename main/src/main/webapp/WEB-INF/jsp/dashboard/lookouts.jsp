<%@include file="dashboardHeaderBS3.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a href="#" class="btn btn-primary" id="newLookout"><span class="glyphicon glyphicon-plus"></span> New Lookout</a>
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


<div id="edit-lookout" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <div class="media">
          <div class="text-center">
            <img id="truckIcon" class="center-block img-thumbnail" src=""/>
          </div>
          <div class="media-body">
            <h3 class=" text-center">Edit Lookout</h3>
          </div>
        </div>
      </div>
      <div class="modal-body">


        <div class="form-group">
          <label class="control-label" for="twitterHandle">Twitter ID</label>
          <input id="twitterHandle" class="form-control" type="text"/>
        </div>
        <div class="form-group">
          <label class="control-label" for="location">Location</label>

          <input id="location" class="form-control" type="text"/>
        </div>
        <div class="form-group">
          <label class="control-label" for="location">Keywords</label>

          <input id="keywords" class="form-control" type="text"/>
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
              "' class='btn btn-default'><span class='glyphicon glyphicon-remove'></span> Delete</button></td>";
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
    $("#twitterHandle").val(lookout.twitterHandle);
    $("#location").val(lookout.location);
    $("#keywords").val(lookout.keywords);
    $("#edit-lookout").modal({ show: true, keyboard: true, backdrop: true});
    var $saveButton = $("#saveButton");
    $saveButton.unbind('click');
    $saveButton.click(function (e) {
      e.preventDefault();
      delete lookout["location"];
      lookout.twitterHandle = $("#twitterHandle").val();
      lookout.locationName = $("#location").val();
      lookout.keywords = $("#keywords").val();
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
  <%@include file="dashboardFooterBS3.jsp" %>
