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
      <link href="/css/bootstrap-toggle.css" rel="stylesheet">
    </c:when>
    <c:otherwise>
      <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">
      <link href="https://gitcdn.github.io/bootstrap-toggle/2.2.2/css/bootstrap-toggle.min.css" rel="stylesheet">
    </c:otherwise>
  </c:choose>
  <link href="/css/dropzone.css" rel="stylesheet"/>
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

        <c:if test="${!empty(vendorIconUrl)}">
          <img src="${vendorIconUrl.protocolRelative}" alt="${vendorIconDescription}"
               style="margin-right:20px; margin-left:10px" class="img-circle"
               width="48" height="48">
        </c:if>
        <c:if test="${empty(truck)}">
          <span class="navbar-brand">Vendor Dashboard</span>
        </c:if>
      </div>
      <div class="collapse navbar-collapse">
        <c:if test="${!empty(logoutUrl)}">
          <ul class="nav navbar-nav">
            <li <c:if test="${tab == 'vendorhome'}"> class="active"</c:if>><a class="btn btn-block" href="/vendor"><span
                class="glyphicon glyphicon-dashboard"></span> Dashboard</a></li>
            <c:if test="${!empty(truck)}">

              <li class="dropdown">
                <a href="#" class="dropdown-toggle btn btn-block" data-toggle="dropdown"><span
                    class="glyphicon glyphicon-cog"></span> Settings <b class="caret"></b></a>
                <ul class="dropdown-menu">
                  <li><a href="/vendor/settings/${truck.id}">General</a></li>
                  <li><a href="/vendor/menu/${truck.id}">Menu</a></li>
                  <li><a href="/vendor/socialmedia/${truck.id}">Social Media</a></li>
                  <li><a href="/vendor/notifications/${truck.id}">Notifications</a></li>
                </ul>
              </li>

            </c:if>
          </ul>
          <ul class="nav navbar-nav pull-right">
            <li><a title="home page" class="btn btn-block" href="/"><span class="glyphicon glyphicon-home"></span> Front
              Page</a>
            </li>
            <li><a title="logoff" class="btn btn-block" href="${logoutUrl}"><span
                class="glyphicon glyphicon-log-out"></span> Logout</a></li>
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
