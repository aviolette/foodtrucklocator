(function () {
  let truckInfo = JSON.parse($("#truck-info").text());

  TruckScheduleWidget.init(truckInfo.truckId, truckInfo.locations,
      truckInfo.categories, {hasCalendar: false});

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
})();
