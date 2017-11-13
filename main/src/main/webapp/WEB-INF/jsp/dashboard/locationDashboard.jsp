<%@include file="dashboardHeaderBS3.jsp" %>

<div class="row">
  <div class="col-md-12">
    <form method="GET" action="">
      <div class="form-group">
        <label for="locationSearch">Location</label>
        <div class="input-group">
          <span class="input-group-addon"><span class="glyphicon glyphicon-search"></span></span>
          <input id="locationSearch" class="form-control" name="q" type="text"/>
        <span class="input-group-btn">
          <input type="submit" class="btn btn-default" value="Search" title="Search"/>
        </span>
        </div>
      </div>
    </form>
  </div>
</div>

<ul class="list-unstyled">
  <c:forEach var="loc" items="${allLocations}">
    <li><ftl:location admin="true" location="${loc}"/></li>
  </c:forEach>
</ul>
<%@include file="dashboardFooterBS3.jsp" %>