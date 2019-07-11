<%@ include file="header.jsp" %>
<style>
  h3 {
    margin-top: 0;
  }
</style>
<div class="row">
  <div class="col-md-12">
<c:choose>
  <c:when test="${empty(boozyDate)}">
    <h1>Event Schedule</h1>
    <p class="lead">These are upcoming food truck events at Chicago-land breweries and tap-rooms.</p>
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
    <div class="row">
  </c:if>
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-body">
        <div class="row mt-3">
          <div class="col-lg-4 col-sm-5">
            <img src="${group.location.imageUrl.protocolRelative}"  width="100%"/>
          </div>
          <div class="col-lg-8 col-sm-7">
            <c:forEach items="${group.stops}" var="stop" varStatus="stopStatus">
              <c:if test="${stopStatus.index == 0}">
                <h3><ftl:location location="${group.location}" at="${stop.startTime}"/></h3>
                <p>${group.location.description}</p>
              </c:if>

              <h4><joda:format value="${stop.startTime}" style="-S"/> - <joda:format value="${stop.endTime}" style="-S"/></h4>
              <div class="row">

              <c:forEach items="${stop.trucks}" var="truck">
                <div class="col-md-6">

                  <div class="thumbnail mt-2"><img width="100"  src="${truck.previewIconUrl.protocolRelative}"
                                              alt="" class="img-rounded"/>
                  </div>
                </div>
              </c:forEach>
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
