<%@include file="../common.jsp" %>
<c:choose>
  <c:when test="${localFrameworks}">
    <script src="/script/lib/d3.min.js" type="text/javascript"></script>
    <script src="/script/lib/d3.layout.min.js" type="text/javascript"></script>
    <script src="/script/lib/rickshaw.min.js" type="text/javascript"></script>
   </c:when>
  <c:otherwise>
    <script src="//storage.googleapis.com/ftf_static/script/d3.min.js" type="text/javascript"></script>
    <script src="//storage.googleapis.com/ftf_static/script/d3.layout.min.js" type="text/javascript"></script>
    <script src="//storage.googleapis.com/ftf_static/script/rickshaw.min.js" type="text/javascript"></script>
  </c:otherwise>
</c:choose>
<script src="/script/graph.js"></script>
