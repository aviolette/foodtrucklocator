<%@include file="dashboardHeaderBS3.jsp" %>

<c:choose>
  <c:when test="${syncEnabled}">
    <form method="POST" action="">
      <div class="checkbox">
        <label><input name="trucks"
                      type="checkbox"/> <span>Download trucks</span></label>
      </div>
      <div class="checkbox">
        <label><input name="schedule"
                      type="checkbox"/> <span>Download schedule</span></label>
      </div>
      <button type="submit" class="btn btn-primary">Sync</button>
    </form>
  </c:when>
  <c:otherwise>
    <script>
      flash("Sync is disabled and should never enabled in production. To enable, set the sync URL in the configuration settings", "warning");
    </script>
  </c:otherwise>
</c:choose>

<%@include file="dashboardFooterBS3.jsp" %>
