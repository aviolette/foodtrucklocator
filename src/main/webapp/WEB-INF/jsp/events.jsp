<%@ include file="header.jsp" %>

<c:choose>
  <c:when test="${empty(events)}">
    There are no events.
  </c:when>
  <c:otherwise>
    <c:forEach var="event" items="${events}">
      <h2><a href="/events/${event.key}">${event.name}</a></h2>
      <%@include file="include/eventView.jsp" %>
    </c:forEach>
  </c:otherwise>
</c:choose>

<%@ include file="footer.jsp" %>
