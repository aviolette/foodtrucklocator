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
  <link href="/css/foodtruckfinder.css?ver=11" rel="stylesheet"/>
  <link rel="stylesheet" href="//storage.googleapis.com/ftf_static/css/typeahead.css"/>
  <link rel="stylesheet" href="/css/truck_edit_widget.css"/>
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
        <span class="navbar-brand">Vendor Dashboard</span>
      </div>
      <div class="collapse navbar-collapse">
        <c:if test="${!empty(logoutUrl)}">
          <ul class="nav navbar-nav">
            <li <c:if test="${tab == 'vendorhome'}"> class="active"</c:if>><a href="/vendor">Dashboard</a></li>
            <c:if test="${!empty(truck)}">
              <li <c:if test="${tab == 'menu'}"> class="active"</c:if>><a href="/vendor/menu/${truck.id}">Menu</a></li>
              <li <c:if test="${tab == 'profile'}"> class="active"</c:if>><a
                  href="/vendor/settings/${truck.id}">General</a></li>
              <li <c:if test="${tab == 'linxup'}"> class="active"</c:if>><a
                  href="/vendor/linxup/${truck.id}">Beacons</a></li>
            </c:if>
          </ul>
          <ul class="nav navbar-nav pull-right">
            <c:if test="${!empty(vendorIconUrl)}">
              <li><img src="${vendorIconUrl.protocolRelative}" alt="${vendorIconDescription}" class="img-circle"
                       width="48" height="48"></li>
            </c:if>
            <li><a title="home page" class="btn btn-block" href="/"><span class="glyphicon glyphicon-home"></span> </a>
            </li>
            <li><a title="logoff" class="btn btn-block" href="${logoutUrl}"><span
                class="glyphicon glyphicon-log-out"></span> </a></li>
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

    <div id="flash" class="alert alert-warning hidden">
    </div>
