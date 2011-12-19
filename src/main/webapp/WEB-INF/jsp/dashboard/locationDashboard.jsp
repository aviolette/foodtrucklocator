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

<%--
<ul class="results">
<c:forEach var="result" items="results">
  <li><a href="">${result.name}</a></li>
</c:forEach>
</ul>

--%>
<%@include file="dashboardFooter.jsp" %>