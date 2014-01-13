<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>
<form method="GET" action="">
  <fieldset>
    <div>
      <label for="locationSearch">Location</label>

      <div class="controls">
        <div class="input-prepend">
          <span class="add-on"><i class="icon-search"></i></span>
          <input id="locationSearch" class="span6" name="q" utocomplete="off" type="text" value="" data-provide="typeahead"
                 data-items="4"/>
          <input type="submit" class="btn primary" value="Search" title="Search"/>
        </div>
      </div>
    </div>
  </fieldset>
</form>
<script type="text/javascript">
  $("#locationSearch").typeahead({source:${locations}});
</script>
<%@include file="dashboardFooter.jsp" %>