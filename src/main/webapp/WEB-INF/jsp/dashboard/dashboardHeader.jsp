<%@ include file="../common.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Administrative Dashboard</title>
  <meta name="description" content="Administrative Dashboard">
  <meta name="author" content="Andrew Violette">

  <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
  <!--[if lt IE 9]>
  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
  <![endif]-->

  <script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
  <script>window.jQuery ||
  document.write('<script src="/script/lib/jquery-1.6.2.min.js"><\/script>')</script>
  <script src="/script/dashboard.js" type="text/javascript"></script>
  <link href="/bootstrap/bootstrap.css" rel="stylesheet">
  <link rel="stylesheet" href="/css/dashboard.css"/>

  <style type="text/css">

  </style>

  <!-- Le fav and touch icons -->
  <link rel="shortcut icon" href="/favicon.ico">
</head>

<body>

<div class="topbar">
  <div class="fill">
    <div class="container">
      <a class="brand" href="#">CFTF - Admin Dashboard</a>
      <ul class="nav">
        <li<c:if test="${nav == 'trucks'}"> class="active"</c:if>><a href="/admin/trucks">Trucks</a>
        </li>
        <li<c:if test="${nav == 'locations'}"> class="active"</c:if>><a href="/admin/locations">Locations</a>
        </li>
        <li<c:if test="${nav == 'settings'}"> class="active"</c:if>><a href="/admin/configuration">Settings</a>
        </li>
        <li<c:if test="${nav == 'stats'}"> class="active"</c:if>><a
            href="/admin/stats">Statistics</a>
        </li>
      </ul>
    </div>
  </div>
</div>

<div class="container">

  <div class="content">
    <div class="page-header">
      <h1><c:choose><c:when
          test="${empty(headerName)}">Dashboard</c:when><c:otherwise>${headerName}</c:otherwise></c:choose>
      </h1>
      <c:if test="${!empty(breadcrumbs)}">
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
      </c:if>
    </div>
    <div class="row">
      <div class="span14">
        <div class="alert-message" style="display:none" id="flash">&nbsp;</div>


