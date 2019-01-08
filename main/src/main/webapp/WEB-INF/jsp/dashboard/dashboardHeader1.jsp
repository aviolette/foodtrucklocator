<%@ include file="../common.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <!-- These meta tags come first. -->
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <title>Chicago Food Truck Finder Admin Console</title>

  <!-- Include the CSS -->
  <link href="http://fonts.googleapis.com/css?family=Roboto:300,400,500,700,400italic" rel="stylesheet">
  <link href="/css/toolkit-inverse.min.css" rel="stylesheet">
  <link href="/css/glyphicons.css" rel="stylesheet"/>

</head>
<body>
<div class="with-iconav">
  <nav class="iconav">
    <a class="iconav-brand" href="/">
      <img src="/img/logo.png" width="60" height="60" alt="FTF Logo"/>
    </a>
    <div class="iconav-slider">
      <ul class="nav nav-pills iconav-nav flex-md-column">
        <li class="nav-item">
          <a class="nav-link" href="/admin/trucks" title="" data-toggle="tooltip" data-placement="right" data-container="body" data-original-title="Trucks">
            <span class="glyphicons glyphicons-truck"></span>
            <small class="iconav-nav-label d-md-none">Truck</small>
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/admin/locations" title="" data-toggle="tooltip" data-placement="right" data-container="body" data-original-title="Location">
            <span class="glyphicons glyphicons-map-marker"></span>
            <small class="iconav-nav-label d-md-none">Locations</small>
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/admin/messages" title="" data-toggle="tooltip" data-placement="right" data-container="body" data-original-title="Location">
            <span class="glyphicons glyphicons-pencil"></span>
            <small class="iconav-nav-label d-md-none">Messages</small>
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/admin/alexa_query" title="" data-toggle="tooltip" data-placement="right" data-container="body" data-original-title="Alexa">
            <span class="glyphicons glyphicons-microphone"></span>
            <small class="iconav-nav-label d-md-none">Messages</small>
          </a>
        </li>
      </ul>
    </div>
  </nav>
  <div class="container">
