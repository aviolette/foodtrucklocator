<%@ include file="header.jsp" %>
<style>
  .panel-heading {
    font-size: 150%;
    font-weight: bold;
  }
  h3 {
    margin-top: 0;
  }
</style>
<div class="row">
  <div class="col-md-12">
<c:choose>
  <c:when test="${empty(boozyDate)}">
    <h1>Boozy Stops</h1>
    <p class="lead">These are the upcoming food truck events that blend booze and food trucks.</p>
  </c:when>
  <c:otherwise>
    <h1>Boozy Stops for <joda:format pattern="EEE MMM dd" value="${boozyDate}"/></h1>
  </c:otherwise>
</c:choose>
  </div>
</div>

<c:forEach items="${allGroups}" var="dayGroup" varStatus="dayGroupStatus">
  <h2 class="date-header"><joda:format pattern="EEEE, MMMM dd" value="${dayGroup.day}"/></h2>
<c:forEach items="${dayGroup.groups}" var="group" varStatus="status">
  <c:if test="${(status.index mod 2) == 0}">
    <div class="row" style="margin-top:40px">
  </c:if>
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-body">
        <div class="row">
          <div class="col-md-4">
            <img src="${group.location.imageUrl.protocolRelative}" height="150" width="150"/>
          </div>
          <div class="col-md-8">
            <c:forEach items="${group.stops}" var="stop" varStatus="stopStatus">
              <c:if test="${stopStatus.index == 0}">
                <h3><ftl:location location="${group.location}" at="${stop.startTime}"/></h3>
                <p>${group.location.description}</p>
              </c:if>
              <div class="media">
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
    </div>
  </div>
  <c:if test="${(status.index % 2) == 1 or status.last}">
    </div>
  </c:if>
</c:forEach>
</c:forEach>



<%@include file="include/core_js.jsp" %>

<%@ include file="footer.jsp" %>
