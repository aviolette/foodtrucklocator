<%@ include file="dashboardHeader.jsp" %>

<div class="btn-toolbar">
  <div class="btn-group">
    <a href="/cron/tweets" class="btn" id="twitterButton"><i class="icon-refresh"></i>&nbsp;Refresh all tweets</a>
    <a href="/cron/invalidateCache" class="btn" id="invalidateButton"><i class="icon-ban-circle"></i>&nbsp;Invalidate cache</a>
  </div>
  <div class="btn-group toggle-visibility" >
    <button id="inactiveButton" type="button" class="btn active">Inactive</button>
    <button id="muteButton" type="button" class="btn active">Muted</button>
  </div>
  <div class="btn-group">
    <a href="#" class="btn" id="newTruck">New Truck</a>&nbsp;
  </div>
</div>
<h3>Active Trucks</h3>
<table class="table table-striped active-trucks">
  <thead>
  <tr>
    <th class="gutter">&nbsp;</th>
    <th>Truck</th>
    <th>Current Location</th>
    <th>Ends At</th>
    <th>Starts At</th>
    <th>Next Location</th>
    <th>Categories</th>
    <th>Twittalyzer</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" varStatus="truckStopsStatus" items="${trucks}">
    <c:if test="${truckStops.active}">

      <tr class="rowItem">
        <td class="gutter">&nbsp;</td>
        <td><a class="truckLink" href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
        <td>
          <c:choose>
          <c:when
              test="${!empty(truckStops.currentStop)}">
          <c:url value="/admin/locations"
                 var="locationUrl">
            <c:param name="q" value="${truckStops.currentStop.location.name}"/>
          </c:url> <a
            href="${locationUrl}">${truckStops.currentStop.location.name}</a></td>
        <td><joda:format value="${truckStops.currentStop.endTime}" style="-S"/></c:when><c:otherwise>
          Not Active
        </td>
        <td>&nbsp;</c:otherwise>
          </c:choose>

        </td>
        <td>
          <c:choose>
          <c:when
              test="${!empty(truckStops.nextStop)}">
          <c:url value="/admin/locations" var="locationUrl">
            <c:param name="q" value="${truckStops.nextStop.location.name}"/>
          </c:url> <joda:format value="${truckStops.nextStop.startTime}" style="-S"/></td>
        <td><a
            href="${locationUrl}">${truckStops.nextStop.location.name}</a></c:when>
          <c:otherwise></td><td>None</c:otherwise>
          </c:choose>
        </td>
        <td>${truckStops.truck.categoryList}</td>
        <td><c:choose><c:when
            test="${truckStops.truck.usingTwittalyzer}"></c:when><c:otherwise><span
            class="label warning">off</span></c:otherwise></c:choose></td>
      </tr>
    </c:if>
  </c:forEach>
  </tbody>
</table>
<h3>Trucks That Are Inactive Today</h3>
<table class="table table-striped inactive-trucks">
  <thead>
  <tr>
    <th></th>
    <th>Truck</th>
    <th>Categories</th>
    <th>Twittalyzer</th>
    <th>&nbsp;</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="truckStops" items="${trucks}">
    <c:if test="${!truckStops.active && !truckStops.truck.inactive}">
      <tr <c:choose><c:when test="${truckStops.truck.muted}">class="muted rowItem"</c:when><c:otherwise>class="rowItem"</c:otherwise></c:choose>>
        <td class="gutter"></td>
        <td><a class="truckLink" href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
        <td>${truckStops.truck.categoryList}</td>
        <td><c:choose><c:when
            test="${truckStops.truck.usingTwittalyzer}"></c:when><c:otherwise><span
            class="label warning">off</span></c:otherwise></c:choose></td>
        <td>
          <button class="btn mute-button" for-truck="${truckStops.truck.id}"><c:choose><c:when
              test="${truckStops.truck.muted}">Unmute</c:when><c:otherwise>Mute</c:otherwise></c:choose></button>
        </td>
      </tr>
    </c:if>
  </c:forEach>
  </tbody>
</table>
<div id="inactiveTrucks" style="display:none">
  <h3>Inactive Trucks</h3>
  <table class="table table-striped">
    <thead>
    <tr>
      <th>Truck</th>
      <th>Twittalyzer</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="truckStops" items="${trucks}">
      <c:if test="${!truckStops.active && truckStops.truck.inactive}">
        <tr class="rowItem">
          <td><a href="/admin/trucks/${truckStops.truck.id}">${truckStops.truck.name}</a></td>
          <td>${truckStops.truck.categoryList}</td>
          <td><c:choose><c:when
              test="${truckStops.truck.usingTwittalyzer}"></c:when><c:otherwise><span
              class="label warning">off</span></c:otherwise></c:choose></td>
        </tr>
      </c:if>
    </c:forEach>
    </tbody>
  </table>
