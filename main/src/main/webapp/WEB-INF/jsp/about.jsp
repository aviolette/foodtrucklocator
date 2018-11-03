<%@ include file="header.jsp" %>
<style>
  .media {
    margin: 30px 0;
  }
</style>
<div class="row">
  <div class="col-md-12">

    <p class="lead">The Chicago Food Truck Finder is used to find food trucks in real-time on the streets of Chicago.
      It started out as a project for <a href="http://www.andrewviolette.net">me</a> in 2011, during the food truck
      boom in Chicago.  I love food trucks and want them to be found &#x1F600;.</p>

    <a name="collect"></a>
    <h2 class="about-page">How do food trucks add their stops to this site?</h2>

    <div class="media">
      <img class="mr-3" src="/img/glyphicons-basic-817-satellite-dish@3x.png"/>
      <div class="media-body">
        <h5>GPS Devices</h5>
        <p>A few trucks use Linxup GPS devices that are plugable into the truck's OBDII port.  These allow accurate,
          real-time reporting of location.</p>
      </div>
    </div>
    <div class="media">
      <img class="mr-3" src="/img/glyphicons-basic-46-calendar@3x.png"/>
      <div class="media-body">
        <h5>Calendars</h5>
        <p>Trucks post their schedules in calendars in various formats: Google, iCal, and various proprietary formats
          that I've reverse engineered. I scan these calendars periodically and sync the schedules to this site.</p>
      </div>
    </div>
    <div class="media">
      <img class="mr-3" src="/img/glyphicons-basic-547-cloud-refresh@3x.png"/>
      <div class="media-body">
        <h5>Social Media Sync</h5>
        <p>Trucks post where they're going to be on social media.  This site scans social media and tries to determine
          where and when a truck is going to be at a location</p>
      </div>
    </div>
    <div class="media">
      <img class="mr-3" src="/img/glyphicons-basic-166-mobile-phone@3x.png"/>
      <div class="media-body">
        <h5>Vendor Portal</h5>
        <p>Some trucks manually add their stops by using my vendor portal.</p>
      </div>
    </div>
  </div>
</div>

<%@include file="include/core_js.jsp" %>
<%@ include file="footer.jsp" %>
