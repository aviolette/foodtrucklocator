<%@include file="../common.jsp" %>
<div id="truckDialog" class="modal hide fade">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>

    <h3 id="truckTitle"></h3>
  </div>
  <div class="modal-body">
    <div id="truckIconDiv" class="truckSection">
      <div class="iconSection">
        <img id="truckIcon"/>
      </div>
      <div class="menuContent">
        <div id="truckSocial" class="infoRow"></div>
      </div>
    </div>
    <div id="truckInfo"></div>
    <h3>Scheduled Stops</h3>
    <ul class="unstyled" id="truckSchedule"></ul>
  </div>
  <div class="modal-footer">
    <button type="button" class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
  </div>
</div>
