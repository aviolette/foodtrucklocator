package foodtruck.schedule;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.api.services.calendar.model.Event;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2018-12-26
 */
public class GoogleCalendarReader {

  private static final Logger log = Logger.getLogger(GoogleCalendarReader.class.getName());
  private final TruckDAO truckDAO;
  private final AddressExtractor addressExtractor;
  private final List<String> exemptions;
  private final GeoLocator geoLocator;
  private final Clock clock;

  @Inject
  public GoogleCalendarReader(TruckDAO truckDAO, AddressExtractor addressExtractor, GeoLocator geoLocator, Clock clock,
      @Named("exemptLocations") List<String> exemptLocationNames) {
    this.truckDAO = truckDAO;
    this.addressExtractor = addressExtractor;
    this.geoLocator = geoLocator;
    this.exemptions = exemptLocationNames;
    this.clock = clock;
  }

  @Nullable
  public TempTruckStop buildTruckStop(Truck truck, int timezoneAdjustment, Event event) {
    String titleText = event.getSummary();
    if (hasInvalidTitle(titleText)) {
      log.log(Level.INFO, "Skipping {0} for {1}", new Object[]{titleText, truck.getId()});
      return null;
    }
    // HACK: toasty cheese adds codes to the end of their locations noting which trucks are going where

    if ("mytoastycheese".equals(truck.getId())) {
      int dash = titleText.lastIndexOf("-");
      if (dash == -1) {
        dash = titleText.lastIndexOf(":");
      }
      if (dash > 0) {
        String truckPortion = titleText.substring(dash + 1);
        String alternativeTruck = null;
        if (truckPortion.contains("BBQ")) {
          alternativeTruck = "besttruckinbbq";
        } else if (truckPortion.contains("Crave")) {
          alternativeTruck = "thecravebar";
        } else if (truckPortion.contains("Taco")) {
          alternativeTruck = "mytoastytaco";
        } else if (truckPortion.contains("Toasty Cheese")) {
          titleText = titleText.substring(0, dash)
              .trim();
        }
        if (alternativeTruck != null) {
          truck = truckDAO.findByIdOpt(alternativeTruck)
              .orElse(truck);
          titleText = titleText.substring(0, dash)
              .trim();
        }
      }
    }
    Location location = locationFromWhereField(event, truck);
    String where = (location == null) ? null : location.getName();
    if (location == null || !location.isResolved()) {
      // Sometimes the location is in the title - try that too
      if (!Strings.isNullOrEmpty(titleText)) {
        where = titleText;
        location = locationFromTitleText(titleText, truck);
      }
    }
    if (location != null && location.isResolved() && !event.isEndTimeUnspecified()) {
      return buildTruckStopFromLocation(location, truck, event, timezoneAdjustment);
    } else if (location != null && location.isBlacklistedFromCalendarSearch()) {
      log.log(Level.INFO, "Skipping {0} because it is blacklisted.", where);
    } else {
      // TODO: this shouldn't be hard-coded
      if (where != null && !exemptions.contains(where)) {
        log.log(Level.SEVERE, "Location could not be resolved for {0}, {1}. Link: {2}",
            new Object[]{truck.getId(), where, event.getHtmlLink()});
      }
    }
    return null;
  }

  @Nullable
  private Location locationFromTitleText(String titleText, Truck truck) {
    log.info("Trying title text: " + titleText);
    final List<String> parsed = addressExtractor.parse(titleText, truck);
    String locString = Iterables.getFirst(parsed, null);
    if (locString == null) {
      log.info("Failed to parse titletext for address, trying whole thing: " + titleText);
      locString = titleText;
    }
    if (locString != null) {
      return geoLocator.locate(locString, GeolocationGranularity.NARROW);
    }
    return null;
  }

  @Nullable
  private Location locationFromWhereField(Event event, Truck truck) {
    String where = event.getLocation();
    if (!Strings.isNullOrEmpty(where)) {
      if (where.endsWith(", United States")) {
        where = where.substring(0, where.lastIndexOf(","));
        // Fixes how google calendar normalizes fully-qualified addresses with a state, zip and country code
      } else if (where.lastIndexOf(", IL ") != -1) {
        where = where.substring(0, where.lastIndexOf(", IL ")) + ", IL";
      }
      // HACK Alert, the address extractor doesn't handle non-Chicago addresses well, so
      // if it is a fully qualified address written by me, it will probably end in City, IL
      if (!where.endsWith(", IL")) {
        where = coalesce(Iterables.getFirst(addressExtractor.parse(where, truck), null), where);
      }
      return geoLocator.locate(where, GeolocationGranularity.NARROW);
    }
    return null;
  }

  @Nullable
  private TempTruckStop buildTruckStopFromLocation(Location location, Truck truck, Event event,
      int timezoneAdjustment) {
    ZonedDateTime startTime, endTime;
    if (event.getStart()
        .getDateTime() == null) {
      if (truck.getCategories()
          .contains("AssumeNoTimeEqualsLunch")) {
        String dcs[] = event.getStart()
            .getDate()
            .toStringRfc3339()
            .split("-");
        startTime = ZonedDateTime.of(Integer.parseInt(dcs[0]), Integer.parseInt(dcs[1]), Integer.parseInt(dcs[2]), 11, 0, 0, 0,
            clock.zone8());
        endTime = startTime.plusHours(2);
      } else {
        log.log(Level.WARNING, "Skipping {0} {1} because no time is specified", new Object[]{truck.getId(), location});
        return null;
      }
    } else {
      startTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getStart()
          .getDateTime()
          .getValue()), clock.zone8()).plusHours(timezoneAdjustment);
      endTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getEnd()
          .getDateTime()
          .getValue()), clock.zone8()).plusHours(timezoneAdjustment);
    }
    return TempTruckStop.builder()
        .truckId(truck.getId())
        .locationName(location.getName())
        .startTime(startTime)
        .endTime(endTime)
        .calendarName("Google: " + truck.getCalendarUrl())
        .build();
  }

  // TODO: make this generic and pull it out
  private String coalesce(String st1, String st2) {
    return (Strings.isNullOrEmpty(st1)) ? st2 : st1;
  }

  private boolean hasInvalidTitle(String titleText) {
    if (!Strings.isNullOrEmpty(titleText)) {
      String lowerTitle = titleText.toLowerCase();
      return lowerTitle.contains("private") || lowerTitle.contains("wedding") || lowerTitle.contains("catering") ||
          lowerTitle.contains("downtown chicago") || titleText.contains("TBD") || titleText.contains("TBA");
    }
    return false;
  }
}
