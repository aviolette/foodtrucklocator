<%@ include file="dashboardHeader.jsp" %>


<form action="" method="POST">
  <fieldset title="Truck Information">
    <legend>Truck Information</legend>
    <div class="row">
      <div class="span6">
        <div class="clearfix">
          <label for="truckName">Name:</label>

          <div class="input">
            <input type="text" class="input-xlarge" id="truckName" name="name" value="${truck.name}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="twitterHandle">Twitter Handle:</label>

          <div class="input">
            <input type="text" class="input-xlarge" id="twitterHandle" name="twitterHandle"
                   value="${truck.twitterHandle}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="url">Url:</label>

          <div class="input">
            <input type="text" class="input-xlarge" id="url" name="url" value="${truck.url}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="description">Description</label>

          <div class="input">
            <input type="text" class="input-xlarge" id="description" name="description"
                   value="${truck.description}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="foursquareUrl">Foursquare ID</label>

          <div class="input">
            <input type="text" class="input-xlarge" id="foursquareUrl" name="foursquareUrl"
                   value="${truck.foursquareUrl}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="yelp">Yelp Slug</label>

          <div class="input">
            <input type="text" class="input-xlarge" id="yelp" name="yelp"
                   value="${truck.yelpSlug}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="facebook">Facebook URI</label>

          <div class="input">
            <input type="text" class="input-xlarge" id="facebook" name="facebook"
                   value="${truck.facebook}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="facebookPageId">Facebook PageId</label>

          <div class="input">
            <input type="text" class="input-xlarge" id="facebookPageId" name="facebookPageId"
                   value="${truck.facebookPageId}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="email">Email</label>

          <div class="input">
            <input type="text" class="input-xlarge" id="email" name="email" value="${truck.email}"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="phone">Phone</label>

          <div class="input">
            <input type="text" class="input-xlarge" id="phone" name="phone" value="${truck.phone}"/>
          </div>
        </div>
  </fieldset>
  </div>
  <div class="span6">
    <fieldset title="Internal Display and Matching Information">
      <legend>Internal Display and Matching Information</legend>
      <div class="clearfix">
        <label for="iconUrl">Icon URL:</label>

        <div class="input">
          <input type="text" class="input-xlarge span5" class="input-xlarge" id="iconUrl" name="iconUrl"
                 value="${truck.iconUrl}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="iconUrl">Preview Image URL:</label>

        <div class="input">
          <input type="text" class="input-xlarge span5" class="input-xlarge" id="previewIcon" name="previewIcon"
                 value="${truck.previewIcon}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="calendarUrl">Calendar URL</label>

        <div class="input">
          <input type="text" class="input-xlarge span5" id="calendarUrl" name="calendarUrl"
                 value="${truck.calendarUrl}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="defaultCity">Default City</label>

        <div class="input">
          <input type="text" class="input-xlarge" id="defaultCity" name="defaultCity"
                 value="${truck.defaultCity}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="matchOnlyIf">Match-only Regex</label>

        <div class="input">
          <input type="text" class="input-xlarge" id="matchOnlyIf" name="matchOnlyIf"
                 value="${truck.matchOnlyIfString}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="donotMatchIf">Donot Match Regex</label>

        <div class="input">
          <input type="text" class="input-xlarge" id="donotMatchIf" name="donotMatchIf"
                 value="${truck.donotMatchIfString}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="categories">Categories</label>

        <div class="input">
          <input type="text" class="input-xlarge" id="categories" name="categories"
                 value="${truck.categoryList}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="beaconnaiseEmails">Beaconnaise Emails</label>

        <div class="input">
          <input type="text" class="input-xlarge" id="beaconnaiseEmails" name="beaconnaiseEmails"
                 value="${truck.beaconnaiseList}"/>
        </div>
      </div>
      <div class="clearfix">
        <label id="options">Options</label>

        <div class="input">
          <ul class="unstyled">
            <li><label><input type="checkbox" name="options"
                              value="twittalyzer" ${truck.usingTwittalyzer ? "checked='checked'" : ""}/>
              <span>Use twittalyzer</span></label></li>
            <li><label><input type="checkbox" name="options"
                              value="systemNotifications" ${truck.allowSystemNotifications ? "checked='checked'" : ""}/>
              <span>Allow System Notifications</span></label></li>
            <li><label><input type="checkbox" name="options"
                              value="displayEmailPublicly" ${truck.displayEmailPublicly ? "checked='checked'" : ""}/>
              <span>Display email publicly</span></label></li>
            <li><label><input type="checkbox" name="options"
                              value="twitterGeolocation" ${truck.twitterGeolocationDataValid ? "checked='checked'" : ""}/>
              <span>Can Use Twitter Geolocation Data</span></label>
            <li><label><input type="checkbox" name="options"
                              value="inactive" ${truck.inactive ? "checked='checked'" : ""}/>
              <span>Inactive</span></label>
            </li>
            <li><label><input type="checkbox" name="options"
                              value="hidden" ${truck.hidden ? "checked='checked'" : ""}/>
              <span>Truck is hidden from public listings</span></label>
          </ul>
        </div>
      </div>
    </fieldset>
  </div>
  </div>

  <input type="submit" class="btn btn-primary" value="Update"/> &nbsp;
</form>

<button id="deleteTruck" class="btn btn-danger">DELETE THIS TRUCK</button>

<script>
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

<%@ include file="dashboardFooter.jsp" %>