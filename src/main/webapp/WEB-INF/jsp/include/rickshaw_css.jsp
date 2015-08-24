<%@include file="../common.jsp" %>
<c:choose>
  <c:when test="${localFrameworks}">
    <link href="/css/rickshaw/rickshaw.min.css" rel="stylesheet">
  </c:when>
  <c:otherwise>
    <link href="//storage.googleapis.com/ftf_static/css/rickshaw.min.css" rel="stylesheet">
  </c:otherwise>
</c:choose>
