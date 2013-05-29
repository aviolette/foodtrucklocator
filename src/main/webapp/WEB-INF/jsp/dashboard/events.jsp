<%@include file="dashboardHeader.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a href="/admin/events/new" class="btn" id="newEvent">New Event</a>
  </div>
</div>

<h2>Events</h2>

<ul>
  <c:forEach items="${events}" var="event">
    <li><a href="/admin/events/${event.key}">${event.name}</a></li>
  </c:forEach>
</ul>


<%@include file="dashboardFooter.jsp" %>
