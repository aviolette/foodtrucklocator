<%@ include file="header.jsp" %>

<p class="lead">These are businesses owned by past and present food truck owners.</p>
<ol>
<c:forEach var="venue" items="${locations}">
    <li style="padding-bottom:20px"><div><a href="/locations/${venue.location.key}">${venue.location.name}</a></div>
        <div>Associated with truck <a href="/trucks/${venue.truck.id}">${venue.truck.name}</a></div>
        <div>${venue.location.description}</div>
        <c:if test="${!empty(venue.location.url)}">
            <div><a href='${venue.location.url}'>${venue.location.url}</a></div>
        </c:if>
        <c:if test="${!empty(venue.location.twitterHandle)}">
            <div><a href="http://twitter.com/${location.location.twitterHandle}">@${venue.location.twitterHandle}</a></div>
        </c:if>
    </li>
</c:forEach>
</ol>
<%@include file="include/core_js.jsp" %>
<%@ include file="footer.jsp" %>
