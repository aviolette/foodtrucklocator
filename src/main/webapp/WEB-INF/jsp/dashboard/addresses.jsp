<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>

<ul class="nav nav-tabs">
  <li class="active" section="addressSection"><a href="/admin/trucks/addresses">Addresses</a></li>
  <li section="testSection"><a href="/admin/trucks/addressTest">Test</a></li>
  <li section="testSuiteSection"><a href="#">Test Suite</a></li>
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
<div class="tabSection" id="testSuiteSection" style="display:none">
  <button class="btn btn-primary" id="runTestsButton">Run Tests</button>
  <button class="btn" id="newTestButton">New Test</button>

  <table class="table">
    <thead>
    <tr>
      <th>Name</th>
      <th>Input</th>
      <th>Expected</th>
      <th>Truck Id</th>
      <th>Actual</th>
    </tr>
    </thead>
    <tbody id="testSuite"></tbody>
  </table>

  <div class="modal hide" id="testDialog">
    <div class="modal-header">
      <button type="button" class="close" data-dismiss="modal">X</button>
      <h3>Test</h3>
    </div>
    <div class="modal-body">
      <form>
        <fieldset>
          <div class="clearfix">
            <label for="testName">Name</label>

            <div class="input">
              <input id="testName" type="text"/>
            </div>
          </div>
          <div class="clearfix">
            <select class="input-large" id="testTruckId">
              <c:forEach var="truck" items="${trucks}">
                <option value="${truck.id}">${truck.name}</option>
              </c:forEach>
            </select>
          </div>
          <div class="clearfix">
            <label for="testInput">Input</label>

            <div class="input">
              <input id="testInput" type="text"/>
            </div>
          </div>
          <div class="clearfix">
            <label for="testExpected">Expected</label>

            <div class="input">
              <input id="testExpected" type="text"/>
            </div>
          </div>
        </fieldset>
      </form>
    </div>
    <div class="modal-footer">
      <a href="#" class="btn" data-dismiss="modal">Close</a>
      <a href="#" id="testSaveButton" class="btn btn-primary">Save</a>
    </div>
  </div>

</div>
<div class="tabSection" id="addressSection">
    <textarea id="addressRules" style="width:900px" rows="20" cols="80">

    </textarea>
    <br/>
    <button id="scriptSaveButton">Save</button>

</div>


<script type="text/javascript">

  function refreshAddressRules() {
    var $addressRules = $("#addressRules");
    $.ajax({
      url : '/services/addressRules',
      success : function(data) {
        $addressRules.val(data["script"]);
      }
    });
  }

  function refreshTestList() {
    $testSuite = $("#testSuite");
    $testSuite.empty();

    $.ajax({
      url : '/services/addressTest',
      success: function(data) {
        $.each(data, function(idx, test) {
          $testSuite.append("<tr class='testRow' id='test" + test.id + "'><td>" + test.name +
              "</td><td class='inputValue'>" +
              test.input + "</td><td class='expectedValue'>" +
              test.expected + "</td><td class='truckId'>" + test.truck +
              "</td><td class='actualValue'>&nbsp;</td><td><button class='runTest btn'>Run</button>&nbsp;<button class='btn btn-danger deleteTest'>Delete</button></div></td></tr>");
        });
        $(".testRow .deleteTest").click(function(e) {
          var $tr = $(e.target.parentNode.parentNode);
          var id = $tr.attr("id").substring(4);
          $.ajax({
            url: "/services/addressTest/" + id,
            type: 'DELETE',
            complete: function() {
              refreshTestList();
            }
          });
        });
        $(".testRow .runTest").click(function(e) {
          var $tr = $(e.target.parentNode.parentNode);
          var id = $tr.attr("id").substring(4);
          runTest($tr);
        })
      }
    });
  }

  function runTest($row) {
    $row.removeClass("success");
    $row.removeClass("error");
    var input = $("td.inputValue", $row).html();
    var expected = $("td.expectedValue", $row).html();
    var truck = $("td.truckId").html();
    var url = "/services/addressCheck?q=" + encodeURIComponent(input) + "&truck=" +
        encodeURIComponent(truck);
    $.ajax({
      url : url,
      success : function(data) {
        var results = data["results"];
        if (results.length == 0) {
          $row.addClass(expected.length == 0 ? "success" : "error");
          $("td.actualValue", $row).append("<strong>No Results</strong>");
        } else {
          var firstResult = results[0];
          $row.addClass(firstResult == expected ? "success" : "error");
          $("td.actualValue", $row).append(results[0]);
        }
      }
    });

  }

  $(document).ready(function() {
    $(".nav-tabs a").click(function(e) {
      e.preventDefault();
      $(".nav-tabs li").removeClass("active");
      $(".tabSection").css("display", "none");
      var $parent = $(e.target.parentNode);
      $parent.addClass("active");
      $("#" + $parent.attr("section")).css("display", "block");
    });
    $("#runTestsButton").click(function(e) {
      $(".testRow").each(function(idx, item) {
        runTest($(item));
      });
    });
    $("#scriptSaveButton").click(function(e) {
      e.preventDefault();
      var stop = {script : $("#addressRules").val()}
      $.ajax({
        url: "/services/addressRules",
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(stop),
        complete: function(e) {
          $('#addressRuleModal').modal('hide');
        },
        success: function(e) {
        }
      });
    });
    $("#addressRuleModalButton").click(function(e) {
      $('#addressRuleModal').modal('show');
    });
    $("#newTestButton").click(function(e) {
      $("#testDialog").modal("show");
    });
    $("#testSaveButton").click(function(e) {
      e.preventDefault();
      var testJSON = {
        name : $("#testName").attr("value"),
        truckId: $("#testTruckId").attr("value"),
        input: $("#testInput").attr("value"),
        expected: $("#testExpected").attr("value")
      };
      $.ajax({
        url: "/services/addressTest",
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(testJSON),
        complete: function(e) {
          $('#testDialog').modal('hide');
        },
        success: function(e) {
          refreshTestList();
        }
      });
    });
    $("#testButton").click(function(e) {
      e.preventDefault();
      var value = $("#searchField").attr("value");
      var truck = $("#truckList").attr("value");
      var url = "/services/addressCheck?q=" + encodeURIComponent(value) + "&truck=" +
          encodeURIComponent(truck);
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
    refreshTestList();
    refreshAddressRules();
  });
</script>

<%@include file="dashboardFooter.jsp" %>