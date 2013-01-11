<%@ include file="../common.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Administrative Dashboard</title>
  <meta name="description" content="Administrative Dashboard">
  <meta name="author" content="Andrew Violette">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <%--  <script src="//ajax.googleapis.com/ajax/libs/jquery/1.7/jquery.js"></script> --%>
  <script>window.jQuery ||
  document.write("<script src='/script/lib/jquery-1.7.1.min.js'>\x3C/script>")</script>
  <script type="text/javascript" src="/bootstrap2.2.2-custom/js/bootstrap.js"></script>
  <script src="/script/lib/modernizr-1.7.min.js"></script>
  <script src="/script/dashboard.js" type="text/javascript"></script>
  <link href="/bootstrap2.2.2-custom/css/bootstrap.min.css" rel="stylesheet"/>
  <link rel="stylesheet" href="/css/dashboard.css"/>

  <style type="text/css">
    .container {
      padding-top: 50px
    }

    tr.muted {
      display: none;
    }
  </style>

  <style type="text/css" media="screen">

    #editor {
      width: 900px;
      height: 600px;
      margin-top: 10px;
    }
  </style>


  <!-- Le fav and touch icons -->
  <link rel="shortcut icon" href="/favicon.ico">
</head>

<body>

<div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <a class="brand" href="#">Chicago Food Truck Finder - Dashboard</a>
    <ul class="nav">
      <li<c:if test="${nav == 'trucks'}"> class="active"</c:if>><a href="/admin/trucks">Trucks</a>
      </li>
      <li<c:if test="${nav == 'addresses'}"> class="active"</c:if>><a href="/admin/addresses">Addresses</a>
      </li>
      <li<c:if test="${nav == 'locations'}"> class="active"</c:if>><a href="/admin/locations">Locations</a>
      </li>
      <li<c:if test="${nav == 'notifications'}"> class="active"</c:if>><a href="/admin/notifications">Notifications</a>
      </li>
      <li<c:if test="${nav == 'settings'}"> class="active"</c:if>><a href="/admin/configuration">Settings</a>
      </li>
      <li<c:if test="${nav == 'stats'}"> class="active"</c:if>><a
          href="/admin/stats">Statistics</a>
      </li>
    </ul>
  </div>
</div>

<div class="container">

  <div class="content">
    <c:if test="${!empty(breadcrumbs)}">
    <div class="page-header">
      <ul class="breadcrumb">
        <c:forEach items="${breadcrumbs}" var="breadcrumb" varStatus="breadcrumbStatus">
          <c:choose>
            <c:when test="${breadcrumbStatus.last}">
              <li class="active">${breadcrumb.name}</li>
            </c:when>
            <c:otherwise>
              <li><a href="${breadcrumb.url}">${breadcrumb.name}</a> <span
                  class="divider">/</span></li>
            </c:otherwise>
          </c:choose>
        </c:forEach>
      </ul>
    </div>
    </c:if>
    <div class="row">
      <div class="span14">
        <div class="alert" style="display:none" id="flash">&nbsp;</div>


