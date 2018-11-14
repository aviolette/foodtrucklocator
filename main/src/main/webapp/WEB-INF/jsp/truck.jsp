<%@ include file="header.jsp" %>

<div id="content">

  <nav aria-label="breadcrumb">
    <ol class="breadcrumb">
      <li class="breadcrumb-item" aria-current="page"><a href="/">Home</a></li>
      <li class="breadcrumb-item" aria-current="page"><a href="/trucks">Trucks</a></li>
      <li class="breadcrumb-item active" aria-current="page">${truck.name}</li>
    </ol>
  </nav>

  <div class="row" style="padding-bottom: 0;">
    <div class="col-md-4">
      <img class="img-thumbnail previewIcon" src="${truck.previewIconUrl.protocolRelative}" width="150" height="150"/>
      <div><c:if test="${isAdmin}">
        <a class="btn btn-default" href="/admin/trucks/${truck.id}"><span class='glyphicons glyphicons-pencil'></span> Edit</a>
        </c:if></div>
      <c:if test="${truck.popupVendor}"><p><span class="badge badge-info">Popup Vendor</span></p></c:if>
      <p class="lead">${truck.description}</p>
      <div>
        <c:if test="${!empty(truck.facebook)}"><a target="_blank" href="http://facebook.com${truck.facebook}"><img
            alt="Facebook" src="//storage.googleapis.com/ftf_static/img/facebook32x32.png"></a></c:if>
        <c:if test="${!empty(truck.twitterHandle)}"><a target="_blank"
                                                       href="http://twitter.com/${truck.twitterHandle}"><img
            alt="@${truck.twitterHandle} on twitter"
            src="//storage.googleapis.com/ftf_static/img/twitter32x32.png"></a></c:if>
        <c:if test="${!empty(truck.foursquareUrl)}"><a target="_blank"
                                                       href="http://foursquare.com/venue/${truck.foursquareUrl}"><img
            alt="Check in on foursquare" src="//storage.googleapis.com/ftf_static/img/foursquare32x32.png"></a></c:if>
        <c:if test="${!empty(truck.instagramId)}"><a target="_blank"
                                                     href="http://instagram.com/${truck.instagramId}"><img
            alt="View on instagram" src="//storage.googleapis.com/ftf_static/img/instagram32x32.png"></a></c:if>
      </div>
      <div style="padding-top:20px">
        <ul class="list-unstyled">
          <c:if test="${!empty(truck.url)}">
            <li class="lead"><a target="_blank" href="${truck.url}">${truck.url}</a></li>
          </c:if>
          <c:if test="${!empty(truck.publicEmail)}">
            <li><span class="glyphicons glyphicons-envelope"></span>&nbsp;
              <a target="_blank"
                 href="mailto:${truck.publicEmail}">${truck.publicEmail}</a></li>
          </c:if>
          <c:if test="${!empty(truck.phone)}">
            <li><span class="glyphicons glyphicons-phone-alt"></span>&nbsp;
                ${truck.phone}</li>
          </c:if>
        </ul>
      </div>
      <c:if test="${!empty(dailyData)}">
        <h2>Today's Specials</h2>
        <ul class="list-unstyled">
          <c:forEach items="${dailyData.specials}" var="special">
            <li class="lead">${special.special} <c:if test="${special.soldOut}"><span class="label label-danger">sold out!</span></c:if>
            </li>
          </c:forEach>
        </ul>
      </c:if>
    </div>
    <div class="col-md-8">
      <ul class="nav nav-tabs" role="tablist">
        <li role="presentation" class="nav-item"><a href="#schedule-section" aria-controls="schedule-section" role="tab"
                                                  data-toggle="tab" class="nav-link active">Schedule</a></li>
        <li role="presentation" class="nav-item"><a href="#menu-section" aria-controls="menu-section" role="tab"
                                   data-toggle="tab" class="nav-link">Menu</a></li>
      </ul>
      <div class="tab-content">
        <div class="tab-pane active" role="tabpanel" id="schedule-section">
          <div>
            <c:forEach items="${stops}" var="schedule" varStatus="scheduleStatus">
              <c:forEach items="${schedule.stops}" var="stop" varStatus="stopStatus">
                <c:if test="${stopStatus.first}">
                  <h2 class="date-header"><joda:format value="${stop.startTime}" pattern="EEEE MMM dd"/></h2>
                </c:if>
                <div class="row location-row<c:if test="${stop.activeNow}"> alert-success</c:if>"><!-- ${stop.key} -->
                  <div
                      class="<c:choose><c:when test="${empty(stop.description) and empty(stop.imageUrl)}">col-md-12</c:when><c:otherwise>col-md-5</c:otherwise></c:choose>"
                      style="padding-left:0">
                    <h3><c:choose><c:when test="${stop.fromBeacon && stop.activeNow}">

                      <em>Transmitting from beacon since <joda:format
                          value="${stop.startTime}"
                          style="-S"/> <span class='glyphicon glyphicon-flash'></span></em>

                        </c:when><c:otherwise>

                      <joda:format
                          value="${stop.startTime}"
                          style="-S"/> - <joda:format value="${stop.endTime}" style="-S"/>


                      </c:otherwise></c:choose>
                    </h3>
                    <p class="location"><span class="glyphicon glyphicon-map-marker"></span> <ftl:location
                        at="${stop.startTime}" location="${stop.location}"/></p>
                  </div>
                  <c:if test="${!empty(stop.description) or !empty(stop.imageUrl)}">
                    <div class="col-md-7">
                      <c:if test="${!empty(stop.description)}">
                        <p class="stop-description">${stop.description}</p>
                      </c:if>
                      <c:if test="${!empty(stop.imageUrl)}">
                        <div>
                          <img width="100%" src="${stop.imageUrl}"/>
                        </div>
                      </c:if>
                    </div>
                  </c:if>
                </div>
              </c:forEach>

            </c:forEach>
            <c:if test="${!hasStops}">
              <div class="row">
                <div class="col-md-12">
                  <p class="lead">There are no stops scheduled for this truck.</p>
                </div>
              </div>

            </c:if>
          </div>
        </div>
        <div class="tab-pane" role="tabpanel" id="menu-section">
          <c:choose>
            <c:when test="${!empty(menu)}">
              <div class="row" id="menu">

              </div>
            </c:when>
            <c:when test="${empty(menu) && !empty(truck.menuUrl)}">
              <div class="row" id="menu">
                <div class="col-md-12">
                  <a name="menulink"></a>
                  <div>
                    <img src="${truck.menuUrl}" width="100%" id="menu-image" title="Truck Menu"/>
                  </div>
                  <small><em>*disclaimer - this data may or may not be accurate or up-to-date.</em></small>
                </div>
              </div>
            </c:when>
            <c:otherwise>
              <div class="row">
                <div class="col-md-12">
                  <p class="lead">There is no published menu for this truck</p>
                </div>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>


</div>
<%@include file="include/core_js.jsp" %>
<c:if test="${!empty(menu)}">
  <script type="text/javascript" src="/script/menu.js"></script>
  <script type="text/javascript">
    renderMenu(${menu.scrubbedPayload}, $("#menu"));
  </script>
</c:if>


<%@ include file="footer.jsp" %>
