<%@ include file="header.jsp" %>

<div class="row top-row">
  <div class="col-md-12">
    <h1>Popular Locations</h1>
  </div>
</div>

<c:forEach items="${locations}" var="location" varStatus="locationStatus">
  <c:if test="${locationStatus.index % 4 == 0}"><c:if test="${locationStatus.index !=0 }"></div></c:if><div class="row"></c:if>
  <div class='col-xs-6 col-md-3 text-center'>
    <a href='/locations/${location.key}'>
      <img width='180' height='180' class='img-rounded' src='${location.imageUrl.protocolRelative}'/>
      <div><strong>${location.name}</strong></div>
    </a>
  </div>
</c:forEach>
</div>
<%@include file="include/core_js.jsp" %>

<%@ include file="footer.jsp" %>