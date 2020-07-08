package foodtruck.schedule;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;
import foodtruck.time.Clock;
import foodtruck.util.MoreStrings;

/**
 * @author aviolette
 * @since 2018-12-24
 */
@SuppressWarnings("ALL")
public class FatShallotScheduleReader implements StopReader {

  private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d");
  private final static Pattern RANGE = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)");
  private final static String TRUCKID = "thefatshallot";
  private final static String CALENDAR = TRUCKID;
  private final Clock clock;
  private final AddressExtractor extractor;
  private final GeoLocator geoLocator;
  private final ZoneId zone;

  @Inject
  public FatShallotScheduleReader(Clock clock, AddressExtractor extractor, GeoLocator geoLocator, ZoneId zone) {
    this.clock = clock;
    this.extractor = extractor;
    this.geoLocator = geoLocator;
    this.zone = zone;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    int year = clock.now8()
        .getYear();
    int currentMonth = clock.now8().getMonthValue();
    Document parsedDoc = Jsoup.parse(document);
    ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
    for (Element h3 : parsedDoc.select("h3")) {
      LocalDate date = null;
      String item = "";
      for (Element strong : h3.select("strong")) {
        item += MoreStrings.capitalize(strong.text()
            .toLowerCase())
            .trim()
            .replaceAll("[^\\x00-\\x7F]", "") + " ";
      }
      if (!Strings.isNullOrEmpty(item)) {
        item = item.trim();
        if (item.endsWith("st") || item.endsWith("rd") || item.endsWith("nd") || item.endsWith("th")) {
          item = item.substring(0, item.length() - 2);
        }
        if (item.contains(",") && !item.contains(", ")) {
          item = item.replace(",", ", ");
        }
        try {
          TemporalAccessor parsedTime = FORMATTER.parse(item);
          int month = parsedTime.get(ChronoField.MONTH_OF_YEAR);
          if (currentMonth == 1 && month == 12) {
            date = null;
          } else {
            date = LocalDate.of(year, month, parsedTime.get(ChronoField.DAY_OF_MONTH));
          }
        } catch (DateTimeParseException ignored) {
          continue;
        }
      }
      if (date != null) {
        parseStopsOnDate(h3, date, stops);
      }
    }
    return stops.build();
  }

  private void parseStopsOnDate(Element h3, LocalDate date, ImmutableList.Builder<TempTruckStop> stops) {
    Element e = h3.nextElementSibling();
    while (e != null && e.tagName()
        .equalsIgnoreCase("p")) {
      TempTruckStop.Builder builder = TempTruckStop.builder()
          .calendarName(CALENDAR)
          .truckId("thefatshallot");
      String tweet = e.text()
          .replaceAll("&amp;", "&");
      e = e.nextElementSibling();
      if (tweet.contains("Revival") ||
          tweet.toLowerCase().contains("lincoln park restaurant") ||
          tweet.toLowerCase().contains("fooda") ||
          tweet.toLowerCase().contains("lincoln park shop")) {
        continue;
      }
      List<String> parse = extractor.parse(tweet, Truck.builder()
          .id("thefatshallot")
          .name("The Fat Shallot")
          .build());
      for (String location : parse) {
        geoLocator.locateOpt(location)
            .ifPresent(loc -> {
              builder.locationName(loc.getName());

              parseRange(tweet, builder, date);

              stops.add(builder.build());
            });
      }
    }
  }


  // this is slightly naive and does not parse all the cases (half-hours, am/pm, etc.)
  private void parseRange(String tweet, TempTruckStop.Builder builder, LocalDate date) {
    Matcher m = RANGE.matcher(tweet);
    if (m.find()) {
      int start = Integer.parseInt(m.group(1));
      int end = Integer.parseInt(m.group(2));
      if (start < 11) {
        start = start + 12;
      }
      if (start > end && end < 12) {
        end = end + 12;
      }
      if (start < end) {
        builder.startTime(date.atTime(start, 0).atZone(zone));
        builder.endTime(date.atTime(end, 0).atZone(zone));
        return;
      }
    }
    // fall through case for now
    builder.startTime(date.atTime(11, 0).atZone(zone));
    builder.endTime(date.atTime(14, 0).atZone(zone));
  }

  @Override
  public String getCalendar() {
    return CALENDAR;
  }
}
