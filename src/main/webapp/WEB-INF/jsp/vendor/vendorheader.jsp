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
  <title>Beaconnaise</title>
  <link href="/bootstrap2.2.2-custom/css/bootstrap.min.css" rel="stylesheet"/>
  <script src="/script/lib/modernizr-1.7.min.js"></script>
  <style type="text/css">
    #listContainer {
      overflow-y: auto !important;
    }
  </style>
</head>
<body>
<div id="topBar" class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container-fluid">
      <a class="brand" href="/">Chicago Food Truck Finder</a>
      <ul class="nav">
        <li <c:if test="${tab == 'vendorhome'}"> class="active"</c:if>><a href="/vendor">Home</a></li>
        <li <c:if test="${tab == 'beaconnaise'}"> class="active"</c:if>><a href="/vendor/beaconnaise/${truck.id}">Beaconnaise</a></li>
        <li <c:if test="${tab == 'trucksettings'}"> class="active"</c:if>><a href="/vendor/settings/${truck.id}">Settings</a></li>
      </ul>
    </div>
  </div>
</div>

<div
    style="padding-top: 60px" class="container">
<noscript>
  <div class="alert alert-error">
    Javascript is required for this site to function properly.
  </div>
</noscript>

<div id="flash" style="display:none" class="alert alert-info">
</div>
