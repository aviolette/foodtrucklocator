<%@include file="../common.jsp" %>


<dl>
  <c:if test="${isAdmin}">
    <dt>Admin Page</dt>
    <dd><a href="/admin/events/${event.key}">Go to admin page</a></dd>
  </c:if>
  <dt>When</dt>
  <dd>
    <c:choose>
      <c:when test="${event.startAndEndOnSameDay}">
        <joda:format value="${event.startTime}" pattern="MMM dd"/> from
        <joda:format value="${event.startTime}" pattern="hh:mm a"/> to
        <joda:format value="${event.endTime}" pattern="hh:mm a"/>
      </c:when>
      <c:otherwise>
        <joda:format value="${event.startTime}" pattern="MMM dd hh:mm a"/> to
        <joda:format value="${event.endTime}" pattern="MMM dd hh:mm a"/>
      </c:otherwise>
    </c:choose>
  </dd>
  <dt>Where</dt>
  <dd>
    <address>${event.location.name}</address>
  </dd>
  <c:if test="${!empty(event.url)}">
    <dt>Url</dt>
    <dd><a href="${event.url}">${event.url}</a></dd>
  </c:if>
  <dt>Trucks</dt>
  <dd><c:forEach items="${event.trucks}" var="truck" varStatus="truckStatus">
    <a href="/trucks/${truck.id}">${truck.name}</a><c:if test="${!truckStatus.last}">, </c:if>
  </c:forEach></dd>
</dl>
<br/>

<div>
  ${event.description}
</div>
