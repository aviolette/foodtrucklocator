package foodtruck.schedule;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 2019-01-02
 */
public class ICalReader {

  private static final Logger log = Logger.getLogger(ICalReader.class.getName());
  private final ZoneId defaultZone;
  private final GeoLocator locator;

  @Inject
  public ICalReader(ZoneId defaultZone, GeoLocator locator) {
    this.defaultZone = defaultZone;
    this.locator = locator;
  }

  public List<ICalEvent> parse(String document, boolean extractSummaryLocation) {
    ImmutableList.Builder<ICalEvent> events = ImmutableList.builder();
    try (BufferedReader reader = new BufferedReader(new StringReader(document))) {
      String line;
      ICalEvent event = null;
      boolean locationEncountered = false;
      while ((line = reader.readLine()) != null) {
        if (line.equals("BEGIN:VEVENT")) {
          event = new ICalEvent();
        } else if (line.equals("END:VEVENT")) {
          if (event != null) {
            try {
              events.add(event);
            } catch (Exception e) {
              log.log(Level.SEVERE, e.getMessage(), e);
            }
          }
          locationEncountered = false;
          event = null;
        }

        int colon = line.indexOf(':');
        if (colon == -1) {
          continue;
        }
        String tag = line.substring(0, colon);
        String rest = line.length() > colon ? line.substring(colon+1) : "";
        ZoneId zone = defaultZone;
        if (tag.startsWith("DTSTART") || tag.startsWith("DTEND")) {
          int semi = line.indexOf("=");
          if (semi != -1) {
            String timezone = tag.substring(semi + 1);
            if (timezone.equals("DATE")) {
              zone = null;
            } else {
              zone = ZoneId.of(timezone);
            }
          }
        }

        if (tag.startsWith("DTSTART")) {
          event.setStart(parseTime(rest, zone));
        } else if (tag.startsWith("DTEND")) {
          event.setEnd(parseTime(rest, zone));
        } else if (tag.startsWith("SUMMARY")) {
          rest = rest.replaceAll("\\\\,", ",");
          rest = rest.replaceAll("&amp\\\\;", "and");
          if (extractSummaryLocation && !locationEncountered) {
            appendLocation(event, rest);
          }
          event.setSummary(rest);
        } else if (tag.equals("LOCATION")) {
          rest = rest.replaceAll("\\\\,", ",");
          rest = rest.replaceAll("&amp\\\\;", "and");
          locationEncountered = true;
          appendLocation(event, rest);
        }
      }
    } catch (Exception e) {
      log.log(Level.INFO, document);
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new RuntimeException(e);
    }
    return events.build();
  }

  private void appendLocation(ICalEvent event, String rest) {

    Optional<Location> location = locator.locateOpt(rest);
    if (!location.isPresent()) {
      log.log(Level.WARNING, "Couldn't resolve location >{0}<", rest);
    }
    location.ifPresent(loc -> {
      if (loc.isResolved()) {
        event.setLocation(loc);
      }
    });
  }

  private ZonedDateTime parseTime(String origTime, ZoneId zone) {
    if (zone == null) {
      // this is an all-day event
      LocalDate ld = LocalDate.from(DateTimeFormatter.BASIC_ISO_DATE.parse(origTime));
      return ZonedDateTime.of(ld, LocalTime.MIDNIGHT, defaultZone);
    }
    String time = origTime;
    int index = time.indexOf('T');
    if (index > 0) {
      String date = time.substring(0, index);
      date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
      String sub = time.substring(index+1);
      time = date + "T" + sub.substring(0, 2) + ":" + sub.substring(2, 4) + ":" + sub.substring(4, 6);
    }
    if (origTime.endsWith("Z")) {
      return ZonedDateTime.ofInstant(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(time + "Z")), ZoneOffset.UTC);
    }
    ZoneOffset offset = zone.getRules().getOffset(Instant.now());
    return ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(time + offset.toString()));
  }


  public static class ICalEvent {
    private ZonedDateTime start;
    private ZonedDateTime end;
    @Nullable
    private String summary;
    @Nullable
    private String description;
    @Nullable
    private Location location;

    public ICalEvent() {}

    public ICalEvent(ZonedDateTime start, ZonedDateTime end, String summary, String description, Location location) {
      this.start = start;
      this.end = end;
      this.summary = summary;
      this.description = description;
      this.location = location;
    }

    public ZonedDateTime getStart() {
      return start;
    }

    public void setStart(ZonedDateTime start) {
      this.start = start;
    }

    public ZonedDateTime getEnd() {
      return end;
    }

    public void setEnd(ZonedDateTime end) {
      this.end = end;
    }

    public String getSummary() {
      return summary;
    }

    public void setSummary(String summary) {
      this.summary = summary;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public Location getLocation() {
      return location;
    }

    public void setLocation(Location location) {
      this.location = location;
    }

    @Override
    public String toString() {
      return "ICalEvent{" + "start=" + start + ", end=" + end + ", summary='" + summary + '\'' + ", description='" +
          description + '\'' + ", location=" + location + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ICalEvent iCalEvent = (ICalEvent) o;
      return Objects.equal(start, iCalEvent.start) && Objects.equal(end, iCalEvent.end) &&
          Objects.equal(summary, iCalEvent.summary) && Objects.equal(description, iCalEvent.description) &&
          Objects.equal(location, iCalEvent.location);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(start, end, summary, description, location);
    }
  }
}
