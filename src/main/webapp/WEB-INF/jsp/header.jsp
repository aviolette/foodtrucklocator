<%@ include file="common.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="author" content="Andrew Violette, @aviolette/@chifoodtruckz on twitter"/>
  <meta name="description" content="${description}"/>
  <link rel="apple-touch-icon" href="/img/apple-touch-icon.png">
  <link rel="apple-touch-icon" sizes="76x76" href="/img/apple-touch-icon-76x76.png">
  <link rel="apple-touch-icon" sizes="120x120" href="/img/apple-touch-icon-iphone-retina.png">
  <link rel="apple-touch-icon" sizes="152x152" href="/img/apple-touch-icon-ipad-retina.png">
  <title>${title}</title>
  <%@ include file="include/bootstrap_css.jsp" %>
  <link href="/css/foodtruckfinder${suffix}-1.0.css" rel="stylesheet"/>
  <c:choose>
    <c:when test="${localFrameworks}">
      <script src="/script/lib/modernizr-1.7.min.js"></script>
    </c:when>
    <c:otherwise>
      <script src="//storage.googleapis.com/ftf_static/script/modernizr-1.7.min.js"></script>
    </c:otherwise>
  </c:choose>
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
<div class="container${suffix} cftf-main-container">

<div id="topBar" class="navbar navbar-fixed-top navbar-inverse" role="navigation">
  <div class="container">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="/">${brandTitle}</a>
    </div>
    <div class="collapse navbar-collapse">
      <ul class="nav navbar-nav">
        <li <c:if test="${tab == 'map'}"> class="active"</c:if>><a href="/">Activity</a></li>
        <li <c:if test="${tab == 'trucks'}"> class="active"</c:if>><a href="/trucks">Trucks</a></li>
        <c:if test="${showWeekly}">
          <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">Locations <b class="caret"></b></a>
            <ul class="dropdown-menu">
              <li><a href="/weekly-schedule">Popular Spots</a></li>
              <li><a href="/booze">Boozy Spots</a></li>
              <li><a href="/businesses">Food Truck Owned Businesses</a></li>
            </ul>
          </li>
        </c:if>
        <c:if test="${showAbout}">
          <li <c:if test="${tab == 'about'}"> class="active"</c:if>><a href="/about">About</a></li>
        </c:if>
        <c:if test="${showStats}">
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown">Stats <b class="caret"></b></a>
          <ul class="dropdown-menu">
            <li><a href="/stats/timeline">Timeline</a></li>
          </ul>
        </li>
        </c:if>
        <c:if test="${showBlog}">
        <li><a href="http://blog.chicagofoodtruckfinder.com/">Blog</a></li>
        </c:if>
        <c:if test="${isAdmin}">
          <li><a href="/admin">Admin</a></li>
        </c:if>
      </ul>
      <ul class="nav navbar-right navbar-nav">
        <li><a style="padding: 10px 0 0 0" href="http://twitter.com/${twitterHandle}"><img src="//storage.googleapis.com/ftf_static/img/twitter02_dark.png"/></a></li>
        <li><a style="padding: 10px 0 0 0" href="http://facebook.com/${facebookPage}"><img src="//storage.googleapis.com/ftf_static/img/facebook_dark.png"/></a></li>
        <c:if test="${!empty(user)}">
          <%--          <li><a href="#">${user}</a></li> --%>
          <li><a href="${signoutUrl}">Logout</a></li>
        </c:if>

      </ul>
    </div>
  </div>
</div>

  <noscript>
    <div class="alert alert-error">
      Javascript is required for this site to function properly.
    </div>
  </noscript>
