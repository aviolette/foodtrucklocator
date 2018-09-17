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
  <c:choose>
    <c:when test="${bootstrap4}">
      <link href="/css/foodtruckfinder${suffix}-1.4.css" rel="stylesheet"/>
      <link href="/css/glyphicons.css" rel="stylesheet"/>
      <link href="/css/glyphicons-social.css" rel="stylesheet"/>
    </c:when>
    <c:otherwise>
      <link href="/css/foodtruckfinder${suffix}-1.3.1.css" rel="stylesheet"/>
    </c:otherwise>
  </c:choose>
  <c:if test="${!empty(additionalCss)}">
    <link href="${additionalCss}" rel="stylesheet"/>
  </c:if>
</head>
<body>
<nav class="navbar navbar-expand-md navbar-dark fixed-top bg-dark">
  <div class="container" id="foo">
    <a class="navbar-brand" href="/"><img src="/img/logo.png" width="30" height="30" alt=""> ${brandTitle}</a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarsExampleDefault" aria-controls="navbarsExampleDefault" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>

    <div class="collapse navbar-collapse" id="navbarsExampleDefault">
      <ul class="navbar-nav mr-auto">
        <li class="nav-item<c:if test="${tab == 'map'}"> active</c:if>">
          <a class="nav-link" href="/"><span class="glyphicons glyphicons-electricity"></span>Activity</a>
        </li>
        <li class="nav-item">
          <a class="nav-link<c:if test="${tab == 'trucks'}"> active</c:if>" href="/trucks"><span class="glyphicons glyphicons-truck"></span>Trucks</a>
        </li>
      </ul>
      <div class="navbar-nav flex-row ml-md-auto">
        <li class="nav-item"><a href="/about" class="nav-link">About</a></li>
        <li class="nav-item"><a class="nav-link" href="https://twitter.com/chifoodtruckz" title="twitter: @chifoodtruckz"><span class="social social-twitter"></span></a></li>
      </div>
    </div>
  </div>
</nav>

<div class="container${suffix} cftf-main-container">

  <noscript>
    <div class="alert alert-error">
      Javascript is required for this site to function properly.
    </div>
  </noscript>
