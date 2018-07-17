<%@ include file="common.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="author" content="Andrew Violette, @aviolette/@chifoodtruckz on twitter"/>
  <link rel="apple-touch-icon" href="/img/apple-touch-icon.png">
  <link rel="apple-touch-icon" sizes="76x76" href="/img/apple-touch-icon-76x76.png">
  <link rel="apple-touch-icon" sizes="120x120" href="/img/apple-touch-icon-iphone-retina.png">
  <link rel="apple-touch-icon" sizes="152x152" href="/img/apple-touch-icon-ipad-retina.png">
  <title>Chicago Food Truck Finder</title>
  <%@ include file="include/bootstrap_css.jsp" %>
  <style>
    body {
      padding-top: 20px;
      padding-bottom: 20px;
    }

    @media (min-width: 768px) {
      .container {
        max-width: 730px;
      }
    }

    .jumbotron {
      text-align: center;
      border-bottom: 1px solid #e5e5e5;
    }
  </style>
</head>
<body>
<div class="container">

  <div class="header">

  </div>
  <div class="jumbotron">
    <h1>Shutting Down</h1>
    <p>It was a fun ride keeping this site up for 7 years. I'd like to specifically thank: <a
        href="http://beaversdonuts.com">Beavers Donuts</a>, <a href="http://chicagolunchbox.com">Chicago
      Lunchbox</a>, <a href="http://asweetsgirl.com">A Sweets Girl</a>, <a
        href="http://courageouscakes.com">Cupcakes for Courage</a>,
      <a href="http://jarabechicago.com">Adelita</a>, <a href="http://theroostcarolinakitchen.com">The
        Roost</a>, <a href="http://theslideride.com">The Slide Ride</a>, and <a
          href="http://thecornerfarmacy.com">The Corner Farmacy</a> for supporting me over the
      years. Go visit them and eat their food.
    </p>
  </div>
  <div>
    <p>When I built this website back in 2011,
      food trucks were a relatively new phenomenon in Chicago. Trucks were able to park (without too
      much harassment) in many places throughout the Loop, River North, and West Loop. Over time,
      the number of places that trucks could go legally dwindled. Now trucks have semi-permanent
      spots at Wacker/Adams, Clark/Monroe, Michigan/Monroe and University of Chicago. Even there,
      they get chased
      away.</p>
    <p>In the early days, I got to know a lot of the truck owners and made a lot of great friends.
      It was great being part of the scene and witnessing small businesses thrive against the odds,
      and
      making great food along the way. While food trucks still have great food, the vibe is
      different in ways
      I can't quantify. Most of the newer trucks don't really make their location available in any
      format that I can grok and display on this website.
    </p>
    <p>At the same time, the operational costs of this website have doubled due to the way google
      prices for maps and geolocation lookups. For that reason, I have decided to shutdown this
      website. Shutting this down will also affect the twitter feeds, Alexa app, and Chrome extension that rely
      on this website for data.</p>
    <p>Thanks for being fans! - Andrew Violette</p>
  </div>
</div>
<script>
  (function (i, s, o, g, r, a, m) {
    i['GoogleAnalyticsObject'] = r;
    i[r] = i[r] || function () {
      (i[r].q = i[r].q || []).push(arguments)
    }, i[r].l = 1 * new Date();
    a = s.createElement(o),
        m = s.getElementsByTagName(o)[0];
    a.async = 1;
    a.src = g;
    m.parentNode.insertBefore(a, m)
  })(window, document, 'script', 'https://www.google-analytics.com/analytics.js', 'ga');

  ga('create', "${googleAnalytics}", 'auto');
  ga('send', 'pageview');

</script>

</body>
</html>
