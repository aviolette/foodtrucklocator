<%@ include file="header.jsp" %>

<div class="row top-row">
  <div class="col-md-12">
    <h1>Popular Locations</h1>
  </div>
</div>

<div class="row">
  <c:forEach items="${locations}" var="location" varStatus="locationStatus">
    <div class='col-xs-6 col-md-2'>
      <a href='/locations/${location.key}'>
        <img width='180' height='180' src='${location.imageUrl}'/>
        <p class='text-center'><strong>${location.name}</strong></p>
      </a>
    </div>
  </c:forEach>
</div>

<%@ include file="footer.jsp" %>