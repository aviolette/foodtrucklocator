package foodtruck.schedule;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.util.Clock;
import foodtruck.util.FriendlyDateOnlyFormat;

/**
 * @author aviolette
 * @since 11/20/14
 */
public class GoogleCalendarV3Consumer implements ScheduleStrategy {
  private final Calendar calendarClient;
  private static final Logger log = Logger.getLogger(GoogleCalendarV3Consumer.class.getName());
  private final ConfigurationDAO configDAO;
  private final TruckDAO truckDAO;
  private final AddressExtractor addressExtractor;
  private final GeoLocator geoLocator;
  private final Clock clock;
  private final DateTimeFormatter formatter;

  @Inject
  public GoogleCalendarV3Consumer(AddressExtractor addressExtractor, Calendar calendarClient,
      ConfigurationDAO configDAO, TruckDAO truckDAO, GeoLocator geoLocator, Clock clock,
      @FriendlyDateOnlyFormat DateTimeFormatter formatter) {
    this.calendarClient = calendarClient;
    this.configDAO = configDAO;
    this.truckDAO = truckDAO;
    this.addressExtractor = addressExtractor;
    this.geoLocator = geoLocator;
    this.clock = clock;
    this.formatter = formatter;
  }

  @Override
  public List<TruckStop> findForTime(Interval range, @Nullable Truck searchTruck) {
    String truckId = searchTruck == null ? null : searchTruck.getId();
    log.info("Initiating calendar search " + truckId);
    List<TruckStop> stops = performTruckSearch(range, searchTruck, false);
    stops = Lists.newLinkedList(stops);
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
      log.info("Custom calendar search: " + calendarUrl);
      stops.addAll(performTruckSearch(range, truck, true));
    } catch (RuntimeException rte) {
      log.info("Search truck: " + truck.getId());
      log.log(Level.SEVERE, rte.getMessage(), rte);
    }
  }

  private List<TruckStop> performTruckSearch(Interval range, @Nullable Truck searchTruck, boolean customCalendar) {
    ImmutableList.Builder<TruckStop> builder = ImmutableList.builder();
    try {
      final String calendarId = customCalendar ? searchTruck.getCalendarUrl() : configDAO.find().getGoogleCalendarAddress();
      String pageToken = null;
      do {
        Calendar.Events.List query = calendarClient.events().list(calendarId).setSingleEvents(true).setTimeMin(
            toGoogleDateTime(range.getStart())).setTimeMax(toGoogleDateTime(range.getEnd())).setPageToken(pageToken);
        if (searchTruck != null && !customCalendar) {
          query.setQ(searchTruck.getId());
        }
        Events events = query.execute();
        List<Event> items = events.getItems();
        for (Event event : items) {
          final String titleText = event.getSummary();
          Truck truck = (searchTruck != null || Strings.isNullOrEmpty(titleText)) ? searchTruck : truckDAO.findById(titleText);
          if (truck == null) {
            log.log(Level.WARNING, "Could not find title text for {0}", new Object[] { event.getHtmlLink() });
            continue;
          }
          String where = event.getLocation();
          Location location = null;
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
              where =
                  coalesce(Iterables.getFirst(addressExtractor.parse(where, truck), null),
                      where);
            }
            location = geoLocator.locate(where, GeolocationGranularity.NARROW);
          }
          if ((location == null || !location.isResolved()) && customCalendar) {
            // Sometimes the location is in the title - try that too
            if (!Strings.isNullOrEmpty(titleText)) {
              where = titleText;
              log.info("Trying title text: " + titleText);
              final List<String> parsed = addressExtractor.parse(titleText, searchTruck);
              String locString = Iterables.getFirst(parsed, null);
              if (locString == null) {
                log.info("Failed to parse titletext for address, trying whole thing: " + titleText);
                locString = titleText;
              }
              if (locString != null) {
                location = geoLocator.locate(locString, GeolocationGranularity.NARROW);
              }
            }
          }
          if (location != null && location.isResolved()) {
            final String entered = enteredOn(event.getUpdated());
            String note = customCalendar ? "Stop added from vendor's calendar" :
                "Entered manually " + (entered == null ? "" : "on ") + entered;
            Confidence confidence = customCalendar ? Confidence.HIGH : Confidence.MEDIUM;
            final TruckStop truckStop = TruckStop.builder().truck(truck)
                .origin(customCalendar ? StopOrigin.VENDORCAL : StopOrigin.MANUAL)
                .location(location)
                .confidence(confidence)
                .appendNote(note)
                .startTime(new DateTime(event.getStart().getDateTime().getValue(), clock.zone()))
                .endTime(new DateTime(event.getEnd().getDateTime().getValue(), clock.zone()))
                .build();
            log.log(Level.INFO, "Loaded truckstop: {0}", truckStop);
            builder.add(truckStop);
          } else {
            log.log(Level.WARNING, "Location could not be resolved for {0}, {1} between {2} and {3}. Link: {4}",
                new Object[] {truck.getId(), where, range.getStart(), range.getEnd(), event.getHtmlLink()});
          }
        }
        pageToken = events.getNextPageToken();
      } while (pageToken != null);
    } catch (IOException e) {
      log.log(Level.SEVERE, "An error occurred while caching the schedule", e);
    }
    return builder.build();
  }

  private @Nullable String enteredOn(com.google.api.client.util.DateTime entry) {
    try {
      return formatter.print(new DateTime(entry.getValue(), clock.zone()));
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
      return clock.nowFormattedAsTime();
    }
  }

  // TODO: make this generic and pull it out
  private String coalesce(String st1, String st2) {
    return (Strings.isNullOrEmpty(st1)) ? st2 : st1;
  }

  private com.google.api.client.util.DateTime toGoogleDateTime(DateTime start) {
    return new com.google.api.client.util.DateTime(start.getMillis());
  }
}

