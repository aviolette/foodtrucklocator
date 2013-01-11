<%@ include file="common.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="author" content="Andrew Violette, @aviolette/@chifoodtruckz on twitter"/>
  <meta name="description"
        content="Find food trucks on the streets of Chicago by time and location.  Results are updated in real-time throughout the day."/>
  <title>Chicago Food Truck Finder</title>
  <link href="/bootstrap2.2.2-custom/css/bootstrap.min.css" rel="stylesheet"/>
  <link href="/css/main.css?ver=4" rel="stylesheet"/>
  <script src="script/lib/modernizr-1.7.min.js"></script>
</head>
<body>
<div id="topBar" class="navbar">
  <div class="navbar-inner">
    <div class="container-fluid">
      <a class="brand" href="/">Chicago Food Truck Finder</a>
      <ul class="nav">
        <li><a id="aboutLink" href="#about">About</a></li>
        <%--
        <li><a href="#settings">Settings</a></li>
        <li><a href="/trucks">Trucks</a></li>
        <li><a target="_blank" href="http://blog.chicagofoodtruckfinder.com">Blog</a></li>
        --%>
      </ul>
      <p style="padding-top: 4px" class="navbar-form pull-right"><a href="https://twitter.com/chifoodtruckz"
                               class="twitter-follow-button" data-button="grey"
                               data-text-color="#FFF" data-link-color="#FFF">Follow
        @chifoodtruckz</a></p>

      <div style="padding-right: 10px !important" class="navbar-form pull-right fb-like"
           data-href="http://www.facebook.com/chicagofoodtruckfinder"
           data-send="false" data-layout="button_count" data-width="50"
           data-show-faces="false"></div>

    </div>
  </div>
</div>

<div class="container-fluid">
