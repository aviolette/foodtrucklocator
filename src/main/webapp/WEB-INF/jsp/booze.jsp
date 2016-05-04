<%@ include file="header.jsp" %>

<c:choose>
    <c:when test="${empty(boozyDate)}">
     <h1>Boozy Stops</h1>
    <p>These are the upcoming food truck events that blend booze and food trucks.</p>
    </c:when>
    <c:otherwise>
        <h1>Boozy Stops for <joda:format pattern="EEE MMM dd" value="${boozyDate}"/></h1>
    </c:otherwise>
</c:choose>

<c:forEach items="${daySchedules}" var="schedule" varStatus="counter">
  <c:if test="${(counter.index mod 3) == 0}">
    <div class="row">
  </c:if>

  <c:forEach items="${schedule.groups}" var="group" varStatus="status">
    <div class="col-md-4">
    <div class="panel panel-default">
      <div class="panel-heading"><c:if test="${empty(boozyDate)}"><a href="/booze?date=<joda:format value="${schedule.day}" pattern="yyyyMMdd"/>"></c:if><joda:format pattern="EEE MMM dd" value="${schedule.day}"/><c:if test="${empty(boozyDate)}"></a></c:if>
      </div>
      <div class="panel-body">


        <c:forEach items="${group.stops}" var="stop" varStatus="stopStatus">
          <c:if test="${stopStatus.index == 0}">
            <h3><ftl:location location="${group.location}" at="${stop.startTime}"/></h3>
          </c:if>
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




      </div>
    </div>
    </div>
  </c:forEach>



  <c:if test="${(counter.index % 3) == 0}">
    </div>
  </c:if>
</c:forEach>

<%@include file="include/core_js.jsp" %>

<%@ include file="footer.jsp" %>
