<%@ include file="../header.jsp" %>

<style>
  .jumbotron {
    text-align: center;
    background-color: transparent;
    margin-bottom: 0;
  }
</style>
<div class="jumbotron">
  <p class="lead">
    This ${website}'s mission is to help people find food trucks. We publish your truck's schedule on our website and
    applications. We publish alerts via our applications and social media accounts targeted at users who are interested
    in
    food trucks at a specific location. We provide tools for the truck owner to help you control the display of your
    data
    on this website via <a href="/vendor">our vendor portal</a>. From there you can edit your schedule, contact
    information, menu,
    and social media notifications.
  </p>
</div>

<div class="row">
  <div class="col-lg-12">
    <h1>How to Publish Your Location on the Food Truck Finder</h1>
  </div>
</div>

<div class="row">
  <div class="col-lg-3">
    <h2>Tracking Device</h2>
    <p>I sell a tracking device that can be plugged into your truck's OBD port or wired into your ignition. Once you
      plug
      this device in, and do some minor instrumentation on my website, your truck will automatically show on my website
      and
      send out alerts to your customers via social media.</p>
    <p><a href="/vendinfo/device" class="btn btn-primary">Read More &raquo;</a></p>
  </div>
  <div class="col-lg-3">
    <h2>Published Calendar</h2>
    <p>You can put your schedule in a google calendar or iCal calendar. I can read it in and automatically update your
      schedule on my website.</p>
  </div>
  <div class="col-lg-3">
    <h2>Vendor Portal</h2>
    <p>You can directly add your schedule to my website via <a href="/vendor">the vendor portal.</a>
    </p>
  </div>
  <div class="col-lg-3">
    <h2>Social Media Scanning</h2>
    <p>
      This has long been the most popular way of putting your stops on my website. My site will read in tweets, find out
      the
      location and time information and put them on my website.
    </p>
  </div>
</div>

<%@ include file="../include/core_js.jsp" %>
<%@ include file="../footer.jsp" %>
