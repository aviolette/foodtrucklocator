package foodtruck.schedule;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.TempTruckStop;
import foodtruck.time.Clock;

import static foodtruck.schedule.SimpleCalReader.inferTruckId;

/**
 * @author aviolette
 * @since 2018-12-23
 */
public class PollyannaReader implements StopReader {

  private static final Logger log = Logger.getLogger(PollyannaReader.class.getName());
  public static final String CALENDAR_NAME = "pollyanna";

  private final Clock clock;

  @Inject
  public PollyannaReader(Clock clock) {
    this.clock = clock;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    try {
      ZonedDateTime now = clock.now8();
      JSONObject doc = new JSONObject(document);
      ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
      JSONArray events = doc.getJSONObject("project")
          .getJSONObject("data")
          .getJSONArray("events");
      for (int i=0; i < events.length(); i++) {
        JSONObject event = events.getJSONObject(i);

        Instant endTimeAtStartOfDay = Instant.ofEpochMilli(event.getLong("end"));
        OffsetDateTime dateInUTC = OffsetDateTime.ofInstant(endTimeAtStartOfDay, ZoneOffset.UTC);
        LocalDate eventDate = dateInUTC.toLocalDate();
        ZonedDateTime endTime = ZonedDateTime.of(eventDate,
            LocalTime.of(event.getInt("endHour"), event.getInt("endMinutes")), clock.zone8());
        if (now.isAfter(endTime)) {
          continue;
        }

        String location = event.getString("location");
        if (!location.startsWith("Pollyanna Brewing")) {
          continue;
        } else if (location.contains("Roselle")) {
          location = "Pollyanna Brewing - Roselle";
        } else {
          location = "Pollyanna Brewing Co.";
        }

        if (event.optBoolean("allday")) {
          log.log(Level.FINE, "skipping all day event {0}", event);
          continue;
        }

        String truckId = inferTruckId(event.getString("title"));
        if (truckId == null) {
          truckId = inferTruckId(event.getString("description"));
          if (truckId == null) {
            continue;
          }
        }

        ZonedDateTime startTime = ZonedDateTime.of(eventDate, LocalTime.of(event.getInt("startHour"), event.getInt("startMinutes")), clock.zone8());

        stops.add(TempTruckStop.builder()
            .truckId(truckId)
            .locationName(location)
            .calendarName(CALENDAR_NAME)
            .endTime(endTime)
            .startTime(startTime)
            .build());

      }
      return stops.build();
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getCalendar() {
    return CALENDAR_NAME;
  }
}
