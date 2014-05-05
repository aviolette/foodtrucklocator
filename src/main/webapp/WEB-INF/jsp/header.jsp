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
  <link href="/css/main.css?ver=7" rel="stylesheet"/>
  <script src="/script/lib/modernizr-1.7.min.js"></script>
  <style type="text/css">
    #listContainer {
      overflow-y: auto !important;
    }
    .tooltip-inner {
      text-align: left;
      padding: 10px;
    }
    .tooltip-inner ul {
      padding-left:10px;
      margin:0;
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
        <li <c:if test="${tab == 'map'}"> class="active"</c:if>><a href="/">Map</a></li>
        <li <c:if test="${tab == 'trucks'}"> class="active"</c:if>><a href="/trucks">Vendors</a></li>
        <li <c:if test="${tab == 'weekly'}"> class="active"</c:if>><a href="/weekly-schedule">Weekly Schedule</a></li>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown">Stats <b class="caret"></b></a>
          <ul class="dropdown-menu">
            <li><a href="/stats/heatmap">Heatmap</a></li>
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
        <c:if test="${!empty(user)}">
<%--          <li><a href="#">${user}</a></li> --%>
          <li><a href="${signoutUrl}">Logout</a></li>
        </c:if>

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
