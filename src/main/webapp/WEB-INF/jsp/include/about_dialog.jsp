<%@include file="../common.jsp" %>
<div id="aboutDialog" class="modal hide fade">
  <div class="modal-header">
    <a href="#" class="close">&times;</a>

    <h3>About the Chicago Food Truck Finder</h3>
  </div>
  <div class="modal-body">

    <p>The Chicago Food Truck Finder locates food trucks and puts them on a map.</p>
    <h4>How does it work?</h4>

    <p>Data that is included on this website comes from multiple sources:
    <ol>
      <li>Schedules provided by trucks - Some trucks provide their schedules as google calendars. My
        website reads those in daily and uses the information in the entries to put them the map.
      </li>
      <li>Manual research - Some trucks publish data to their websites in a format that cannot be
        automatically parsed by my website, so some amount of manual entry is required. I usually do
        this at the beginning of the week when trucks publish their schedules. I am also on the
        lookout for food truck festivals and meetups and manually group these truck entries on my
        calendar.
      </li>
      <li>Twitter - My website scans twitter every few minutes for new tweets by trucks. If there is
        location and/or temporal data in it, new entries are added to my website. It also tries to
        figure out when trucks have left a location and terminates the stop.
      </li>
      <li>Foursquare - I do have a foursquare connector, but am not currently using it with any
        trucks
      </li>
    </ol>
    </p>
    <p>If a truck does not enter a time they are going to be at a spot or time they are going to
      leave, I have heuristics that attempt to determine when they will be there. It's not perfect,
      but I have been tweaking the algorithm for over a year and am pretty satisfied with the
      results.</p>
  </div>
</div>
