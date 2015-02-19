<%@include file="dashboardHeader.jsp" %>

<form method="post">
  <fieldset>
    <legend>General</legend>
    <div class="clearfix">
      <label for="mapCenter">Map Center</label>
      <input class="span6" type="text" value="${config.center.name}" name="mapCenter" id="mapCenter"/>
    </div>
    <div>
      <div class="input">
        <ul class="unstyled">
          <li><label><input name="scheduleCachingOn"
                            <c:if test="${config.scheduleCachingOn}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Schedule Caching in memcached for 5 minutes</span></label></li>
          <li><label><input name="showPublicTruckGraphs"
                            <c:if test="${config.showPublicTruckGraphs}">checked="checked"</c:if>
                            type="checkbox"/> <span>Show graphs on truck pages</span></label></li>
          <li><label><input name="autoOffRoad"
                            <c:if test="${config.autoOffRoad}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable ability for twittalyzer to take trucks off road automatically</span></label></li>
          <li><label><input name="foodTruckRequestOn"
                            <c:if
                                test="${config.foodTruckRequestOn}">checked="checked"</c:if>
                            type="checkbox"/> <span>Show food truck request button on trucks page</span></label></li>
          <li><label><input name="sendNotificationTweetWhenNoTrucks" <c:if test="${config.sendNotificationTweetWhenNoTrucks}">checked="checked"</c:if> type="checkbox"> <span>Send out notifications if no trucks at a stop.</span></label></li>
          <li><label><input name="retweetStopCreatingTweets"
                            <c:if test="${config.retweetStopCreatingTweets}">checked="checked"</c:if>
                            type="checkbox"/> <span>Retweet Stop-creating Tweets</span></label></li>

        </ul>
      </div>
    </div>
    <legend>Front Door App Key</legend>
    <div class="clearfix">
      <label for="frontDoorAppKey">Front Door App Key</label>
      <input class="span6" type="text" value="${config.frontDoorAppKey}" name="frontDoorAppKey" id="frontDoorAppKey"/>
    </div>
  </fieldset>
  <fieldset>
    <legend>Notifications</legend>
    <div class="clearfix">
      <label for="notificationSender">Email of sender</label>
      <input class="span6" type="text" value="${config.notificationSender}" name="notificationSender" id="notificationSender"/>
    </div>
    <div class="clearfix">
      <label for="notificationReceivers">List of notification receivers</label>
      <input class="span6" type="text" value="${config.notificationReceivers}" name="notificationReceivers" id="notificationReceivers"/>
    </div>
  </fieldset>
  <fieldset>
    <legend>Google Calendar</legend>
    <div class="clearfix">
      <label for="calendarUrl">Google Calendar URL</label>
      <input class="span6" type="text" value="${config.googleCalendarAddress}" name="googleCalendarAddress" id="calendarUrl"/>
    </div>
  </fieldset>
  <fieldset>
    <legend>Sync</legend>
    <p>These parameters should not be used in a production environment.  They allow you to sync data from an up-stream food truck finder app.  You can use it to pull data from your production environment into a dev environment.</p>
    <div class="clearfix">
      <label for="syncUrl">Sync URL</label>
      <input class="span6" type="text" value="${config.syncUrl}" name="syncUrl" id="syncUrl"/>
    </div>
    <div class="clearfix">
      <label for="syncAppKey">Sync App Key</label>
      <input class="span6" type="text" value="${config.syncAppKey}" name="syncAppKey" id="syncAppKey"/>
    </div>
  </fieldset>
  <fieldset>
    <legend>Geolocation</legend>
    <div>
      <p>You will need to have one of these checked. In Dev, it is ok to just have Google checked, but this doesn't always work in prod since AppEngine uses a shared IP pool and you will run into query limits, so it is suggested that you also use YQL Geolocation too.</p>
      <div class="input">
        <ul class="unstyled">
          <li><label><input name="googleGeolocationEnabled"
                            <c:if test="${config.googleGeolocationEnabled}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Google Geolocation</span></label></li>
          <li><label><input name="yahooGeolocationEnabled"
                            <c:if test="${config.yahooGeolocationEnabled}">checked="checked"</c:if>
                            type="checkbox"/> <span>Enable Yahoo YQL Geolocation</span></label></li>
        </ul>
      </div>
      <div class="clearfix">
        <label for="calendarUrl">Yahoo App Key</label>
        <p>Get a Yahoo App Key <a href="https://developer.yahoo.com/wsregapp/index.php">here</a></p>
        <input class="span6" type="text" value="${config.yahooAppId}" name="yahooAppId" placeholder="Yahoo APP ID is required to use YQL" id="yahooAppId"/>
      </div>
    </div>
  </fieldset>
  <fieldset>
    <legend>Twitter</legend>
    <div>
      <div class="input">
        <ul class="unstyled">
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