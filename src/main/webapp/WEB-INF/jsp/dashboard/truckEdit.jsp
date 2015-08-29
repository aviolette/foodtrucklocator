<%@ include file="dashboardHeaderBS3.jsp" %>


<form action="" method="POST" id="theForm">
  <div class="form-group">
    <label for="truckName">Name:</label>
    <input type="text" class="form-control" id="truckName" name="name" value="${truck.name}"/>
  </div>
  <div class="form-group">
    <label for="twitterHandle">Twitter Handle:</label>
    <input type="text" class="form-control" id="twitterHandle" name="twitterHandle"
           value="${truck.twitterHandle}"/>
  </div>
  <div class="form-group">
    <label for="url">Url:</label>
    <input type="text" class="form-control" id="url" name="url" value="${truck.url}"/>
  </div>
  <div class="form-group">
    <label for="description">Description</label>
    <input type="text" class="form-control" id="description" name="description"
           value="${truck.description}"/>
  </div>
  <div class="form-group">
    <label for="foursquareUrl">Foursquare ID</label>
    <input type="text" class="form-control" id="foursquareUrl" name="foursquareUrl"
           value="${truck.foursquareUrl}"/>
  </div>
  <div class="form-group">
    <label for="yelp">Yelp Slug</label>
    <input type="text" class="form-control" id="yelp" name="yelp"
           value="${truck.yelpSlug}"/>
  </div>
  <div class="form-group">
    <label for="facebook">Facebook URI</label>
    <input type="text" class="form-control" id="facebook" name="facebook"
           value="${truck.facebook}"/>
  </div>
  <div class="form-group">
    <label for="facebookPageId">Facebook PageId</label>
    <input type="text" class="form-control" id="facebookPageId" name="facebookPageId"
           value="${truck.facebookPageId}"/>
  </div>
  <div class="form-group">
    <label for="instagramId">Instagram Id</label>
    <input type="text" class="form-control" id="instagramId" name="instagramId"
           value="${truck.instagramId}"/>
  </div>
  <div class="form-group">
    <label for="email">Email</label>
    <input type="text" class="form-control" id="email" name="email" value="${truck.email}"/>
  </div>
  <div class="form-group">
    <label for="phone">Phone</label>
    <input type="text" class="form-control" id="phone" name="phone" value="${truck.phone}"/>
  </div>
  <div class="form-group">
    <label for="iconUrl">Icon URL:</label>
    <input type="text" class="form-control" id="iconUrl" name="iconUrl"
           value="${truck.iconUrl}"/>
  </div>
  <div class="form-group">
    <label for="iconUrl">Preview Image URL:</label>
    <input type="text" class="form-control" id="previewIcon" name="previewIcon"
           value="${truck.previewIcon}"/>
  </div>
  <div class="form-group">
    <label for="iconUrl">Fullsize Image URL:</label>
    <input type="text" class="form-control" id="fullsizeImage" name="fullsizeImage"
           value="${truck.fullsizeImage}"/>
  </div>
  <div class="form-group">
    <label for="calendarUrl">Calendar URL</label>
    <input type="text" class="form-control" id="calendarUrl" name="calendarUrl"
           value="${truck.calendarUrl}"/>
  </div>
  <div class="form-group">
    <label for="timezoneAdjustment">Timezone adjustment</label>
    <input type="text" class="form-control" id="timezoneAdjustment" name="timezoneAdjustment"
           value="${truck.timezoneAdjustment}"/>
  </div>
  <div class="form-group">
    <label for="defaultCity">Default City</label>
    <input type="text" class="form-control" id="defaultCity" name="defaultCity"
           value="${truck.defaultCity}"/>
  </div>
  <div class="form-group">
    <label for="matchOnlyIf">Match-only Regex</label>
    <input type="text" class="form-control" id="matchOnlyIf" name="matchOnlyIf"
           value="${truck.matchOnlyIfString}"/>
  </div>
  <div class="form-group">
    <label for="donotMatchIf">Donot Match Regex</label>
    <input type="text" class="form-control" id="donotMatchIf" name="donotMatchIf"
           value="${truck.donotMatchIfString}"/>
  </div>
  <div class="form-group">
    <label for="categories">Categories</label>
    <input type="text" class="form-control" id="categories" name="categories"
           value="${truck.categoryList}"/>
  </div>
  <div class="form-group">
    <label for="beaconnaiseEmails">Beaconnaise Emails</label>
    <input type="text" class="form-control" id="beaconnaiseEmails" name="beaconnaiseEmails"
           value="${truck.beaconnaiseList}"/>
  </div>

  <div class="checkbox">
    <label><input type="checkbox" name="options"
                  value="twittalyzer" ${truck.usingTwittalyzer ? "checked='checked'" : ""}/> Use twittalyzer</label>
  </div>
  <div class="checkbox">
    <label><input type="checkbox" name="options"
                  value="facebooker" ${truck.scanFacebook ? "checked='checked'" : ""}/> Use facebooker</label>
  </div>
  <div class="checkbox">
    <label><input type="checkbox" name="options"
                  value="systemNotifications" ${truck.allowSystemNotifications ? "checked='checked'" : ""}/> Allow System Notifications</label>
  </div>
  <div class="checkbox">
    <label><input type="checkbox" name="options"
                  value="displayEmailPublicly" ${truck.displayEmailPublicly ? "checked='checked'" : ""}/>
      Display email publicly</label>
  </div>
  <div class="checkbox">
    <label><input type="checkbox" name="options"
                  value="twitterGeolocation" ${truck.twitterGeolocationDataValid ? "checked='checked'" : ""}/>
      Can Use Twitter Geolocation Data</label>
  </div>
  <div class="checkbox">
    <label><input type="checkbox" name="options"
                  value="inactive" ${truck.inactive ? "checked='checked'" : ""}/>
      Inactive</label>
  </div>
  <div class="checkbox">
    <label><input type="checkbox" name="options"
                  value="hidden" ${truck.hidden ? "checked='checked'" : ""}/>
      Truck is hidden from public listings</label>


  </div>

</form>
<button id="submitButton" class="btn btn-primary">Save</button>
<button id="updateStats" class="btn btn-warning">Update Stats</button>
<button id="syncProfile" class="btn btn-warning">Sync Profile</button>
<button id="deleteTruck" class="btn btn-danger">DELETE THIS TRUCK</button>

<script>
  $("#submitButton").click(function(e) {
    $("#theForm").submit();
  });
  $("#updateStats").click(function(e) {
    e.preventDefault();
    $.ajax({
      url: '/cron/updateTruckStats?truckId=${truck.id}&force=true',
      complete: function() {
        location.href='/trucks/${truck.id}';
      }
    });
  });
  $("#syncProfile").click(function(e) {
    e.preventDefault();
    $.ajax({
      url: '/cron/profileSync?truckId=${truck.id }',
      type: 'POST',
      complete: function() {
        location.href='/trucks/${truck.id}';
      }
    });
  });
  $("#deleteTruck").click(function () {
    if (confirm("ARE YOU SURE?")) {
      $.ajax({
        url: "/services/trucks/${truck.id}",
        type: "DELETE",
        success: function () {
          location.href = '/admin/trucks';
        }
      });
    }
  });
</script>

<%@ include file="dashboardFooterBS3.jsp" %>