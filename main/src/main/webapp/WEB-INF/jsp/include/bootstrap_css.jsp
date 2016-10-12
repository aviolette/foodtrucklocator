<%@include file="../common.jsp" %>
<c:choose>
  <c:when test="${localFrameworks}">
    <link rel="stylesheet" href="/bootstrap3.0.3/css/bootstrap.min.css"/>
  </c:when>
  <c:otherwise>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">
  </c:otherwise>
</c:choose>
