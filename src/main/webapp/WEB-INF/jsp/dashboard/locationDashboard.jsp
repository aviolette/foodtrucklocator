<%@include file="../common.jsp"%>
<%@include file="dashboardHeader.jsp" %>
<form method="GET" action="">
  <fieldset>
    <div>
      <label for="locationSearch">Location</label>
      <div class="input">
        <input id="locationSearch" class="xlarge" name="searchfield" size="30" type="text"/>
        <input type="submit" class="btn primary" value="Search" title="Search"/>
      </div>
    </div>
  </fieldset>
</form>

<h3>Results</h3>
<ul id="results">

</ul>

<script type="text/javascript">
  var resultsJson = ${results};
  var $resultList = $(results);
  $.each(resultsJson, function(idx, result) {
    $resultList.append("<li><a href='/admin/locations/" + result.key + "'>" + result.name + "</a></li>");
  });
</script>
<%@include file="dashboardFooter.jsp" %>