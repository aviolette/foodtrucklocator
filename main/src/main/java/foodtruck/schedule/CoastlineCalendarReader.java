package foodtruck.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
    Document parsedDoc = Jsoup.parse(document);
    TempTruckStop.Builder builder = null;
    ImmutableList.Builder stops = ImmutableList.builder();
    int current = -1;
    LocalDate date = null;
    LocalTime startTime = null;
    LocalTime endTime = null;
    String location = null;
    Truck truck = truckDAO.findByIdOpt("coastlinecove")
        .orElseThrow(() -> new RuntimeException("Coastline Cove truck not found"));
    for (Element item : parsedDoc.select(".x-d-route")) {
      String route = item.attr("data-route");
      if (!route.startsWith("events/")) {
        continue;
      }
      String routes[] = route.split("/");
      int index = Integer.parseInt(routes[1]);
      if (index != current) {
        if (builder != null) {
          buildAndAddStop(builder, stops, date, startTime, endTime, location);
        }
        date = null;
        startTime = null;
        endTime = null;
        current = index;
        location = null;
        builder = TempTruckStop.builder()
            .calendarName(CALENDAR_NAME)
            .truckId(truck.getId());
      }
      String key = routes[2];
      switch (key) {
        case "date":
          date = parseDate(item.text());
          break;
        case "start":
          startTime = parseTime(item.text());
          break;
        case "end":
          endTime = parseTime(item.text());
          break;
        case "location": {
            Optional<String> loc = extractor.parse(item.text(), truck)
                .stream()
                .findFirst();
            location = loc.orElse(null);
          }
          break;
        case "title":
          if (location == null) {
            Optional<String> loc = extractor.parse(item.text(), truck)
                .stream()
                .findFirst();
            location = loc.orElse(null);
          }
          break;
      }
    }
    if (builder != null) {
      buildAndAddStop(builder, stops, date, startTime, endTime, location);
    }
    return stops.build();
  }

  private void buildAndAddStop(TempTruckStop.Builder builder, ImmutableList.Builder<TempTruckStop> stops, LocalDate date,
      LocalTime startTime, LocalTime endTime, String location) {
    builder.locationName(location);
    if (date != null && startTime != null && endTime != null) {
      builder.startTime(ZonedDateTime.of(date, startTime, zone));
      builder.endTime(ZonedDateTime.of(date, endTime, zone));
    }
    if (builder.isComplete()) {
      stops.add(builder.build());
    }
  }

  private LocalTime parseTime(String text) {
    return LocalTime.from(timeFormatter.parse(massage(text)));
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
    time = time.replaceAll("\\.", "");
    time = time.toUpperCase();
    if (time.endsWith(" AM") || time.endsWith(" PM")) {
      if (time.contains(":")) {
        return time;
      }
      int index = time.indexOf(" ");
      return time.substring(0, index) + ":00" + time.substring(index);
    }
    if (time.length() < 5) {
      return time + " PM";
    } else if (time.length() == 5 && time.substring(0, 1)
        .equals("1")) {
      return time + " AM";
    }
    return time;
  }
}
