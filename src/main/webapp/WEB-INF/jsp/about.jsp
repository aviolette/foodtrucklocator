<%@ include file="header.jsp" %>

<p class="lead">The Chicago Food Truck Finder is a set of applications written by me,
  <a href="http://www.andrewviolette.net">Andrew Violette</a>, to find food trucks in Chicago.
  The website puts food trucks on a map with their location and estimated times.  My goal has always been to build a
  truck locator, which not only has information about where food trucks are, but also includes schedule information for
  later in the day and beyond. Accuracy has always been my number one goal.</p>

<p>This website is free to use and is supported without the use of advertising or sponsors (although I rule out the former, I don't necessarily rule out the latter).  I currently offset the costs of this
  website by selling my iPhone application</p>

<a name="notify"></a>
<h3>How can I receive alerts for food trucks near me?</h3>

<p>Currently, I have three apps that proactively notify you when food trucks are near you:</p>

<dl>
  <dt>Location-specific twitter accounts</dt>
  <dd>I have a ton of twitter accounts that tweet trucks that tweet about specific locations.  I've created one of these accounts for most of the hot-spots that food trucks go.  You can view the complete list <a href="https://twitter.com/chifoodtruckz/notifications/members">here</a>.</dd>
  <dt>Android Application</dt>
  <dd>The <a href="https://play.google.com/store/apps/details?id=net.andrewviolette.truckz">Chicago Food Truck Finder Android application</a> in the Play store uses your phone's geolocation to notify you of any food trucks in the area.</dd>
  <dt>iPhone Application</dt>
  <dd>Although <a href="https://itunes.apple.com/us/app/chicago-food-truck-finder/id1002801516">my iphone app</a> does not support push notifications YET, it will in the near future.</a> </dd>
  <dt>Chrome Extension</dt>
  <dd>The <a href="">Chicago Food Truck Finder Notifier</a> chrome extension is in the Chrome store.  This puts an icon on your Chrome toolbar that indicates how many food trucks are nearby.</dd>
</dl>

<a name="booking"></a>
<h3>"I need food trucks for my event, can you help me?"</h3>

This is not something I can currently do effectively, so your best bet is to use to peruse <a href="/trucks">the selection of trucks on this website</a> and contact them individually.

<a name="howdoesitwork"></a>
<h3>How Does it Work?</h3>

<p>The Chicago Food Truck Finder starts by coalescing vendor calendars and a global calendar that I create into one big schedule.
  This schedule gets built every night.  In the morning, I go through every truck and verify that I haven't missed anything. As the
  day goes on, my website scans all the twitter accounts for food trucks, and through the magic of Regular Expressions,
  Google and Yahoo Geolocation APIs, and my own set of heuristics, I add stops to my website.  In addition, the CFTF also scans certain people's twitter accounts
  to crowd-source adding stops (For instance, my website scans @uchinomgo's twitter account to add trucks at University of Chicago).
</p>

<a name="integration"></a>
<h3>How do I get my food truck and stops listed on your website?</h3>

<p>I have a separate page on how to do that <a href="http://blog.chicagofoodtruckfinder.com/page/integration-faq">here</a>.</p>
<%@include file="include/core_js.jsp" %>

<%@ include file="footer.jsp" %>
