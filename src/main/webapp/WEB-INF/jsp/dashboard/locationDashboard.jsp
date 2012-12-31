<%@include file="../common.jsp" %>
<%@include file="dashboardHeader.jsp" %>
<form method="GET" action="">
  <fieldset>
    <div>
      <label for="locationSearch">Location</label>

      <div class="input">
        <input id="locationSearch" class="span6" name="q" size="30" type="text"/>
        <input type="submit" class="btn primary" value="Search" title="Search"/>
      </div>
    </div>
  </fieldset>
</form>
<%@include file="dashboardFooter.jsp" %>