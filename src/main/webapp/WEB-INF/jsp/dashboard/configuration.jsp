<%@include file="dashboardHeader.jsp" %>

<form method="post">
  <fieldset>
    <legend>General</legend>
    <div class="clearfix">
      <label for="mapCenter">Map Center</label>
      <input class="span6" type="text" value="${config.center.name}" name="mapCenter" id="mapCenter"/>
    </div>
  </fieldset>
  <fieldset>
    <legend>Geolocation</legend>
    <div>
      <div class="input">
        <ul class="unstyled">
          <li><label><input name="googleGeolocationEnabled"
                            <c:if test="${config.googleGeolocationEnabled}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Google Geolocation</span></label></li>
          <li><label><input name="yahooGeolocationEnabled"
                            <c:if test="${config.yahooGeolocationEnabled}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Yahoo Placefinder</span></label></li>
        </ul>
      </div>
    </div>
  </fieldset>
  <fieldset>
    <legend>Twitter</legend>
    <div>
      <div class="input">
        <ul class="unstyled">
          <li><label><input name="tweetUpdateServletEnabled"
                            <c:if
                                test="${config.tweetUpdateServletEnabled}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Remote Tweet Updating</span></label></li>
          <li><label><input name="localTwitterCachingEnabled"
                            <c:if test="${config.localTwitterCachingEnabled}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Local Twitter Caching (recommended)</span></label></li>
          <li><label><input name="remoteTwitterCachingEnabled"
                            <c:if test="${config.remoteTwitterCachingEnabled}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Remote Twitter Caching</span></label></li>

        </ul>
        <div class="clearfix">
          <label for="remoteCache">Remote Cache Address</label>
          <input name="remoteTwitterCacheAddress" id="remoteCache" class="span6"
                 type="text" value="${config.remoteTwitterCacheAddress}"/>
        </div>

      </div>
    </div>
  </fieldset>
  <input type="submit" class="btn primary" value="Save"/>
</form>
<%@include file="dashboardFooter.jsp" %>