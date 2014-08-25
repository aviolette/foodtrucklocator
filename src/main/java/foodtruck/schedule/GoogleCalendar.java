package foodtruck.schedule;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.monitoring.Monitored;
import foodtruck.util.Clock;
import foodtruck.util.FriendlyDateOnlyFormat;
import static foodtruck.schedule.TimeUtils.toJoda;

/**
 * Uses a google calendar feed to load a truck's schedule.
 * @author aviolette@gmail.com
 * @since 8/27/11
 */
public class GoogleCalendar implements ScheduleStrategy {
  private static final Logger log = Logger.getLogger(GoogleCalendar.class.getName());
  private final static int MAX_TRIES = 3;
  private final CalendarService calendarService;
  private final CalendarQueryFactory queryFactory;
  private final DateTimeZone defaultZone;
  private final GeoLocator geolocator;
  private final TruckDAO truckDAO;
  private final AddressExtractor addressExtractor;
  private final DateTimeFormatter formatter;
  private final Clock clock;

  @Inject
  public GoogleCalendar(CalendarService calendarService,
      CalendarQueryFactory queryFactory, DateTimeZone defaultZone, GeoLocator geolocator,
      TruckDAO truckDAO, AddressExtractor addressExtractor, @FriendlyDateOnlyFormat DateTimeFormatter formatter,
      Clock clock) {
    this.calendarService = calendarService;
    this.queryFactory = queryFactory;
    this.defaultZone = defaultZone;
    this.geolocator = geolocator;
    this.truckDAO = truckDAO;
    this.addressExtractor = addressExtractor;
    this.formatter = formatter;
    this.clock = clock;
  }

  // TODO: rewrite this...its awfully crappy
  @Override @Monitored
  public List<TruckStop> findForTime(Interval range, @Nullable Truck searchTruck) {
    CalendarQuery query = queryFactory.create();
    String truckId = searchTruck == null ? null : searchTruck.getId();
    log.info("Initiating calendar search " + truckId);
    List<TruckStop> stops = performTruckSearch(range, searchTruck, query, false);
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
      if (calendarUrl == null || !calendarUrl.startsWith("http")) {
        return;
      }
      log.info("Custom calendar search: " + calendarUrl);
      stops.addAll(performTruckSearch(range, truck,
          queryFactory.create(new URL(calendarUrl)), true));
    } catch (RuntimeException rte) {
      log.info("Search truck: " + truck.getId());
      log.severe(rte.getMessage());
    } catch (MalformedURLException e) {
      log.warning(e.getMessage());
    }
  }

  private List<TruckStop> performTruckSearch(Interval range, Truck searchTruck,
      CalendarQuery query, boolean customCalendar) {
    query.setMinimumStartTime(new DateTime(range.getStart().toDate(),
        defaultZone.toTimeZone()));
    query.setMaximumStartTime(new DateTime(range.getEnd().toDate(),
        defaultZone.toTimeZone()));
    query.setMaxResults(1000);
    if (searchTruck != null && !customCalendar) {
      query.setFullTextQuery(searchTruck.getId());
    }
    query.setStringCustomParameter("singleevents", "true");
    ImmutableList.Builder<TruckStop> builder = ImmutableList.builder();
    try {
      CalendarEventFeed resultFeed = calendarQuery(query);
      for (CalendarEventEntry entry : resultFeed.getEntries()) {
        final String titleText = entry.getTitle().getPlainText();
        if (Strings.isNullOrEmpty(titleText) && searchTruck == null) {
          Link htmlLink = entry.getHtmlLink();
          String entryString = (htmlLink == null) ? entry.getId() : htmlLink.getHref();
          log.log(Level.WARNING, "Could not find title text for {0}", new Object[] { entryString });
        }
        Truck truck = (searchTruck != null || Strings.isNullOrEmpty(titleText)) ? searchTruck :
            truckDAO.findById(titleText);
        if (truck == null) {
          continue;
        }
        Where where = Iterables.getFirst(entry.getLocations(), null);
        if (where == null) {
          throw new IllegalStateException("No location specified");
        }
        When time = Iterables.getFirst(entry.getTimes(), null);
        String whereString = where.getValueString();
        Location location = null;
        if (!Strings.isNullOrEmpty(whereString)) {
          if (whereString.endsWith(", United States")) {
            whereString = whereString.substring(0, whereString.lastIndexOf(","));
          }
          // HACK Alert, the address extractor doesn't handle non-Chicago addresses well, so
          // if it is a fully qualified address written by me, it will probably end in City, IL
          if (!whereString.endsWith(", IL")) {
            whereString =
                coalesce(Iterables.getFirst(addressExtractor.parse(whereString, truck), null),
                    whereString);
          }
          location = geolocator.locate(whereString, GeolocationGranularity.NARROW);
        }
        if ((location == null || !location.isResolved()) && customCalendar) {
          // Sometimes the location is in the title - try that too
          log.info("Trying title text: " + titleText);
          final List<String> parsed =
              addressExtractor.parse(titleText, searchTruck);
          String locString = Iterables.getFirst(parsed, null);
          if (locString == null) {
            log.info("Failed to parse titletext for address, trying whole thing: " + titleText);
            locString = titleText;
          }
          if (locString != null) {
            location = geolocator.locate(locString, GeolocationGranularity.NARROW);
          }
        }
        if (location != null && location.isResolved()) {
          final String entered = enteredOn(entry);
          String note = customCalendar ? "Stop added from vendor's calendar" :
              "Entered manually " + (entered == null ? "" : "on ") + entered;
          Confidence confidence = customCalendar ? Confidence.HIGH : Confidence.MEDIUM;
          final TruckStop truckStop = TruckStop.builder().truck(truck)
              .origin(customCalendar ? StopOrigin.VENDORCAL : StopOrigin.MANUAL)
              .location(location)
              .confidence(confidence)
              .appendNote(note)
              .startTime(toJoda(time.getStartTime(), defaultZone))
              .endTime(toJoda(time.getEndTime(), defaultZone)).build();
          log.log(Level.INFO, "Loaded truckstop: {0}", truckStop);
          builder.add(truckStop);
        } else {
          log.log(Level.WARNING, "Location could not be resolved for {0}, {1} between {2} and {3}",
              new Object[] {truck.getId(), where.getValueString(), range.getStart(),
                  range.getEnd()});
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
    return builder.build();
  }

  private @Nullable String enteredOn(CalendarEventEntry entry) {
    try {
      return formatter.print(entry.getUpdated().getValue());
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
      return clock.nowFormattedAsTime();
    }
  }

  // TODO: make this generic and pull it out
  private String coalesce(String st1, String st2) {
    return (Strings.isNullOrEmpty(st1)) ? st2 : st1;
  }

  private CalendarEventFeed calendarQuery(CalendarQuery query)
      throws IOException, ServiceException {
    for (int i = 0; i < MAX_TRIES; i++) {
      try {
        return calendarService.query(query, CalendarEventFeed.class);
      } catch (Exception timeout) {
        if ((i + 1) == MAX_TRIES) {
          throw new RuntimeException(timeout);
        }
      }
    }
    throw new RuntimeException("Exhausted number of tries");
  }
}
