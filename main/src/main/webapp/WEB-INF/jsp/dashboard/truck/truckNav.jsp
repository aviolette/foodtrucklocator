<%@ include file="../../common.jsp" %>

<ul class="nav nav-bordered mt-4 mt-md-2 mb-3 clearfix" role="tablist">
  <li class="nav-item" role="presentation">
    <a class="nav-link <c:if test='${headerSelection == "main"}'>active show</c:if>"  href="/admin/trucks/${truck.id}">Overview</a>
  </li>
  <li class="nav-item"><a class="nav-link <c:if test='${headerSelection == "config"}'>active show</c:if>" href="/admin/trucks/${truck.id}/configuration">Configuration</a></li>
  <li class="nav-item"><a class="nav-link <c:if test='${headerSelection == "stats"}'>active show</c:if>" href="/admin/trucks/${truck.id}/stats">Stats</a></li>
  <li class="nav-item"><a class="nav-link <c:if test='${headerSelection == "beacon"}'>active show</c:if>" href="/admin/trucks/${truck.id}/beacons">Beacons</a></li>
  <li class="nav-item"><a class="nav-link <c:if test='${headerSelection == "danger"}'>active show</c:if>" href="/admin/trucks/${truck.id}/danger">Danger Zone</a></li>
  <li class="nav-item"><a class="nav-link" href="/trucks/${truck.id}">Public Page</a></li>
</ul>

