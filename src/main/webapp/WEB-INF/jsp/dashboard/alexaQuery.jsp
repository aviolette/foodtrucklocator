<%--suppress CheckTagEmptyBody --%>
<%@include file="dashboardHeaderBS3.jsp" %>

<h1>Alexa Logs</h1>

<div class="row">
  <div class="col-md-12">
    <form class="form-inline" method="GET" action="">
      <div class="form-group">
        <label for="intentName">Intent</label>
        <select class="form-control" name="intentName" id="intentName">
          <c:forEach items="${intentNames}" var="intent">
            <option <c:if test="${intentName == intent}">selected</c:if>>${intent}</option>
          </c:forEach>
        </select>
      </div>
    </form>
  </div>
</div>

<div class="row" style="margin-top:10px">
  <div class="col-md-12">


    <table class="table">
      <thead>
      <tr>
        <th>Time</th>
        <c:forEach items="${slots}" var="slot">
          <th>${slot}</th>
        </c:forEach>
        <th>Reprompt</th>
        <th>Had Card</th>
      </tr>
      </thead>
      <tbody>
      <c:forEach items="${alexaResults}" var="intent">
        <tr>
          <td><joda:format value="${intent.requestTime}" style="MM"/></td>
          <c:forEach items="${intent.slotEntries}" var="slotEntry" varStatus="status">
            <%--suppress CheckTagEmptyBody --%>
            <c:choose>
              <c:when test="${intentName == 'GetFoodTrucksAtLocation' && status.first}">
                <c:url value="/admin/locations" var="locationUrl">
                  <c:param name="q" value="${slotEntry}"/>
                </c:url>
                <td><a href="${locationUrl}">${slotEntry}</a></td>
              </c:when>
              <c:otherwise>
                <td>${slotEntry}</td>
              </c:otherwise>
            </c:choose>
          </c:forEach>
          <td>${intent.hadReprompt}</td>
          <td>${intent.hadCard}</td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </div>
</div>


<script type="text/javascript">
  $("#intentName").change(function () {
    $("form").submit();
  });
</script>

<%@include file="dashboardFooterBS3.jsp" %>