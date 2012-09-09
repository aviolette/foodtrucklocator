<%@include file="dashboardHeader.jsp" %>

<form method="post">
  <fieldset>
    <div class="clearfix">
      <label>&nbsp;</label>

      <div class="input">
        <ul class="unstyled">
          <li><label><input name="googleGeolocationEnabled"
                            <c:if test="${config.googleGeolocationEnabled}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Google Geolocation</span></label></li>
          <li><label><input name="yahooGeolocationEnabled"
                            <c:if test="${config.yahooGeolocationEnabled}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Yahoo Placefinder</span></label></li>
          <li><label><input name="tweetUpdateServletEnabled"
                            <c:if
                                test="${config.tweetUpdateServletEnabled}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Remote Tweet Updating</span></label></li>
        </ul>
      </div>
    </div>
  </fieldset>
  <input type="submit" class="btn primary" value="Save"/>
</form>
<%@include file="dashboardFooter.jsp" %>