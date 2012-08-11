<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>

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

<script type="text/javascript">
  function refreshList() {
    $addressRules = $("#addressRules");
    $addressRules.empty();
  }

  $(document).ready(function() {
    var stop = {pattern : $("#name").attr("value")}
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
    })
    $("#addressRuleModalButton").click(function(e) {
      $('#addressRuleModal').modal('show')
    })
   });
</script>

<%@include file="dashboardFooter.jsp" %>