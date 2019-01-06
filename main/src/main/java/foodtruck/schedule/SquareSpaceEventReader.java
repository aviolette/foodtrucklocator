package foodtruck.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import foodtruck.model.TempTruckStop;

import static foodtruck.schedule.SimpleCalReader.inferTruckId;

/**
 * @author aviolette
 * @since 2018-12-27
 */
public class SquareSpaceEventReader implements StopReader {

  private final ZoneId zoneId;

  @Inject
  public SquareSpaceEventReader(ZoneId zoneId) {
    this.zoneId = zoneId;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    Document parsedDoc = Jsoup.parse(document);
    ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
    String siteName = parsedDoc.select("meta[property=\"og:site_name\"]").attr("content");
    for (Element parent : parsedDoc.select("div.eventlist-column-info")) {
      TempTruckStop.Builder builder = TempTruckStop.builder()
          .locationName(siteName)
          .calendarName(getCalendar() + ": " + siteName);
      String truckId = inferTruckId(parent.select("a.eventlist-title-link").text());
      if (Strings.isNullOrEmpty(truckId)) {
        String text = parent.select("div.eventlist-description")
            .text();
        truckId = inferTruckId(text);
        if (Strings.isNullOrEmpty(truckId)) {
          continue;
        }
      }
      builder.truckId(truckId);
      for (Element tfhr : parent.select("span.event-time-24hr")) {
        parseTime("time.event-time-24hr-start", tfhr).ifPresent(builder::startTime);
        parseTime("time.event-time-12hr-end", tfhr).ifPresent(builder::endTime);
      }
      if (builder.isComplete()) {
        stops.add(builder.build());
      }

    }
    return stops.build();
  }

  private Optional<ZonedDateTime> parseTime(String element, Element parent) {
    Elements elems = parent.select(element);
    if (elems.size() > 0) {
      Element e = elems.get(0);
      LocalDate date = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(e.attr("datetime")));
      LocalTime time = LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(e.text()));
      return Optional.of(ZonedDateTime.of(date, time, zoneId));
    }
    return Optional.empty();
  }

  @Override
  public String getCalendar() {
    return "squarespace";
  }
}
