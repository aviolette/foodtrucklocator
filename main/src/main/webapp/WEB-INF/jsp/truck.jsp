<%@ include file="header.jsp" %>
<%@ include file="include/rickshaw_css.jsp" %>
<style>
  dl.schedule {
    font-size: 125%;
  }

  dl.schedule dd {
    padding-bottom: 10px;
  }

  h3.panel-title {
    font-size: 20px;
  }
  .tab-content {
    padding-top:20px;
  }
  h2.date-header {
    background-color: #5e5e5e;
    color:white;
    padding: 5px 0 5px 10px;
  }
</style>

<div id="content">
  <ol class="breadcrumb">
    <li><a href="/trucks">Trucks</a></li>
    <li class="active">${truck.name}</li>
  </ol>

  <div
      style="height:300px;background-image:url('${truck.biggestBackgroundImageUrl.protocolRelative}');background-size:100% auto"></div>

  <div class="row top-row" style="padding-bottom: 0;">
    <div class="col-md-6">
      <img class="img-rounded previewIcon" src="${truck.previewIconUrl.protocolRelative}" width="150" height="150"/>
    </div>
  </div>
  <div class="row second-top-row">
    <div class="col-md-4">
      <h1>${truck.name}<c:if test="${isAdmin}">
        <a class="btn btn-default" href="/admin/trucks/${truck.id}"><span class='glyphicon glyphicon-pencil'></span></a>
        </c:if></h1>
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
            <li><span class="glyphicon glyphicon-envelope"></span>&nbsp;
              <a target="_blank"
                 href="mailto:${truck.publicEmail}">${truck.publicEmail}</a></li>
          </c:if>
          <c:if test="${!empty(truck.phone)}">
            <li><span class="glyphicon glyphicon-earphone"></span>&nbsp;
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
        <li role="presentation" class="active"><a href="#schedule-section" aria-controls="schedule-section" role="tab" data-toggle="tab">Upcoming Schedule</a></li>
        <li role="presentation"><a href="#menu-section" aria-controls="menu-section" role="tab" data-toggle="tab">Menu</a></li>
      </ul>
      <div class="tab-content">
        <div class="tab-pane active" role="tabpanel" id="schedule-section">
          <div>
            <c:forEach items="${stops}" var="schedule" varStatus="scheduleStatus">

              <c:forEach items="${schedule.stops}" var="stop" varStatus="stopStatus">
                <c:if test="${stopStatus.first}">
                  <h2 class="date-header"><joda:format value="${stop.startTime}" pattern="EEEE MMMM dd"/></h2>
                </c:if>
                <div class="row<c:if test="${stop.activeNow}"> alert-success</c:if>"><!-- ${stop.key} -->
                <div class="col-md-4" style="padding-left:0">
                  <h3><joda:format
                      value="${stop.startTime}"
                      style="-S"/> - <joda:format value="${stop.endTime}" style="-S"/></h3>
                  <p class="lead"><span class="glyphicon glyphicon-map-marker"></span> <ftl:location at="${stop.startTime}" location="${stop.location}"/></p>
                </div>
                  <div class="col-md-8">
                    <c:if test="${!empty(stop.description)}">
                      <p class="stop-description">${stop.description}</p>
                    </c:if>
                    <c:if test="${!empty(stop.imageUrl)}">
                      <div>
                        <img src="${stop.imageUrl}"/>
                      </div>
                    </c:if>
                  </div>
                </div>
              </c:forEach>

            </c:forEach>
          </div>
        </div>
        <div class="tab-pane" role="tabpanel" id="menu-section">
          <c:choose>
            <c:when test="${!empty(menu)}">
              <div class="row" id="menu">

              </div>
            </c:when>
            <c:when test="${empty(meny) && !empty(truck.menuUrl)}">
              <div class="row" id="menu">
                <div class="col-md-6">
                  <a name="menulink"></a>
                  <div>
                    <a target="_blank" href="${truck.menuUrl}">Click here to see this truck's current menu!</a>
                  </div>
                  <small><em>*disclaimer - this data may or may not be accurate or up-to-date.</em></small>
                </div>
              </div>
            </c:when>
          </c:choose>
        </div>
      </div>
    </div>
  </div>



</div>
<%@include file="include/core_js.jsp" %>
<script>
  <c:if test="${!empty(menu)}">
  (function () {
    var menuJSON = ${menu.scrubbedPayload}, $menu = $("#menu");

    function addSection(sectionName, description) {
      var body = (description) ? "<div class='panel-body'><p>" + description + " </p></div>" : "";
      var $panelSection = $("<div class='col-md-6'><div class='panel panel-default'><div class='panel-heading'><h3 class='panel-title'>" + sectionName + "</h3></div>" + body + "<div class='list-group'></div></div></div>")
      $menu.append($panelSection);
      var items = $menu.find("div.list-group");
      return $(items[items.length - 1]);
    }

    function addItem($dl, name, description) {
      $dl.append("<span class='list-group-item'><h4>" + name + "</h4><p>" + description + "</p></span>");
    }

    $.each(menuJSON["sections"], function (i, section) {
      var $section = addSection(section["section"], section["description"]);
      $.each(section["items"], function (j, item) {
        addItem($section, item["name"], item["description"]);
      });
    });
  })();
  </c:if>
</script>


<%@ include file="footer.jsp" %>
