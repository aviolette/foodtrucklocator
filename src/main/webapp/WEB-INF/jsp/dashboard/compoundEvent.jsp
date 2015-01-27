<%@ include file="dashboardHeaderBS3.jsp" %>

<h2>New event at ${location.name}</h2>

<form role="form" action="" method="post">
    <div class="form-group">
        <label class="control-label" for="startTimeInput">Start</label>
        <input name="startTime" class="timeentry form-control" id="startTimeInput" value="${startTime}" type="datetime-local" autofocus/>
    </div>
    <div class="form-group">
        <label class="control-label" for="endTimeInput">End</label>
        <input name="endTime" class="timeentry form-control" id="endTimeInput" value="${endTime}" type="datetime-local"/>
    </div>
    <div class="form-group">
        <label class="control-label" for="truckList">Trucks</label>
        <select id="truckList" name="trucks" class="form-control" multiple>
            <c:forEach var="truck" items="${trucks}">
                <option value="${truck.id}">${truck.name}</option>
            </c:forEach>
        </select>
    </div>
    <a href="/admin/locations/${location.key}" class="btn">Cancel</a>
    <input type="submit" class="btn btn-primary" value="Create"/>
</form>

<%@ include file="dashboardFooterBS3.jsp" %>
