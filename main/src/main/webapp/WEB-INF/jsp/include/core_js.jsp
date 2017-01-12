<%@include file="../common.jsp" %>

<c:choose>
  <c:when test="${localFrameworks}">
    <script src='/script/lib/jquery-1.10.2.min.js'></script>
    <script src="/bootstrap3.0.3/js/bootstrap.min.js"></script>
    <script src="/script/lib/bootstrap-toggle.js"></script>
  </c:when>
  <c:otherwise>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
    <script>window.jQuery ||
    document.write("<script src='/script/lib/jquery-1.10.2.min.js'>\x3C/script>")</script>
    <script src="//netdna.bootstrapcdn.com/bootstrap/3.0.3/js/bootstrap.min.js"></script>
    <script src="https://gitcdn.github.io/bootstrap-toggle/2.2.2/js/bootstrap-toggle.min.js"></script>
  </c:otherwise>
</c:choose>
