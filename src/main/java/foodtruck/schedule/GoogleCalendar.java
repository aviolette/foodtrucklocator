package foodtruck.schedule;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
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

  @Inject
  public GoogleCalendar(CalendarService calendarService,
      CalendarQueryFactory queryFactory, DateTimeZone defaultZone, GeoLocator geolocator,
      TruckDAO truckDAO) {
    this.calendarService = calendarService;
    this.queryFactory = queryFactory;
    this.defaultZone = defaultZone;
    this.geolocator = geolocator;
    this.truckDAO = truckDAO;
  }

  // TODO: rewrite this...its awfully crappy
  @Override
  public List<TruckStop> findForTime(TimeRange range, @Nullable Truck searchTruck) {
    CalendarQuery query = queryFactory.create();
    List<TruckStop> stops = performTruckSearch(range, searchTruck, query);
    stops = Lists.newLinkedList(stops);
    if (searchTruck != null && !Strings.isNullOrEmpty(searchTruck.getCalendarUrl())) {
      saveTruckSearch(range, searchTruck, stops, searchTruck);
    } else if (searchTruck == null) {
      for (Truck truck : truckDAO.findTrucksWithCalendars()) {
        saveTruckSearch(range, truck, stops, truck);
      }
    }
    return stops;
  }

  private void saveTruckSearch(TimeRange range, Truck searchTruck, List<TruckStop> stops,
      Truck truck) {
    try {
      final String calendarUrl = truck.getCalendarUrl();
      if (calendarUrl == null || !calendarUrl.startsWith("http")) {
        return;
      }
      stops.addAll(performTruckSearch(range, searchTruck,
          queryFactory.create(new URL(calendarUrl))));
    } catch (MalformedURLException e) {
      log.warning(e.getMessage());
    }
  }

  private List<TruckStop> performTruckSearch(TimeRange range, Truck searchTruck,
      CalendarQuery query) {
    query.setMinimumStartTime(new DateTime(range.getStartDateTime().toDate(),
        defaultZone.toTimeZone()));
    query.setMaximumStartTime(new DateTime(range.getEndDateTime().toDate(),
        defaultZone.toTimeZone()));
    query.setMaxResults(1000);
    if (searchTruck != null) {
      query.setFullTextQuery(searchTruck.getId());
    }
    query.setStringCustomParameter("singleevents", "true");
    ImmutableList.Builder<TruckStop> builder = ImmutableList.builder();
    try {
      CalendarEventFeed resultFeed = calendarQuery(query);
      for (CalendarEventEntry entry : resultFeed.getEntries()) {
        Truck truck = (searchTruck != null) ? searchTruck :
            truckDAO.findById(entry.getTitle().getPlainText());
        if (truck == null) {
          continue;
        }
        Where where = Iterables.getFirst(entry.getLocations(), null);
        if (where == null) {
          throw new IllegalStateException("No location specified");
        }
        When time = Iterables.getFirst(entry.getTimes(), null);
        final Location location = geolocator.locate(where.getValueString(),
            GeolocationGranularity.BROAD);
        if (location != null) {
          final TruckStop truckStop = new TruckStop(truck, toJoda(time.getStartTime(), defaultZone),
              toJoda(time.getEndTime(), defaultZone), location, null);
          log.log(Level.INFO, "Loaded truckstop: {0}", truckStop);
          builder.add(truckStop);
        } else {
          log.log(Level.WARNING, "Location could not be resolved for {0}, {1} between {2} and {3}",
              new Object[] {truck.getId(), where.getValueString(), range.getStartDateTime(),
                  range.getEndDateTime()});
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
    return builder.build();
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