</div>
<script type="text/javascript">
  (function() {

    var Cursor = {
      current : function() {
        return this.$currentSelection;
      },
      init : function() {
        var self = this;
        self.currentTable = "table.inactive-trucks";
        self.$currentSelection = $(self.currentTable + " tr.rowItem").first();
        if (self.$currentSelection.length == 0) {
          self.currentTable = "table.active-trucks";
          self.$currentSelection = $(self.currentTable + " tr.rowItem").first();
        }
        self.$currentSelection.addClass("selected");
      },
      next : function() {
        var $item = this.$currentSelection;
        while (true) {
          $item = $item.next();
          if ($item.css("display") != "none"|| $item.length == 0) {
            break;
          }
        }
        if ($item.length == 0 && this.currentTable == "table.active-trucks") {
          this.currentTable = "table.inactive-trucks";
          $item = $(this.currentTable + " tr.rowItem").first();
        }
        if ($item.length > 0) {
          this.$currentSelection.removeClass("selected");
          $item.first().addClass("selected");
          this.$currentSelection = $item;
          this.scrollIfNecessary();
        }
      },
      scrollIfNecessary : function() {
        var $window = $(window);
        var bottom = this.$currentSelection.position().top;
        if (bottom > ($window.scrollTop() + $window.height())) {
          window.scrollTo(0, bottom + this.$currentSelection.height());
        } else if ((bottom - this.$currentSelection.height()) < $window.scrollTop()) {
          window.scrollTo(0, bottom - this.$currentSelection.height());
        }
      },
      prev : function() {
        var $item = this.$currentSelection;
        while (true) {
          $item = $item.prev();
          if ($item.css("display") != "none" || $item.length == 0) {
            break;
          }
        }
        if ($item.length == 0 && this.currentTable == "table.inactive-trucks") {
          this.currentTable = "table.active-trucks";
          $item = $(this.currentTable + " tr.rowItem").last();
        }
        if ($item.length > 0) {
          this.$currentSelection.removeClass("selected");
          $item.first().addClass("selected");
          this.$currentSelection = $item;
          this.scrollIfNecessary();
        }
      }
    };

    function toggleMuted($muteButton) {
      var displayValue = $muteButton.hasClass("active") ? "table-row" : "none";
      $(".muted").css("display", displayValue);
    }

    Cursor.init();
    $(document).keypress(function(e) {
      switch(e.which) {
        case 111: //o
          e.preventDefault();
          Cursor.current().find("a.truckLink").each(function(i, item) {
            location.href = $(item).attr("href");
          });
          break;
        case 109: //m
          e.preventDefault();
          Cursor.current().find(".mute-button").each(function(i, item) {
            muteButtonClick($(item));
            Cursor.next();
          });
          break;
        case 106:  //j
          e.preventDefault();
          Cursor.next();
          break;
        case 107: //k
          e.preventDefault();
          Cursor.prev();
          break;
      }
    });
    $('.toggle-visibility button').click(function(e) {
      var $target = $(e.target);
      if ($target.attr("id") == 'inactiveButton') {
        var displayValue = $target.hasClass("active") ? "block" : "none";
        $("#inactiveTrucks").css("display", displayValue);
      } else {
        var displayValue = $target.hasClass("active") ? "table-row" : "none";
        $(".muted").css("display", displayValue);
      }
      if ($target.hasClass("active")) {
        $target.removeClass("active");
      } else {
        $target.addClass("active");
      }
    });
    function muteButtonClick($button) {
      var truckId = $button.attr("for-truck");
      var inner = $button.html();
      var verb = inner == "Mute" ? "mute" : "unmute";
      $.ajax({
        url : "/services/trucks/" + truckId + "/" + verb,
        type: "POST",
        success : function() {
          $button.html(verb == "mute" ? "Unmute" : "Mute");
          if (verb == "mute") {
            $button.parent().parent().addClass("muted");
          } else {
            $button.parent().parent().removeClass("muted");
          }
          var displayValue = $("#muteButton").hasClass("active") ? "none" : "table-row";
          $(".muted").css("display", displayValue);
        }
      });
    }
    $(".mute-button").click(function(e) {
      muteButtonClick($(e.target));
    });
    function bindAjaxCallToButton(button, url) {
      var link = $("#" + button);
      link.click(function(evt) {
        evt.preventDefault();
        link.addClass("disabled");
        $.ajax({
          context: document.body,
          url: url,
          complete : function() {
            link.removeClass("disabled");
          },
          success: function() {
            window.location.reload();
          }
        });
      });
    }

    bindAjaxCallToButton("twitterButton", "/cron/tweets");
    bindAjaxCallToButton("invalidateButton", "/cron/invalidateCache");
    function newTruckDialog() {
      var truckId = prompt("Enter truck ID:");
      if (!truckId) {
        return;
      }
      $.ajax({
        url : "/services/trucks",
        type: "POST",
        contentType: "application/json",
        data : JSON.stringify({id : truckId, name : "UNNAMED-" + truckId, twitterHandle: truckId}),
        success : function() {
          location.href = "/admin/trucks/" + truckId
        }
      });
    }

    $("#newTruck").click(function(e) {
      e.preventDefault();
      newTruckDialog();
    });

  })();
</script>

<%@ include file="dashboardFooter.jsp" %>