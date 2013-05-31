<%@include file="../common.jsp" %>

<div>
  <strong>When: </strong><joda:format value="${event.startTime}" pattern="MMM dd, YYYY hh:mm"/> to
  <joda:format value="${event.endTime}" pattern="MMM dd, YYYY hh:mm"/>
</div>
<div>
  <strong>Where: </strong>${event.location.name}
</div>
<c:if test="${!empty(event.url)}">
  <div>
    <strong>Url:</strong> <a href="${event.url}">${event.url}</a>
  </div>
</c:if>
<div>
  <strong>Trucks: </strong><c:forEach items="${event.trucks}" var="truck" varStatus="truckStatus">
  <a href="/trucks/${truck.id}">${truck.name}</a><c:if test="${!truckStatus.last}">, </c:if>
</c:forEach>
</div>
<br/>

<div>
  ${event.description}
</div>
