<%@ include file="header.jsp" %>

<p class="lead">These are businesses owned by past and present food truck owners.</p>

<ol>
<c:forEach var="venue" items="${locations}">
<li><div><a href="/locations/${venue.location.key}">${venue.location.name}</a></div>
    <div>Owned by: ${venue.truck.name}</div>
    <div>${venue.location.description}</div>
</li>
</c:forEach>
</ol>
<%@include file="include/core_js.jsp" %>

<%@ include file="footer.jsp" %>
