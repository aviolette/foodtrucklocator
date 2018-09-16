<%@include file="../common.jsp" %>
<c:choose>
  <c:when test="${bootstrap4}">
    <link rel="stylesheet" href="//stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css">
  </c:when>
  <c:otherwise>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">
  </c:otherwise>
</c:choose>
