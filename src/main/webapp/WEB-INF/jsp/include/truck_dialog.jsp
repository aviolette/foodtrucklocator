<%@include file="../common.jsp" %>
<div id="truckDialog" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <div class="media">
          <div class="text-center">
            <img id="truckIcon" class="center-block img-thumbnail" src=""/>
          </div>
          <div class="media-body">
            <h3 id="truckTitle" class=" text-center"></h3>
            <p class="text-center hidden" id="truck-url"></p>
          </div>
        </div>
      </div>
      <div class="modal-body">
        <div class="menuContent">
          <div id="truckSocial" class="infoRow"></div>
        </div>
        <div id="truckInfo"></div>
        <div style="margin-top:10px">
          <a href="" id="truckInfoLink">View complete profile...</a>
        </div>
        <h3>Scheduled Stops</h3>
        <table class="table"><tbody id="truckSchedule">
        </tbody></table>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal" aria-hidden="true">Close</button>
      </div>
    </div>
  </div>
</div>