<%@include file="../common.jsp" %>
<c:choose>
  <c:when test="${localFrameworks}">
    <script src="/script/lib/modernizr-1.7.min.js"></script>
  </c:when>
  <c:otherwise>
    <script src="//storage.googleapis.com/ftf_static/script/modernizr-1.7.min.js"></script>
  </c:otherwise>
</c:choose>
