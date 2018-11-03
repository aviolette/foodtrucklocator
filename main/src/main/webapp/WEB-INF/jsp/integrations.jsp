<%@ include file="header.jsp" %>

<style>
  .media {
    margin: 30px 0;
  }
  .app-icon {
    min-width:206px;
  }
</style>
<div class="row">
  <div class="col-md-12">

    <p class="lead">Aside from the website, you can also find out about Chicago food trucks from these integrations</p>

    <div class="media">
      <img class="mr-4"  src="/img/alexa_horizontal_RGB_black_206.png"/>
      <div class="media-body">
        <p>You can enable the <a target="_blank" href="https://www.amazon.com/Andrew-Violette-Chicago-Truck-Finder/dp/B01LBSEQ4U">Chicago Food Truck Finder Alexa Skill</a> to find out information about what trucks are near you.</p>
      </div>
    </div>
    <div class="media">
      <img class="mr-4" src="/img/ChromeWebStore_Badge_v2_206x58.png"/>
      <div class="media-body">
        <p>The <a target="_blank" href="https://chrome.google.com/webstore/detail/food-truck-finder-notifie/hapnieohgibnoaldifflafpcflcicdlc">Chicago Food Truck Finder Chrome Notifier</a> provides an icon in Google Chrome that indicates how many trucks are near you.
          It will provide notifications when new trucks enter your area.</p>
      </div>
    </div>
<%--
    <div class="media">
      <a class="mr-4 app-icon" href="https://slack.com/oauth/authorize?scope=incoming-webhook&client_id=438175788853.466163281394"><img alt="Add to Slack" height="40" width="139" src="https://platform.slack-edge.com/img/add_to_slack.png" srcset="https://platform.slack-edge.com/img/add_to_slack.png 1x, https://platform.slack-edge.com/img/add_to_slack@2x.png 2x" /></a>
      <div class="media-body">
        <p>The Slack integration is a webhook that sends notifications about trucks that are in your area.  Click the button on the left to add it to your workspace</p>
      </div>
    </div>
    --%>
  </div>
</div>




<%@include file="include/core_js.jsp" %>
<%@ include file="footer.jsp" %>
