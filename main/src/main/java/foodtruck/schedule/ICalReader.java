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
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
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
  private static final Splitter SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

  @Inject
  public ICalReader(ZoneId defaultZone, GeoLocator locator) {
    this.defaultZone = defaultZone;
    this.locator = locator;
  }

  public List<ICalEvent> parse(String document, boolean extractSummaryLocation) {
    ImmutableList.Builder<ICalEvent> events = ImmutableList.builder();
    try (BufferedReader reader = new BufferedReader(new StringReader(document))) {
      String line;

      boolean locationEncountered = false;
      ICalEvent.Builder builder = null;
      while ((line = reader.readLine()) != null) {
        if (line.equals("BEGIN:VEVENT")) {
          builder = new ICalEvent.Builder();
        } else if (line.equals("END:VEVENT")) {
          if (builder != null) {
            try {
              events.add(builder.build());
            } catch (Exception e) {
              log.log(Level.SEVERE, e.getMessage(), e);
            }
          }
          locationEncountered = false;
          builder = null;
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
              timezone = timezone.replaceAll("\"", "");
              zone = ZoneId.of(timezone);
            }
          }
        }

        if (builder == null) {
          continue;
        }

        if (tag.startsWith("DTSTART")) {
          builder.start(parseTime(rest, zone));
        } else if (tag.startsWith("DTEND")) {
          builder.end(parseTime(rest, zone));
        } else if (tag.startsWith("CATEGORIES")) {
          builder.categories(SPLITTER.splitToList(rest));
        } else if (tag.startsWith("SUMMARY")) {
          rest = rest.replaceAll("\\\\,", ",");
          rest = rest.replaceAll("&amp\\\\;", "and");
          if (extractSummaryLocation && !locationEncountered) {
            appendLocation(builder, rest);
          }
          builder.summary(rest);
        } else if (tag.equals("LOCATION")) {
          rest = rest.replaceAll("\\\\,", ",");
          rest = rest.replaceAll("&amp\\\\;", "and");
          locationEncountered = true;
          appendLocation(builder, rest);
        }
      }
    } catch (Exception e) {
      log.log(Level.INFO, document);
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new RuntimeException(e);
    }
    return events.build();
  }

  private void appendLocation(ICalEvent.Builder builder, String rest) {

    Optional<Location> location = locator.locateOpt(rest);
    if (!location.isPresent()) {
      log.log(Level.WARNING, "Couldn't find location: " + rest);
    }
    location.ifPresent(loc -> {
      if (loc.isResolved()) {
        builder.location(loc);
      } else {
        log.log(Level.WARNING, "Couldn't resolve location: " + rest);
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

    private List<String> categories;
    @Nullable
    private String image;

    private ICalEvent(Builder builder) {
      this.start = builder.start;
      this.end = builder.end;
      this.summary = builder.summary;
      this.description = builder.description;
      this.location = builder.location;
      this.categories = builder.categories;
      this.image = builder.image;
    }

    public static Builder builder() {
      return new Builder();
    }

    public ZonedDateTime getStart() {
      return start;
    }

    public ZonedDateTime getEnd() {
      return end;
    }

    public String getSummary() {
      return summary;
    }

    public String getDescription() {
      return description;
    }

    public Location getLocation() {
      return location;
    }

    @Override
    public String toString() {
      return "ICalEvent{" + "start=" + start + ", end=" + end + ", summary='" + summary + '\'' + ", description='" +
          description + '\'' + ", location=" + location + ", categories=" + categories + ", image='" + image + '\'' +
          '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ICalEvent iCalEvent = (ICalEvent) o;
      return start.equals(iCalEvent.start) && end.equals(iCalEvent.end) && Objects.equals(summary, iCalEvent.summary) &&
          Objects.equals(description, iCalEvent.description) && Objects.equals(location, iCalEvent.location) &&
          categories.equals(iCalEvent.categories) && Objects.equals(image, iCalEvent.image);
    }

    @Override
    public int hashCode() {
      return Objects.hash(start, end, summary, description, location, categories, image);
    }

    public List<String> getCategories() {
      return categories;
    }

    public static class Builder {
      private ZonedDateTime start;
      private ZonedDateTime end;
      @Nullable
      private String summary;
      @Nullable
      private String description;
      @Nullable
      private Location location;

      private List<String> categories = ImmutableList.of();
      @Nullable
      private String image;

      public Builder() {}

      public Builder start(ZonedDateTime start) {
        this.start = start;
        return this;
      }

      public Builder end(ZonedDateTime end) {
        this.end = end;
        return this;
      }

      public Builder summary(String summary) {
        this.summary = summary;
        return this;
      }

      public Builder description(String description) {
        this.description = description;
        return this;
      }

      public Builder location(Location location) {
        this.location = location;
        return this;
      }

      public Builder categories(List<String> categories) {
        this.categories = categories;
        return this;
      }

      public Builder image(String image) {
        this.image = image;
        return this;
      }

      public ICalEvent build() {
        return new ICalEvent(this);
      }

    }
  }
}
