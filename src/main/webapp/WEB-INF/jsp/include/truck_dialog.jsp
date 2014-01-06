<%@include file="../common.jsp" %>
<div id="truckDialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">

    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <div class="media">
          <span class="pull-left"><img id="truckIcon" class="media-object" src=""/></span>
          <div class="media-body">
            <h3 id="truckTitle"></h3>
          </div>
        </div>
      </div>
      <div class="modal-body">
        <div class="menuContent">
          <div id="truckSocial" class="infoRow"></div>
        </div>
        <div id="truckInfo"></div>
        <h3>Scheduled Stops</h3>
        <table class="table"><tbody id="truckSchedule">
        </tbody></table>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
      </div>
    </div>
  </div>
</div>