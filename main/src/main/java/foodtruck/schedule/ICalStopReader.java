package foodtruck.schedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 10/14/18
 */
public class ICalStopReader {

  private static final Logger log = Logger.getLogger(ICalStopReader.class.getName());

  private final GeoLocator locator;

  @Inject
  public ICalStopReader(GeoLocator locator) {
    this.locator = locator;
  }


  List<TruckStop> read(String document, Truck truck) {
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    ICalEntry entry = null;
    try (BufferedReader reader = new BufferedReader(new StringReader(document))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.equals("BEGIN:VEVENT")) {
          entry = new ICalEntry();
          continue;
        } else if (line.equals("END:VEVENT")) {
          if (entry.isComplete()) {
            stops.add(entry.toTruckStop(truck));
          }
          entry = null;
          continue;
        }
        if (entry == null) {
          continue;
        }
        int colon = line.indexOf(':');
        if (colon == -1) {
          continue;
        }
        String tag = line.substring(0, colon);
        String rest = line.length() > colon ? line.substring(colon+1) : "";
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        if (tag.startsWith("DTSTART") || tag.startsWith("DTEND")) {
          int semi = line.indexOf("=");
          if (semi != -1) {
            zone = DateTimeZone.forID(tag.substring(semi+1));
          }
        }
        if (tag.startsWith("DTSTART")) {
          entry.setStartTime(parseTime(rest, zone));
        } else if (tag.startsWith("DTEND")) {
          entry.setEndTime(parseTime(rest, zone));
        } else if (tag.equals("LOCATION")) {
          rest = rest.replaceAll("\\\\,", ",");
          System.out.println("Location: " + rest);
          entry.setLocation(locator.locate(rest, GeolocationGranularity.NARROW));
        }
      }
    } catch (IOException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    return stops.build();
  }

  private DateTime parseTime(String time, DateTimeZone zone) {
    // lifted from stack overflow
    int offsetInMillis = zone.getOffset(new DateTime().getMillis());
    String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000),
        Math.abs((offsetInMillis / 60000) % 60));
    offset = (offsetInMillis >= 0 ? "+" : "-") + offset;

    DateTime theTime = ISODateTimeFormat.basicDateTimeNoMillis()
        .withZone(zone)
        .parseDateTime(time + offset);
    return theTime;
  }

  private static class ICalEntry {

    private DateTime startTime, endTime;
    private Location location;

    public boolean isComplete() {
      return startTime != null && endTime != null && location != null;
    }

    public DateTime getStartTime() {
      return startTime;
    }

    public void setStartTime(DateTime startTime) {
      this.startTime = startTime;
    }

    public DateTime getEndTime() {
      return endTime;
    }

    public void setEndTime(DateTime endTime) {
      this.endTime = endTime;
    }

    public Location getLocation() {
      return location;
    }

    public void setLocation(Location location) {
      this.location = location;
    }

    public TruckStop toTruckStop(Truck truck) {
      return TruckStop.builder()
          .startTime(startTime)
          .endTime(endTime)
          .location(location)
          .truck(truck)
          .origin(StopOrigin.VENDORCAL)
          .build();
    }
  }


}
