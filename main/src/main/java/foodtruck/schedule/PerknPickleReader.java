package foodtruck.schedule;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.LocationDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.util.MoreStrings;

public class PerknPickleReader implements StopReader {

  private static final Logger log = Logger.getLogger(PerknPickleReader.class.getName());
  private final ZoneId zone;
  private final LocationDAO locationDAO;

  @Inject
  public PerknPickleReader(ZoneId zone, LocationDAO locationDAO) {
    this.zone = zone;
    this.locationDAO = locationDAO;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    ImmutableList.Builder<TempTruckStop> builder = ImmutableList.builder();
    try {
      JSONObject obj = new JSONObject(document);
      JSONArray events = obj.getJSONArray("events");
      for (int i=0; i < events.length(); i++) {
        JSONObject event = events.getJSONObject(i);
        OffsetDateTime start = OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(event.getString("start")));
        ZonedDateTime startTime = ZonedDateTime.ofInstant(start.toInstant(), zone);
        OffsetDateTime end = OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(event.getString("end")));
        ZonedDateTime endTime = ZonedDateTime.ofInstant(end.toInstant(), zone);
        String locationName = MoreStrings.firstNonEmpty(event.optString("location"), event.getString("title"));
        locationDAO.findByAliasOpt(locationName).ifPresent(location -> {
          if (location.isResolved()) {
            builder.add(TempTruckStop.builder()
                .truckId("perknpickle")
                .locationName(location.getName())
                .calendarName(getCalendar())
                .startTime(startTime)
                .endTime(endTime)
                .build());

          }
        });
      }
    } catch (JSONException e) {
      log.log(Level.INFO, e.getMessage(), e);
      return ImmutableList.of();
    }
    return builder.build();
  }

  @Override
  public String getCalendar() {
    return "perknpickle";
  }
}
