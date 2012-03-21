<%@ include file="dashboardHeader.jsp" %>

<form action="" method="POST">
  <div class="clearfix">
    <label for="truckName">Name:</label>

    <div class="input">
      <input type="text" id="truckName" name="name" value="${truck.name}"/>
    </div>
  </div>
  <div class="clearfix">
    <label for="twitterHandle">Twitter Handle:</label>

    <div class="input">
      <input type="text" id="twitterHandle" name="twitterHandle" value="${truck.twitterHandle}"/>
    </div>
  </div>
  <div class="clearfix">
    <label for="url">Url:</label>

    <div class="input">
      <input type="text" id="url" name="url" value="${truck.url}"/>
    </div>
  </div>
  <div class="clearfix">
    <label for="iconUrl">Icon URL:</label>

    <div class="input">
      <input type="text" id="iconUrl" name="iconUrl" value="${truck.iconUrl}"/>
    </div>
  </div>
  <div class="clearfix">
    <label for="description">Description</label>

    <div class="input">
      <input type="text" id="description" name="description" value="${truck.description}"/>
    </div>
  </div>
  <div class="clearfix">
    <label for="foursquareUrl">Foursquare ID</label>

    <div class="input">
      <input type="text" id="foursquareUrl" name="foursquareUrl" value="${truck.foursquareUrl}"/>
    </div>
  </div>
  <div class="clearfix">
    <label for="facebook">Facebook URI</label>

    <div class="input">
      <input type="text" id="facebook" name="facebook" value="${truck.facebook}"/>
    </div>
  </div>
  <div class="clearfix">
    <label for="calendarUrl">Calendar URL</label>

    <div class="input">
      <input type="text" id="calendarUrl" name="calendarUrl" value="${truck.calendarUrl}"/>
    </div>
  </div>
  <div class="clearfix">
    <label for="foursquareUrl">Default City</label>

    <div class="input">
      <input type="text" id="defaultCity" name="defaultCity" value="${truck.defaultCity}"/>
    </div>
  </div>
  <div class="clearfix">
    <label for="matchOnlyIf">Match-only Regex</label>

    <div class="input">
      <input type="text" id="matchOnlyIf" name="matchOnlyIf" value="${truck.matchOnlyIfString}"/>
    </div>
  </div>
  <div class="clearfix">
    <label for="categories">Categories</label>

    <div class="input">
      <input type="text" id="categories" name="categories" value="${truck.categoryList}"/>
    </div>
  </div>
  <div class="clearfix">
    <label id="options">Options</label>

    <div class="input">
      <ul class="inputs-list">
        <li><label><input type="checkbox" name="options"
                          value="twittalyzer" ${truck.usingTwittalyzer ? "checked='checked'" : ""}/>
          <span>Use twittalyzer</span></label></li>
        <li><label><input type="checkbox" name="options"
                          value="inactive" ${truck.inactive ? "checked='checked'" : ""}/> <span>Inactive</span></label>
        </li>
      </ul>
    </div>
  </div>
  <input type="submit" class="btn primary" value="Update"/>
</form>

<%@ include file="dashboardFooter.jsp" %>