<%@ include file="../common.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="author" content="Andrew Violette, @aviolette/@chifoodtruckz on twitter"/>
  <title>${title}</title>
  <%@ include file="../include/bootstrap_css.jsp" %>
  <link href="/css/foodtruckfinder.css" rel="stylesheet"/>
  <link rel="stylesheet" href="/css/dashboard.css"/>
  <script src="/script/lib/modernizr-1.7.min.js"></script>
  <%@include file="../include/core_js.jsp" %>
  <script src="/script/dashboard.js" type="text/javascript"></script>
  <script src="/script/lib/typeahead.bundle.js"></script>
</head>
<body>
<div class="container  cftf-main-container">
  <div id="topBar" class="navbar navbar-fixed-top navbar-inverse" role="navigation">
    <div class="container">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="/">FTF Admin Dashboard</a>
      </div>
      <div class="collapse navbar-collapse">
        <ul class="nav navbar-nav">
          <li class="<c:if test="${nav == 'trucks'}">active</c:if>"><a href="/admin/trucks">Trucks</a></li>
          <li<c:if test="${nav == 'locations'}"> class="active"</c:if>><a href="/admin/locations">Locations</a></li>

          <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
              Settings
              <b class="caret"></b>
            </a>
            <ul class="dropdown-menu">
              <li class="visible-desktop<c:if test="${nav == 'addresses'}"> active</c:if>"><a
                  href="/admin/addresses">Addresses</a>
              </li>
              <li class="hidden-phone<c:if test="${nav == 'applications'}"> active</c:if>"><a href="/admin/applications">Applications</a>
              </li>
              <li class="visible-desktop<c:if test="${nav == 'notifications'}"> active</c:if>"><a href="/admin/notifications">Notifications</a>
              </li>
              <li<c:if test="${nav == 'settings'}"> class="active"</c:if>><a href="/admin/configuration">Global Settings</a>
              </li>
              <li class="<c:if test="${nav == 'lookouts'}"> active</c:if>"><a href="/admin/lookouts">Lookouts</a>
              </li>
            </ul>
          </li>
          <li class="<c:if test="${nav == 'lookouts'}"> active</c:if>"><a href="/admin/messages">Messages</a>
          </li>
        </ul>
        <ul class="nav navbar-nav pull-right button-group-xs">
          <button class="btn btn-default btn-xs" style="margin: 10px 0 0 0" id="twitterButton"><span class="glyphicon glyphicon-refresh"></span> Refresh</button>
          <button class="btn btn-default btn-xs" style="margin: 10px 0 0 0" id="invalidateButton"><span class="glyphicon glyphicon-ban-circle"></span> Invalidate</button>
        </ul>
      </div>
    </div>
  </div>

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
