<%@ include file="header.jsp" %>

<p>Submit this form to request a food truck.  Thanks!</p>
<form method="POST" action="">
  <c:if test="${!empty(foodTruckRequest)}"><input type="hidden" name="id" value="${foodTruckRequest.key}"/></c:if>
  <div class="form-group">
    <label for="eventName">Event Name</label>
    <input type="text" placeholder="" name="eventName" class="form-control" id="eventName" value="<c:if test="${!empty(foodTruckRequest)}">${foodTruckRequest.eventName}</c:if>"/>
    <p class="help-block hidden" id="eventNameHelp">Event name is required</p>
  </div>
  <div class="form-group">
    <label for="startDate">Start Date</label>
    <input type="text" placeholder="MM/dd/YYYY" name="startDate" class="form-control" id="startDate" value="<c:if test="${!empty(foodTruckRequest)}"><joda:format pattern="MM/dd/YYYY" value="${foodTruckRequest.startTime}"/></c:if>"/>
    <p class="help-block hidden" id="startDateHelp">Start date must be specified and in the form MM/dd/YYYY (e.g. 08/11/2014)</p>
  </div>
  <div class="form-group">
    <label for="endDate">End Date</label>
    <input type="text" placeholder="MM/dd/YYYY" name="endDate" class="form-control" id="endDate" value="<c:if test="${!empty(foodTruckRequest)}"><joda:format pattern="MM/dd/YYYY" value="${foodTruckRequest.endTime}"/></c:if>"/>
    <p class="help-block hidden" id="endDateHelp">End date must be specified and equal to or greater than start date</p>

  </div>
  <div class="form-group">
    <label for="requester">Contact Name</label>
    <input type="text" placeholder="Your first and last name" name="requester" class="form-control" id="requester" value="<c:if test="${!empty(foodTruckRequest)}">${foodTruckRequest.requester}</c:if>"/>
    <p class="help-block hidden" id="requesterHelp">Contact name is required</p>
  </div>
  <div class="form-group">
    <label for="expectedGuests">Expected number of guests</label>
    <input type="text" name="expectedGuests" id="expectedGuests" class="form-control" value="<c:if test="${!empty(foodTruckRequest)}">${foodTruckRequest.expectedGuests}</c:if>"/>
    <p class="help-block hidden" id="expectedGuestsHelp">Please specify the number of expected guests</p>
  </div>

  <div class="radio">
    <label>
      <input type="radio" name="prepaid" id="prepaid" value="prepaid" <c:if test="${empty(foodTruckRequest) || foodTruckRequest.prepaid}">checked</c:if>>
      All food is paid for in advance
    </label>
  </div>
  <div class="radio">
    <label>
      <input type="radio" name="prepaid" id="notprepaid" value="notprepaid" <c:if test="${!empty(foodTruckRequest) and !foodTruckRequest.prepaid}">checked</c:if>>
      Food will be bought at event
    </label>
  </div>

  <div class="form-group">
    <label for="address">Address</label>
    <input type="text" placeholder="123 Main Street, Chicago, IL" name="address" id="address" class="form-control"  <c:if test="${!empty(foodTruckRequest) and foodTruckRequest.location.resolved}">value="${foodTruckRequest.location.name}"</c:if>/>
  </div>
  <div class="form-group">
    <label for="phone">Phone</label>
    <input type="text" placeholder="XXX-XXX-XXXX" name="phone" id="phone" class="form-control" <c:if test="${!empty(foodTruckRequest)}">value="${foodTruckRequest.phone}"</c:if>/>
  </div>
  <div class="form-group">
    <label for="description">Description</label>
    <textarea name="description" id="description" class="form-control"><c:if test="${!empty(foodTruckRequest)}">${foodTruckRequest.description}</c:if></textarea>
    <p class="help-block hidden" id="descriptionHelp">Description is required</p>
  </div>

  <button id="submitButton" type="submit" class="btn btn-primary">Submit</button>
</form>
<%@include file="include/core_js.jsp" %>
<script type="text/javascript">
  (function() {
    function parseDate(dateStr) {
      var parts = dateStr.split('/');
      if (parts.length != 3) {
        return null;
      }
      var d = new Date(parseInt(parts[2]), parseInt(parts[0]) - 1, parseInt(parts[1]));
      return  isNaN(d.getTime()) ? null : d;
    }
    var errors = 0;

    function assertCondition(condition, controlName) {
      if (!condition) {
        $("#" + controlName + "Help").removeClass("hidden");
        $("#" + controlName).parent().addClass("has-error")
        errors = errors + 1;
      } else {
        $("#" + controlName + "Help").addClass("hidden");
        $("#" + controlName).parent().removeClass("has-error");
      }
    }
    $("#submitButton").click(function(e) {
      e.preventDefault();
      var startDate = parseDate($("#startDate").val()),
          yesterday = new Date().getTime() - 86400000,
          endDate = parseDate($("#endDate").val()),
          contactName = $("#requester").val(),
          address = $("#address").val(),
          eventName = $("#eventName").val();
          expectedGuests = $("#expectedGuests").val(),
          description = $("#description").val();
      errors = 0;
      assertCondition(startDate != null && (startDate.getTime() > yesterday), "startDate");
      assertCondition(endDate != null && (endDate.getTime() >= startDate.getTime()), "endDate");
      assertCondition(contactName.length > 0, "requester");
      assertCondition(description.length > 0, "description");
      assertCondition(expectedGuests.length > 0 && !isNaN(parseInt(expectedGuests)), "expectedGuests");
      assertCondition(eventName.length > 0, "eventName");
      if (errors == 0) {
        $("form").submit();
      }
    });
  })();
</script>

<%@ include file="footer.jsp" %>
