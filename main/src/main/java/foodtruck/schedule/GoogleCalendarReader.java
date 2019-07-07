package foodtruck.schedule;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.api.services.calendar.model.Event;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;
import foodtruck.time.Clock;

import static foodtruck.schedule.SimpleCalReader.inferTruckId;

/**
 * @author aviolette
 * @since 2018-12-26
 */
public class GoogleCalendarReader {

  private static final Logger log = Logger.getLogger(GoogleCalendarReader.class.getName());
  private final TruckDAO truckDAO;
  private final List<String> exemptions;
  private final Clock clock;
  private final CalendarAddressExtractor addressExtractor;

  @Inject
  public GoogleCalendarReader(TruckDAO truckDAO, Clock clock,
      @Named("exemptLocations") List<String> exemptLocationNames, CalendarAddressExtractor addressExtractor) {
    this.truckDAO = truckDAO;
    this.exemptions = exemptLocationNames;
    this.clock = clock;
    this.addressExtractor = addressExtractor;
  }

  @Nullable
  public TempTruckStop buildTruckStop(@Nullable Truck truck, int timezoneAdjustment, Event event, String defaultLocation) {
    String titleText = event.getSummary();
    if (hasInvalidTitle(titleText)) {
      String truckID = truck == null ? "" : truck.getId();
      log.log(Level.INFO, "Skipping {0} for {1}", new Object[]{titleText, truckID});
      return null;
    }

    if (truck == null) {
      String truckId = inferTruckId(titleText);
      if (truckId == null) {
        return null;
      }
      truck = truckDAO.findByIdOpt(truckId).orElseThrow(() -> new RuntimeException("Truck not found: " + truckId));
    }

    if (defaultLocation != null) {
      return buildTruckStopFromLocation(defaultLocation, truck, event, timezoneAdjustment);
    }
    Location location = locationFromWhereField(event, truck);
    String where = (location == null) ? null : location.getName();
    if (location == null || !location.isResolved()) {
      // Sometimes the location is in the title - try that too
      if (!Strings.isNullOrEmpty(titleText)) {
        where = titleText;
        location = addressExtractor.parse(titleText, truck).orElse(null);
      }
    }
    if (location != null && location.isResolved() && !event.isEndTimeUnspecified()) {
      return buildTruckStopFromLocation(location.getName(), truck, event, timezoneAdjustment);
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
  private Location locationFromWhereField(Event event, Truck truck) {
    String where = event.getLocation();
    if (!Strings.isNullOrEmpty(where)) {
      if (where.endsWith(", United States")) {
        where = where.substring(0, where.lastIndexOf(","));
        // Fixes how google calendar normalizes fully-qualified addresses with a state, zip and country code
      } else if (where.lastIndexOf(", IL ") != -1) {
        where = where.substring(0, where.lastIndexOf(", IL ")) + ", IL";
      }
      Optional<Location> parsedLocation = addressExtractor.parse(where, truck);
      if (parsedLocation.isPresent()) {
        return parsedLocation.get();
      }
    }
    return null;
  }

  @Nullable
  private TempTruckStop buildTruckStopFromLocation(String location, Truck truck, Event event,
      int timezoneAdjustment) {
    ZonedDateTime startTime, endTime;
    if (event.getStart()
        .getDateTime() == null) {
      if (truck.getCategories()
          .contains("AssumeNoTimeEqualsLunch")) {
        String[] dcs = event.getStart()
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
        .locationName(location)
        .startTime(startTime)
        .endTime(endTime)
        .calendarName("Google: " + MoreObjects.firstNonNull(truck.getCalendarUrl(), location))
        .build();
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
