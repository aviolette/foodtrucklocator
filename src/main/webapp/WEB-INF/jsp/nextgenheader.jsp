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
  <link href="/css/main.css?ver=5" rel="stylesheet"/>
  <script src="/script/lib/modernizr-1.7.min.js"></script>
  <style type="text/css">
    #listContainer {
      overflow-y: auto !important;
    }
  </style>
</head>
<body>
<div id="topBar" class="navbar">
  <div class="navbar-inner">
    <div class="container-fluid">
      <a class="brand" href="/">Chicago Food Truck Finder</a>
      <ul class="nav">
        <li <c:if test="${tab == 'weekly'}"> class="active"</c:if>><a href="/weekly-schedule">Weekly</a></li>
        <li <c:if test="${tab == 'trucks'}"> class="active"</c:if>><a href="/trucks">Trucks</a></li>
        <li><a href="http://blog.chicagofoodtruckfinder.com/">Blog</a></li>
        <li class="visible-desktop"><a id="mobileLink" href="#mobile">Mobile</a></li>
      </ul>
      <div style="padding: 4px 0 0 0 !important; margin:0;" class="navbar-form pull-right">
        <div class="g-plusone"></div>
      </div>
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

<div
    class="<c:choose><c:when test="${containerType == 'fixed'}">container</c:when><c:otherwise>container-fluid</c:otherwise></c:choose>">
  <noscript>
    <div class="alert alert-error">
      Javascript is required for this site to function properly.
    </div>
  </noscript>
