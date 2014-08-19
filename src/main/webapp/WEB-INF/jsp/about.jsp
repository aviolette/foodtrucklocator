<%@ include file="header.jsp" %>

<p class="lead">The Chicago Food Truck Finder is a set of applications written by me, <a href="http://www.andrewviolette.net">Andrew Violette</a>, to find food trucks in Chicago.
  The website puts food trucks on a map with their location and estimated times.  My goal has always been to build a food truck locator, which not only has information about where food trucks are, but also includes schedule information for later in the day and beyond. Accuracy has always been my number one goal.</p>

<h3>How Does it Work?</h3>

<p>The Chicago Food Truck Finder starts by coalescing vendor calendars and a global calendar that I create into one big schedule.
  This schedule gets built every night.  In the morning, I go through every truck and verify that I haven't missed anything. As the
  day goes on, my website scans all the twitter accounts for food trucks, and through the magic of Regular Expressions,
  Google and Yahoo Geolocation APIs, and my own set of heuristics, I add stops to my website.  In addition, the CFTF also scans certain people's twitter accounts
  to crowd-source adding stops (For instance, my website scans @uchinomgo's twitter account to add trucks at University of Chicago).</p>

<p>I have various ways to receive notifications about where food trucks are going to be.

<dl>
  <dt>Location-specific twitter accounts</dt>
  <dd>I have a ton of twitter accounts that tweet trucks that tweet about specific locations.  I've created one of these accounts for most of the hot-spots that food trucks go.  You can view the complete list <a href="https://twitter.com/chifoodtruckz/notifications/members">here</a>.</dd>
  <dt>Android Application</dt>
  <dd>The <a href="https://play.google.com/store/apps/details?id=net.andrewviolette.truckz">Chicago Food Truck Finder Android application</a> in the Play store uses your phone's geolocation to notify you of any food trucks in the area.</dd>
  <dt>Chrome Extension</dt>
  <dd>The <a href="">Chicago Food Truck Finder Notifier</a> chrome extension is in the Chrome store.  This puts an icon on your Chrome toolbar that indicates how many food trucks are nearby.</dd>
</dl>
</p>


<%@ include file="footer.jsp" %>
