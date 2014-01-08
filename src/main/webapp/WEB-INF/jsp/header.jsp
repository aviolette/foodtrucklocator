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
  <title>${title}</title>
  <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">
  <link href="/css/main.css?ver=5" rel="stylesheet"/>
  <script src="/script/lib/modernizr-1.7.min.js"></script>
  <style type="text/css">
    #listContainer {
      overflow-y: auto !important;
    }
  </style>
</head>
<body>
<nav id="topBar" class="navbar navbar-fixed-top navbar-inverse" role="navigation">
  <div class="container">
    <div class="navbar-header">
      <a class="navbar-brand" href="/">Chicago Food Truck Finder</a>
    </div>
    <div class="collapse navbar-collapse">
      <ul class="nav navbar-nav">
        <li class=dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown">Trucks <b class="caret"></b></a>
        <ul class="dropdown-menu">
          <li><a href="/">Today's Schedule</a></li>
          <li <c:if test="${tab == 'weekly'}"> class="active"</c:if>><a href="/weekly-schedule">Weekly Schedule</a></li>
          <li <c:if test="${tab == 'trucks'}"> class="active"</c:if>><a href="/trucks">Truck List</a></li>
        </ul>
        </li>
        <li><a href="http://blog.chicagofoodtruckfinder.com/">Blog</a></li>
        <c:if test="${isAdmin}">
        <li><a href="/admin">Admin</a></li>
        </c:if>
      </ul>
      <ul class="nav navbar-right navbar-nav">
        <li><a style="padding: 10px 0 0 0" href="http://twitter.com/chifoodtruckz"><img src="/img/twitter02_dark.png"/></a></li>
        <li><a style="padding: 10px 0 0 0" href="http://facebook.com/chicagofoodtruckfinder"><img src="/img/facebook_dark.png"/></a></li>
      </ul>
    </div>
  </div>
</nav>

<div class="container cftf-main-container">
  <noscript>
    <div class="alert alert-error">
      Javascript is required for this site to function properly.
    </div>
  </noscript>
