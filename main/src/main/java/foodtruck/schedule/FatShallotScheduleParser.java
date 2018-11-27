package foodtruck.schedule;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.time.Clock;
import foodtruck.util.MoreStrings;

/**
 * @author aviolette
 * @since 11/10/18
 */
public class FatShallotScheduleParser {
  private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d");
  private final Clock clock;
  private final AddressExtractor extractor;
  private final GeoLocator geolocator;
  private final ZoneId zone;
  private final static Pattern RANGE = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)");

  @Inject
  public FatShallotScheduleParser(Clock clock, AddressExtractor extractor, GeoLocator geoLocator, ZoneId zone) {
    this.clock = clock;
    this.extractor = extractor;
    this.geolocator = geoLocator;
    this.zone = zone;

  }

  public List<TruckStop> parse(String document, Truck truck) {
    int year = clock.now8().getYear();
    Document parsedDoc = Jsoup.parse(document);
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (Element h3 : parsedDoc.select("h3")) {
      LocalDate date = null;
      for (Element strong : h3.select("strong")) {
        String item = MoreStrings.capitalize(strong.text()
            .toLowerCase())
            .trim()
            .replaceAll("[^\\x00-\\x7F]", "");

        if (item.endsWith("st") || item.endsWith("rd") || item.endsWith("nd")) {
          item = item.substring(0, item.length() - 2);
        }

        TemporalAccessor parsedTime = formatter.parse(item);
        date = LocalDate.of(year, parsedTime.get(ChronoField.MONTH_OF_YEAR),
            parsedTime.get(ChronoField.DAY_OF_MONTH));
      }
      if (date != null) {
        parseStopsOnDate(truck, h3, date, stops);
      }
    }
    return stops.build();
  }

  private void parseStopsOnDate(Truck truck, Element h3, LocalDate date, ImmutableList.Builder<TruckStop> stops) {
    Element e = h3.nextElementSibling();
    while (e != null && e.tagName().equalsIgnoreCase("p")) {
      TruckStop.Builder builder = TruckStop.builder().appendNote("Created from fat shallot's website")
          .origin(StopOrigin.VENDORCAL)
          .appendNote("Created from fat shallot's website")
          .truck(truck);
      String tweet = e.text()
          .replaceAll("&amp;", "&");
      e = e.nextElementSibling();
      if (tweet.contains("Revival") || tweet.toLowerCase().contains("lincoln park restaurant") || tweet.toLowerCase().contains("fooda")) {
        continue;
      }
      List<String> parse = extractor.parse(tweet, truck);
      for (String location : parse) {
        geolocator.locateOpt(location).ifPresent(loc -> {
          builder.location(loc);

          parseRange(tweet, builder, date);

          stops.add(builder.build());
        });
      }
    }
  }

  // this is slightly naive and does not parse all the cases (half-hours, am/pm, etc.)
  private void parseRange(String tweet, TruckStop.Builder builder, LocalDate date) {
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
        builder.startTime8(date.atTime(start, 0).atZone(zone));
        builder.endTime8(date.atTime(end, 0).atZone(zone));
        return;
      }
    }
    // fall through case for now
    builder.startTime8(date.atTime(11, 0).atZone(zone));
    builder.endTime8(date.atTime(14, 0).atZone(zone));
  }
}
