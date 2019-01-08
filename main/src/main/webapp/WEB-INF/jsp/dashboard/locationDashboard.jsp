<%@include file="dashboardHeader1.jsp" %>

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

<div class="row">
<div class="col-md-6 mb-5">
  <div class="list-group mb-3">
    <h6>Popular Lunch Locations</h6>
    <c:forEach var="loc" items="${allLocations}">
      <a class="list-group-item list-group-item-action justify-content-between d-flex" href="/admin/locations/${loc.key}">
        <span>${loc.name}</span>
      </a>
    </c:forEach>
  </div>
</div>
</div>
<%@include file="dashboardFooter1.jsp" %>