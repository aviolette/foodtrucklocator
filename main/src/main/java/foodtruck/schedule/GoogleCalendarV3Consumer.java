package foodtruck.schedule;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 11/20/14
 */
class GoogleCalendarV3Consumer implements ScheduleStrategy {
  private static final Logger log = Logger.getLogger(GoogleCalendarV3Consumer.class.getName());
  private final Calendar calendarClient;
  private final TruckDAO truckDAO;
  private final AddressExtractor addressExtractor;
  private final GeoLocator geoLocator;
  private final Clock clock;
  private final List<String> exemptLocations;

  @Inject
  public GoogleCalendarV3Consumer(AddressExtractor addressExtractor, Calendar calendarClient, TruckDAO truckDAO,
      GeoLocator geoLocator, Clock clock, @Named("exemptLocations") List<String> exemptLocationNames) {
    this.calendarClient = calendarClient;
    this.truckDAO = truckDAO;
    this.addressExtractor = addressExtractor;
    this.geoLocator = geoLocator;
    this.clock = clock;
    this.exemptLocations = exemptLocationNames;
  }

  @Override
  public List<TruckStop> findForTime(Interval range, @Nullable Truck searchTruck) {
    String truckId = searchTruck == null ? null : searchTruck.getId();
    log.info("Initiating calendar search " + truckId);
    List<TruckStop> stops = Lists.newLinkedList();
    if (searchTruck != null && !Strings.isNullOrEmpty(searchTruck.getCalendarUrl())) {
      customCalendarSearch(range, searchTruck, stops);
    } else if (searchTruck == null) {
      for (Truck truck : truckDAO.findTrucksWithCalendars()) {
        customCalendarSearch(range, truck, stops);
      }
    }
    return stops;
  }

  private void customCalendarSearch(Interval range, Truck truck, List<TruckStop> stops) {
    try {
      final String calendarUrl = truck.getCalendarUrl();
      if (Strings.isNullOrEmpty(calendarUrl)) {
        return;
      }
      log.log(Level.INFO, "Custom calendar search for truck {0} with calendar {1}: ",
          new Object[]{truck.getName(), calendarUrl});
      stops.addAll(performTruckSearch(range, truck));
    } catch (RuntimeException rte) {
      log.info("Search truck: " + truck.getId());
      log.log(Level.SEVERE, rte.getMessage(), rte);
    }
  }

  private List<TruckStop> performTruckSearch(Interval range, Truck truck) {
    ImmutableList.Builder<TruckStop> builder = ImmutableList.builder();
    try {
      final String calendarId = truck.getCalendarUrl();
      if (calendarId == null) {
        return ImmutableList.of();
      }
      String pageToken = null;
      int timezoneAdjustment = truck.getTimezoneAdjustment();
      do {
        Calendar.Events.List query = calendarClient.events()
            .list(calendarId)
            .setSingleEvents(true)
            .setTimeMin(toGoogleDateTime(range.getStart()))
            .setTimeMax(toGoogleDateTime(range.getEnd()))
            .setPageToken(pageToken);
        Events events = query.execute();
        List<Event> items = events.getItems();
        for (Event event : items) {
          buildTruckStop(range, truck, builder, timezoneAdjustment, event);
        }
        pageToken = events.getNextPageToken();
      } while (pageToken != null);
    } catch (IOException e) {
      log.log(Level.SEVERE, "An error occurred while caching the schedule", e);
    }
    return builder.build();
  }

  private void buildTruckStop(Interval range, Truck truck, ImmutableList.Builder<TruckStop> builder,
      int timezoneAdjustment, Event event) {
    String titleText = event.getSummary();
    if (hasInvalidTitle(titleText)) {
      log.log(Level.INFO, "Skipping {0} for {1}", new Object[]{titleText, truck.getId()});
      return;
    }
    // HACK: toasty cheese adds codes to the end of their locations noting which trucks are going where
    if ("mytoastycheese".equals(truck.getId())) {
      if (titleText.endsWith("- B1")) {
        truck = MoreObjects.firstNonNull(truckDAO.findById("besttruckinbbq"), truck);
        titleText = titleText.substring(0, titleText.length() - 4)
            .trim();
        log.log(Level.INFO, "Creating event on besttruckinbbq's schedule from toasty cheese' calendar: {0}", titleText);
      } else if (titleText.endsWith("- T1") || titleText.endsWith("- T2")) {
        titleText = titleText.substring(0, titleText.length() - 4);
      } else if (titleText.endsWith(" T1") || titleText.endsWith(" T2")) {
        titleText = titleText.substring(0, titleText.length() - 3);
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
      TruckStop truckStop = buildTruckStopFromLocation(location, truck, event, timezoneAdjustment);
      if (truckStop == null) {
        return;
      }
      builder.add(truckStop);
    } else {
      // TODO: this shouldn't be hard-coded
      if (where != null && !exemptLocations.contains(where)) {
        log.log(Level.SEVERE, "Location could not be resolved for {0}, {1} between {2} and {3}. Link: {4}",
            new Object[]{truck.getId(), where, range.getStart(), range.getEnd(), event.getHtmlLink()});
      }
    }
  }

  private
  @Nullable
  TruckStop buildTruckStopFromLocation(Location location, Truck truck, Event event, int timezoneAdjustment) {
    DateTime startTime, endTime;
    if (event.getStart().getDateTime() == null) {
      if (truck.getCategories().contains("AssumeNoTimeEqualsLunch")) {
        String dcs[] = event.getStart().getDate().toStringRfc3339().split("-");
        startTime = new DateTime(Integer.parseInt(dcs[0]), Integer.parseInt(dcs[1]), Integer.parseInt(dcs[2]), 11, 0,
            clock.zone());
        endTime = startTime.plusHours(2);
      } else {
        log.log(Level.WARNING, "Skipping {0} {1} because no time is specified", new Object[]{truck.getId(), location});
        return null;
      }
    } else {
      startTime = new DateTime(event.getStart().getDateTime().getValue(), clock.zone()).plusHours(timezoneAdjustment);
      endTime = new DateTime(event.getEnd().getDateTime().getValue(), clock.zone()).plusHours(timezoneAdjustment);
    }
    String note = "Stop added from vendor's calendar";
    final TruckStop truckStop = TruckStop.builder().truck(truck)
        .origin(StopOrigin.VENDORCAL)
        .location(location)
        .appendNote(note)
        .startTime(startTime)
        .endTime(endTime)
        .build();
    log.log(Level.INFO, "Loaded truckstop: {0}", truckStop);
    return truckStop;
  }

  private
  @Nullable
  Location locationFromTitleText(String titleText, Truck truck) {
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

  private
  @Nullable
  Location locationFromWhereField(Event event, Truck truck) {
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

  private boolean hasInvalidTitle(String titleText) {
    if (!Strings.isNullOrEmpty(titleText)) {
      String lowerTitle = titleText.toLowerCase();
      if (lowerTitle.contains("private") || lowerTitle.contains("wedding") || lowerTitle.contains("catering") || lowerTitle.contains(
          "downtown chicago") || titleText.contains("TBD") || titleText.contains("TBA")) {
        return true;
      }
    }
    return false;
  }

  // TODO: make this generic and pull it out
  private String coalesce(String st1, String st2) {
    return (Strings.isNullOrEmpty(st1)) ? st2 : st1;
  }

  private com.google.api.client.util.DateTime toGoogleDateTime(DateTime start) {
    return new com.google.api.client.util.DateTime(start.getMillis());
  }
}

