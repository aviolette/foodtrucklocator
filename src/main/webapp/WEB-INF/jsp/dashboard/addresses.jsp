<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>

<ul class="nav nav-tabs">
  <li class="active" section="addressSection"><a href="/admin/trucks/addresses">Addresses</a></li>
  <li section="testSection"><a href="/admin/trucks/addressTest">Test</a></li>
</ul>

<div class="tabSection" id="testSection" style="display:none">
  <form class="well form-search">
    <input id="searchField" type="text" class="input-x-large search-query">
    <select class="input-large" id="truckList">
      <c:forEach var="truck" items="${trucks}">
        <option value="${truck.id}">${truck.name}</option>
      </c:forEach>
    </select>
    <button id="testButton" class="btn">Test</button>
  </form>
  <ul id="searchResults"></ul>
</div>
<div class="tabSection" id="addressSection">
  <button class="btn btn-primary"  id="addressRuleModalButton" >New Address Rule</button>

  <table>
    <tbody id="addressRules">
    </tbody>
  </table>

  <div class="modal hide" id="addressRuleModal">
    <div class="modal-header">
      <button type="button" class="close" data-dismiss="modal">X</button>
      <h3>Adress Rule</h3>
    </div>
    <div class="modal-body">
      <form>
        <fieldset>
          <div class="clearfix">
            <label>Pattern</label>
            <div class="input">
              <input id="name" type="text"/>
            </div>
          </div>
         </fieldset>
       </form>
    </div>
    <div class="modal-footer">
      <a href="#" class="btn" data-dismiss="modal">Close</a>
      <a href="#" id="saveButton" class="btn btn-primary">Save</a>
    </div>
  </div>
</div>


<script type="text/javascript">
  function refreshList() {
    $addressRules = $("#addressRules");
    $addressRules.empty();
  }

  $(document).ready(function() {
    var stop = {pattern : $("#name").attr("value")}
    $(".nav-tabs a").click(function(e) {
      e.preventDefault();
      $(".nav-tabs li").removeClass("active");
      $(".tabSection").css("display", "none");
      var $parent = $(e.target.parentNode);
      $parent.addClass("active");
      $("#" + $parent.attr("section")).css("display", "block");
    });
    $("#saveButton").click(function(e) {
      e.preventDefault();
      $.ajax({
        url: "/services/addressRules",
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(stop),
        complete: function(e) {
          $('#addressRuleModal').modal('hide');
        },
        success: function(e) {
          refreshList();
        }
      });
    });
    $("#addressRuleModalButton").click(function(e) {
      $('#addressRuleModal').modal('show')
    });
    $("#testButton").click(function(e) {
      e.preventDefault();
      var value = $("#searchField").attr("value");
      var truck = $("#truckList").attr("value");
      var url = "/services/addressCheck?q="+encodeURIComponent(value) + "&truck="+encodeURIComponent(truck);
      $.ajax({
        url: url,
        success : function(data) {
          var $searchResults = $("#searchResults");
          $searchResults.empty();
          $.each(data["results"], function(idx, item) {
            $searchResults.append("<li>" + item + "</li>");
          });
        }
      });
    });
   });
</script>

<%@include file="dashboardFooter.jsp" %>