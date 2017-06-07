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
