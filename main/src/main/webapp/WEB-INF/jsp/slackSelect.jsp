<%@ taglib prefix="o" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>

<p class="lead">
  You're almost finished!  Just select the location where you'd like to receive alerts from.
</p>
<form method="POST" action="">
  <div class="form-group">
    <label for="location">Select a location</label>
    <select name="location" class="form-control" id="location">
      <o:forEach items="${locations}" var="location">
        <option value="${location.key}">${location.name}</option>
      </o:forEach>
    </select>
  </div>
  <button type="submit" class="btn btn-primary mb-2">Complete Setup</button>
</form>


<%@include file="include/core_js.jsp" %>

<%@ include file="footer.jsp" %>