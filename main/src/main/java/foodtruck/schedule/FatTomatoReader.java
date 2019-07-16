package foodtruck.schedule;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;
import foodtruck.time.Clock;

public class FatTomatoReader implements StopReader {

  private static final Logger log = Logger.getLogger(FatTomatoReader.class.getName());

  private final CalendarAddressExtractor extractor;
  private final TruckDAO truckDAO;
  private final ZoneId zone;

  @Inject
  public FatTomatoReader(CalendarAddressExtractor extractor, TruckDAO truckDAO, ZoneId zoneId) {
    this.extractor = extractor;
    this.truckDAO = truckDAO;
    this.zone = zoneId;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {

    ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
    Optional<Truck> truckOpt = truckDAO.findByIdOpt("fattomatoinc");
    if (!truckOpt.isPresent()) {
      throw new RuntimeException("Fat tomato not found");
    }
    Truck truck = truckOpt.get();
    try {
      JSONObject eventsObj = new JSONObject(document);
      JSONArray events = eventsObj.getJSONArray("events");
      for (int i =0; i < events.length(); i++) {
        JSONObject event = events.getJSONObject(i);
        if (event.optBoolean("allDay")) {
          log.log(Level.FINE, "Skipping {0} because it is specified as allDay", event.getString("title"));
          continue;
        }
        String location = event.optString("location");
        if (Strings.isNullOrEmpty(location)) {
          location = event.getString("title");
        }
        OffsetDateTime startTime = OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(event.getString("start")));
        OffsetDateTime endTime = OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(event.getString("end")));
        Optional<Location> location1 = extractor.parse(location, truck);
        if (location1.isPresent()) {
          Location loc = location1.get();
          stops.add(TempTruckStop.builder()
              .calendarName(getCalendar())
              .locationName(loc.getName())
              .startTime(ZonedDateTime.ofInstant(startTime.toInstant(), zone))
              .endTime(ZonedDateTime.ofInstant(endTime.toInstant(), zone))
              .truckId(truck.getId())
              .build());
        }
      }

    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    return stops.build();
  }

  @Override
  public String getCalendar() {
    return "Fat Tomato";
  }
}
