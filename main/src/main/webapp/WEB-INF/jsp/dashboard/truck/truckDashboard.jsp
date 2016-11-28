<%@include file="truckHeader.jsp" %>

<%@include file="../../include/truck_schedule_widget.jsp" %>


<div class="row">
  <div class="col-md-12">
    <h2>Daily Specials</h2>
    <table class="table table-striped">
      <tbody id="specialTable">
      </tbody>
    </table>
    <div class="btn-group">
      <button class="btn btn-default" id="addSpecial"><span class="glyphicon glyphicon-plus"></span>&nbsp;New Daily
        Special
      </button>
    </div>
  </div>
</div>


<h2>Stories</h2>
<table class="table table-striped">
  <thead>
  <tr>
    <td style="width: 100px">Time</td>
    <td>Location</td>
    <td>Source</td>
    <td>&nbsp;</td>
    <td>Text</td>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="tweet" items="${tweets}">
    <tr>
      <td style="width:100px !important"><a target="_blank" href="http://twitter.com/${tweet.screenName}/status/${tweet.id}"><joda:format value="${tweet.time}" style="-S"/></a></td>
      <td><ftl:location location="${tweet.location}"/>&nbsp;</td>
      <td>${tweet.storyType}</td>
      <td><a class="btn btn-default retweet-button" id="retweet-${tweet.id}" href="#"><span class="glyphicon glyphicon-retweet"></span> Retweet</a></td>
      <td><ftl:tweetformat>${tweet.text}</ftl:tweetformat></td>
    </tr>
  </c:forEach>
  </tbody>
</table>

&nbsp;


<script src="/script/truck_edit_widgetv2.js"></script>
<script type="text/javascript">

  (function () {
    TruckScheduleWidget.init("${truck.id}", ${locations},
        [<c:forEach var="category" varStatus="categoryIndex" items="${truck.categories}">"${category}"<c:if test="${!categoryIndex.last}">, </c:if></c:forEach>], {hasCalendar: ${not empty(truck.calendarUrl)}});


    $(".retweet-button").click(function (e) {
      e.preventDefault();
      var id = $(e.target).attr('id').substring(8);
      var account = prompt("With what account?");
      if (account) {
        var payload = {
          tweetId: id,
          account: account
        };
        $.ajax({
          url: "/services/tweets/retweets",
          type: 'POST',
          contentType: 'application/json',
          data: JSON.stringify(payload),
          complete: function () {
          },
          success: function () {

          }
        });
      }
    });


    var specials = null;

    function reloadSpecials() {
      $.ajax({
        url: "/services/trucks/${truckId}/specials",
        type: 'GET',
        dataType: 'json',
        success: function (data) {
          specials = data;
          var $specialsTable = $("#specialTable");
          $specialsTable.html("");
          $.each(specials["specials"], function (idx, foo) {
            $specialsTable.append($("<tr><td>" + foo["special"] + "</td><td><button row-idx=" + idx + " class='specialDeleteButton btn btn-default'><span class='glyphicon glyphicon-remove'></span></tr>"));
          });
          $(".specialDeleteButton").click(function (e) {
            var $target = $(e.target);
            specials["specials"].splice(parseInt($target.attr("row-idx")), 1);
            updateSpecials();
          })
        }
      });
    }

    function updateSpecials() {
      $.ajax({
        url: "/services/trucks/${truckId}/specials",
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(specials),
        complete: function () {
          reloadSpecials();
        },
        success: function () {
        }
      });
    }

    reloadSpecials();

    $("#addSpecial").click(function () {
      var name = prompt("Special name");
      if (!name) {
        return;
      }
      if (!specials) {
        specials = {truckId: "${truckId}", specials: []};
      }
      specials["specials"].push({"special": name, "soldout": false});
      updateSpecials();
    });


  })();

</script>
<%@include file="truckFooter.jsp" %>
