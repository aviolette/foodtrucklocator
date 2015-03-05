<%@include file="dashboardHeader.jsp" %>

<form method="post">
  <fieldset>
    <legend>General</legend>
    <div class="clearfix">
      <label for="mapCenter">Map Center</label>
      <input class="span6" type="text" value="${config.center.name}" name="mapCenter" id="mapCenter"/>
    </div>
    <div>
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
  <input type="submit" class="btn primary" value="Save"/>
</form>
<%@include file="dashboardFooter.jsp" %>