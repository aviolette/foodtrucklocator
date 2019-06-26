<%@include file="../dashboardHeader1.jsp" %>

<%@include file="truckNav.jsp" %>
<script type="application/json" id="upload_data">
  {"artifactType": "truck", "key":"${truck.id}", "initialImage" : "${truck.previewIcon}"}
</script>

<div class="row">
  <div class="col-md-4">

    <div id="upload_section"></div>

  </div>
  <div class="col-md-8">

    <ul class="nav nav-pills mb-3 mt-3">
      <li class="nav-item">
        <a class="nav-link active" id="basic-tab" data-toggle="tab" href="#basic" role="tab" aria-controls="basic"
           aria-selected="true">Basic</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" id="contact-tab" data-toggle="tab" href="#contact" role="tab" aria-controls="contact"
           aria-selected="false">Contact</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" id="calendar-tab" data-toggle="tab" href="#calendar" role="tab" aria-controls="calendar"
           aria-selected="false">Calendar</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" id="attributes-tab" data-toggle="tab" href="#attributes" role="tab"
           aria-controls="attributes" aria-selected="false">Attributes</a>
      </li>
    </ul>


    <form action="" method="POST" id="theForm">

      <div class="tab-content" id="control-tabs">
        <div class="tab-pane fade show active" id="basic" role="tabpanel" aria-labelledby="basic-tab">
          <div class="form-group">
            <label for="truckName">Name:</label>
            <input type="text" class="form-control" id="truckName" name="name" value="${truck.name}"/>
          </div>
          <div class="form-group">
            <label for="description">Description</label>
            <input type="text" class="form-control" id="description" name="description"
                   value="${truck.description}"/>
          </div>
          <div class="form-group">
            <label for="menuUrl">Menu URL:</label>
            <input type="text" class="form-control" id="menuUrl" name="menuUrl"
                   value="${truck.menuUrl}"/>
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
          <div class="form-group">
            <label for="phoneticMarkup">Phonetic Markup</label>
            <input type="text" class="form-control" id="phoneticMarkup" name="phoneticMarkup"
                   value="<c:out value="${truck.phoneticMarkup}" escapeXml="true"/>"/>
          </div>
          <div class="form-group">
            <label for="phoneticAliases">Phonetic Aliases</label>
            <input type="text" class="form-control" id="phoneticAliases" name="phoneticAliases"
                   value="${truck.phoneticAliasesList}"/>
          </div>

          <div class="form-group">
            <label for="blacklistLocations">Blacklist Locations (use ; as a separator)</label>
            <input type="text" class="form-control" id="blacklistLocations" name="blacklistLocations"
                   value="${truck.blacklistLocationsList}"/>
          </div>

          <input type="hidden" class="form-control" id="iconUrl" name="iconUrl"
                 value="${truck.iconUrl}"/>
          <input type="hidden" class="form-control" id="previewIcon" name="previewIcon"
                 value="${truck.previewIcon}"/>
          <input type="hidden" class="form-control" id="fullsizeImage" name="fullsizeImage"
                 value="${truck.fullsizeImage}"/>
          <input type="hidden" class="form-control" id="backgroundImage" name="backgroundImage"
                 value="${truck.backgroundImage}"/>
          <input type="hidden" class="form-control" id="largeBackgroundImage" name="backgroundImageLarge"
                 value="${truck.backgroundImageLarge}"/>


        </div>


        <div class="tab-pane fade" id="contact" role="tabpanel" aria-labelledby="contact-tab">
          <div class="form-group">
            <label for="twitterHandle">Twitter Handle:</label>
            <input type="text" class="form-control" id="twitterHandle" name="twitterHandle"
                   value="${truck.twitterHandle}"/>
          </div>
          <div class="form-group">
            <label for="facebook">Facebook URI</label>
            <input type="text" class="form-control" id="facebook" name="facebook"
                   value="${truck.facebook}"/>
          </div>
          <div class="form-group">
            <label for="instagramId">Instagram Id</label>
            <input type="text" class="form-control" id="instagramId" name="instagramId"
                   value="${truck.instagramId}"/>
          </div>
          <input type="hidden" class="form-control" id="yelp" name="yelp"
                 value="${truck.yelpSlug}"/>
          <input type="hidden" class="form-control" id="foursquareUrl" name="foursquareUrl"
                 value="${truck.foursquareUrl}"/>
          <input type="hidden" class="form-control" id="facebookPageId" name="facebookPageId"
                 value="${truck.facebookPageId}"/>
          <input type="hidden" class="form-control" id="fleetSize" name="fleetSize" value="${truck.fleetSize}"/>

          <div class="form-group">
            <label for="email">Email</label>
            <input type="text" class="form-control" id="email" name="email" value="${truck.email}"/>
          </div>
          <div class="form-group">
            <label for="phone">Phone</label>
            <input type="text" class="form-control" id="phone" name="phone" value="${truck.phone}"/>
          </div>

          <div class="form-group">
            <label for="url">Url:</label>
            <input type="text" class="form-control" id="url" name="url" value="${truck.url}"/>
          </div>

        </div>
        <div class="tab-pane fade" id="calendar" role="tabpanel" aria-labelledby="calendar-tab">

          <div class="form-group">
            <label for="calendarUrl">Google Calendar URL</label>
            <input type="text" class="form-control" id="calendarUrl" name="calendarUrl"
                   value="${truck.calendarUrl}"/>
          </div>
          <div class="form-group">
            <label for="drupalCalendar">Drupal Calendar Url</label>
            <input type="text" class="form-control" id="drupalCalendar" name="drupalCalendar"
                   value="${truck.drupalCalendar}"/>
          </div>
          <div class="form-group">
            <label for="iCalCalendar">iCal Calendar Url</label>
            <input type="text" class="form-control" id="iCalCalendar" name="iCalCalendar"
                   value="${truck.icalCalendar}"/>
          </div>
          <div class="form-group">
            <label for="squarespaceCalendar">Squarespace Calendar Url</label>
            <input type="text" class="form-control" id="squarespaceCalendar" name="squarespaceCalendar"
                   value="${truck.squarespaceCalendar}"/>
          </div>
          <div class="form-group">
            <label for="timezoneAdjustment">Timezone adjustment</label>
            <input type="text" class="form-control" id="timezoneAdjustment" name="timezoneAdjustment"
                   value="${truck.timezoneAdjustment}"/>
          </div>

        </div>
        <div class="tab-pane fade" id="attributes" role="tabpanel" aria-labelledby="attributes-tab">
          <div class="checkbox">
            <label><input type="checkbox" name="options"
                          value="twittalyzer" ${truck.usingTwittalyzer ? "checked='checked'" : ""}/> Use
              twittalyzer</label>
          </div>
          <div class="checkbox">
            <label><input type="checkbox" name="options"
                          value="facebooker" ${truck.scanFacebook ? "checked='checked'" : ""}/> Use facebooker</label>
          </div>
          <div class="checkbox">
            <label><input type="checkbox" name="options"
                          value="displayEmailPublicly" ${truck.displayEmailPublicly ? "checked='checked'" : ""}/>
              Display email publicly</label>
          </div>
          <div class="checkbox">
            <label><input type="checkbox" name="options"
                          value="notifyOfLocationChanges" ${truck.notifyOfLocationChanges ? "checked='checked'" : ""}/>
              Notify of location changes</label>
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
        </div>
      </div>


    </form>
    <div class="btn-toolbar">
      <div class="btn-group">
        <button id="submitButton" class="btn btn-primary btn-lg">Save
        </button>
      </div>
    </div>

  </div>
</div>

<%@include file="../dashboardFooter1.jsp" %>
