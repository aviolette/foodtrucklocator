<%@include file="dashboardHeader.jsp" %>

<a href="/cron/tweets" class="btn primary" id="twitterButton">Twittalyze</a>

<script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
<script type="text/javascript">
  (function() {
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
            alert("Success");
          }
        });
      });
    }

    bindAjaxCallToButton("twitterButton", "/cron/tweets");
  })();
</script>


<%@include file="dashboardFooter.jsp" %>