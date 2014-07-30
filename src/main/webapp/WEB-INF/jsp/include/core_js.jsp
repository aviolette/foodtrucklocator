<%@include file="../common.jsp" %>

<c:choose>
  <c:when test="${localFrameworks}">
    <script src="/bootstrap3.0.3/js/bootstrap.min.js"></script>
    <script src='/script/lib/jquery-1.10.2.min.js'></script>
  </c:when>
  <c:otherwise>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
    <script>window.jQuery ||
    document.write("<script src='/script/lib/jquery-1.10.2.min.js'>\x3C/script>")</script>
    <script src="//netdna.bootstrapcdn.com/bootstrap/3.0.3/js/bootstrap.min.js"></script>
  </c:otherwise>
</c:choose>
