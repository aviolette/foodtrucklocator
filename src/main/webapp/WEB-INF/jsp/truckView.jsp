<%@ include file="header.jsp" %>

<h1>${truck.name}</h1>


${truck.foursquareUrl}

<!--
private final String id;
private final String name;
private final String twitterHandle;
private final String url;
private final String iconUrl;
private final Set<String> categories;
private final String description;
private final String foursquareUrl;
private final boolean twittalyzer;
private final String defaultCity;
private final String facebook;
private final Pattern matchOnlyIf;
private final Pattern donotMatchIf;
private final boolean inactive;
private @Nullable final String calendarUrl;
private final @Nullable String email;
private final @Nullable String phone;
private final boolean twitterGeolocationDataValid;
-->

<h2>Schedule</h2>

<table>
  <thead>
    <tr><th>Day</th><th>This Week</th><th>Last Week</th></tr>
  </thead>
  <tbody>
  <c:forEach items="${schedule}" var="day">
    <tr><td>${day.name}</td><td><c:if test="${!empty(day.current)}">
      <c:forEach items="${day.current.stops}" var="stop">
        ${stop.location.name}&nbsp;
      </c:forEach>
    </c:if>&nbsp;</td><td><c:if test="${!empty(day.prior)}">
      <c:forEach items="${day.prior.stops}" var="stop">
       ${stop.location.name}&nbsp;
      </c:forEach>
    </c:if>&nbsp;</td></tr>
  </c:forEach>


  </tbody>
</table>

<h2>Tweets</h2>

<%@ include file="footer.jsp" %>
