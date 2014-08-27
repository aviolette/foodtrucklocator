<%@ include file="header.jsp" %>

<c:forEach items="${daySchedules}" var="schedule">
  <h2><joda:format pattern="EEE MMM dd" value="${schedule.day}"/></h2>
  <c:forEach items="${schedule.groups}" var="group" varStatus="status">
    <h3><ftl:location location="${group.location}"/></h3>
    <c:forEach items="${group.stops}" var="stop">
    <div class="media">
      <a class="pull-left" href="/trucks/${stop.truck.id}">
        <img class="media-object" src="${stop.truck.iconUrl}" alt="${stop.truck.name} icon"/>
      </a>
      <div class="media-body">
        <a href="/trucks/${stop.truck.id}" class="truckLink">
          <h4 class="media-heading">${stop.truck.name}</h4>
          <joda:format value="${stop.startTime}" style="-S"/> - <joda:format value="${stop.endTime}" style="-S"/>
        </a>
      </div>
    </div>
    </c:forEach>

  </c:forEach>
</c:forEach>

<%@ include file="footer.jsp" %>
