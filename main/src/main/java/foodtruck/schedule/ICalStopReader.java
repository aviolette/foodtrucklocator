package foodtruck.schedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.TempTruckStop;

/**
 * @author aviolette
 * @since 10/14/18
 */
public class ICalStopReader {

  private static final Logger log = Logger.getLogger(ICalStopReader.class.getName());

  private final GeoLocator locator;
  private final AddressExtractor extractor;

  @Inject
  public ICalStopReader(GeoLocator locator, AddressExtractor addressExtractor) {
    this.locator = locator;
    this.extractor = addressExtractor;
  }

  public List<TempTruckStop> findStops(String document, String truck) {
    ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
    TempTruckStop.Builder builder = null;
    try (BufferedReader reader = new BufferedReader(new StringReader(document))) {
      String line;
      boolean locationEncountered = false;
      while ((line = reader.readLine()) != null) {
        if (line.equals("BEGIN:VEVENT")) {
          builder = TempTruckStop.builder().truckId(truck).calendarName("ical");
          continue;
        } else if (line.equals("END:VEVENT")) {
          if (builder.isComplete()) {
            stops.add(builder.build());
          }
          builder = null;
          locationEncountered = false;
          continue;
        }
        if (builder == null) {
          continue;
        }
        int colon = line.indexOf(':');
        if (colon == -1) {
          continue;
        }
        String tag = line.substring(0, colon);
        String rest = line.length() > colon ? line.substring(colon+1) : "";
        ZoneId zone = ZoneId.of("America/Chicago");
        if (tag.startsWith("DTSTART") || tag.startsWith("DTEND")) {
          int semi = line.indexOf("=");
          if (semi != -1) {
            zone = ZoneId.of(tag.substring(semi+1));
          }
        }
        if (tag.startsWith("DTSTART")) {
          builder.startTime(parseTime(rest, zone));
        } else if (tag.startsWith("DTEND")) {
          builder.endTime(parseTime(rest, zone));
        } else if (tag.startsWith("SUMMARY")) {
          rest = rest.replaceAll("\\\\,", ",");
          rest = rest.replaceAll("&amp\\\\;", "and");
          if (!locationEncountered) {
            appendLocation(builder, rest);
          }
        } else if (tag.equals("LOCATION")) {
          rest = rest.replaceAll("\\\\,", ",");
          rest = rest.replaceAll("&amp\\\\;", "and");
          locationEncountered = true;
          appendLocation(builder, rest);
        }
      }
    } catch (IOException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    return stops.build();
  }

  private ZonedDateTime parseTime(String time, ZoneId zone) {
    int index = time.indexOf('T');
    if (index > 0) {
      String date = time.substring(0, index);
      date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
      String sub = time.substring(index+1);
      time = date + "T" + sub.substring(0, 2) + ":" + sub.substring(2, 4) + ":" + sub.substring(4, 6);
      if (sub.length() > 6) {
        time = sub.substring(6);
      }
    }
    if (time.endsWith("Z")) {
      return ZonedDateTime.ofInstant(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(time)), zone);
    }
    // lifted from stack overflow
    ZoneOffset offset = zone.getRules().getOffset(Instant.now());
    return ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(time + offset.toString()));
  }


  private void appendLocation(TempTruckStop.Builder builder, String rest) {
    locator.locateOpt(rest).ifPresent(loc -> builder.locationName(loc.getName()));
  }
}
