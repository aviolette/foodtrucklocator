<%@ include file="../common.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="author" content="Andrew Violette, @aviolette/@chifoodtruckz on twitter"/>
  <meta name="description"
        content="Portal app for food trucks"/>
  <title>Vendor Dashboard</title>
  <c:choose>
    <c:when test="${localFrameworks}">
      <link rel="stylesheet" href="/bootstrap3.0.3/css/bootstrap.min.css"/>
    </c:when>
    <c:otherwise>
      <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">
    </c:otherwise>
  </c:choose>
  <script src="/script/lib/modernizr-1.7.min.js"></script>
  <link href="/css/foodtruckfinder.css?ver=11" rel="stylesheet"/>
  <link rel="stylesheet" href="/css/typeahead.css"/>
  <style type="text/css">
  </style>
</head>
<body>
<div class="container cftf-main-container">
  <div id="topBar" class="navbar navbar-fixed-top navbar-inverse" role="navigation">
    <div class="container">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="/">CFTF Vendor Dashboard</a>
      </div>
      <div class="collapse navbar-collapse">
        <c:if test="${!empty(logoutUrl)}">
          <ul class="nav navbar-nav">
            <li <c:if test="${tab == 'vendorhome'}"> class="active"</c:if>><a href="/vendor">Home</a></li>
<%--            <li <c:if test="${tab == 'beaconnaise'}"> class="active"</c:if>><a href="/vendor/beaconnaise">Beaconnaise</a></li> --%>
            <li <c:if test="${tab == 'trucksettings'}"> class="active"</c:if>><a href="/vendor/settings/${truck.id}">Settings</a></li>
          </ul>
          <ul class="nav navbar-nav pull-right">
            <li><a href="${logoutUrl}">Logout</a></li>
          </ul>
        </c:if>
      </div>
    </div>
  </div>

  <div class="content">
    <noscript>
      <div class="alert alert-error">
        Javascript is required for this site to function properly.
      </div>
    </noscript>

    <div id="flash" style="display:none" class="alert alert-info">
    </div>
