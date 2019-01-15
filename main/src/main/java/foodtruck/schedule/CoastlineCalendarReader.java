package foodtruck.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 2018-12-18
 */
public class CoastlineCalendarReader implements StopReader {

  private static final Logger log = Logger.getLogger(CoastlineCalendarReader.class.getName());
  private static final String CALENDAR_NAME = "coastlinecove";
  private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");
  private static DateTimeFormatter dateFormatter2 = DateTimeFormatter.ofPattern("M/d/yyyy");
  private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
  private final AddressExtractor extractor;
  private final TruckDAO truckDAO;
  private final ZoneId zone;

  @Inject
  public CoastlineCalendarReader(AddressExtractor extractor, TruckDAO truckDAO, ZoneId zone) {
    this.extractor = extractor;
    this.truckDAO = truckDAO;
    this.zone = zone;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    log.info("Loading coastline's calendar");
    int index = document.indexOf("calendar.apps.dev");
    if (index == -1) {
      log.log(Level.SEVERE, "Calendar section is not found");
      return ImmutableList.of();
    }
    index = document.indexOf("var props = ", index);
    if (index == -1) {
      log.log(Level.SEVERE, "Props not found in coastline document");
      return ImmutableList.of();
    }
    Truck truck = truckDAO.findByIdOpt("coastlinecove")
        .orElseThrow(() -> new RuntimeException("Coastline Cove truck not found"));
    String json = document.substring(index + 11, document.indexOf(';', index + 11));
    try {
      JSONObject obj = new JSONObject(json);
      JSONArray events = obj.getJSONArray("manualEvents");
      ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
      for (int i = 0; i < events.length(); i++ ) {
        JSONObject event = events.getJSONObject(i);
        String start = massage(event.getString("start").replaceAll("\\.", "").toUpperCase());
        String end = event.getString("end").replaceAll("\\.", "").toUpperCase();

        LocalDate date = parseDate(event.getString("date"));
        extractor.parse(event.getString("location"), truck)
            .forEach(location -> stops.add(
                TempTruckStop.builder()
                    .truckId(truck.getId())
                    .startTime(date.atTime(LocalTime.from(timeFormatter.parse(start)))
                        .atZone(zone))
                    .endTime(date.atTime(LocalTime.from(timeFormatter.parse(end)))
                        .atZone(zone))
                    .calendarName(getCalendar())
                    .locationName(location)
                    .build()));
      }
      return stops.build();
    } catch (JSONException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private LocalDate parseDate(String date) {
    try {
      return LocalDate.from(dateFormatter.parse(date));
    } catch (DateTimeParseException dtpe) {
      return LocalDate.from(dateFormatter2.parse(date));
    }

  }

  @Override
  public String getCalendar() {
    return CALENDAR_NAME;
  }

  private String massage(String time) {
    if (time.length() < 5) {
      return time + " PM";
    } else if (time.length() == 5 && time.substring(0, 1).equals("1")) {
      return time + " AM";
    }
    return time;
  }
}
