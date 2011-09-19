package foodtruck.schedule;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
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

import foodtruck.geolocation.GeoLocator;
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
public class GoogleCalendarScheduleSearch {
  private final CalendarService calendarService;
  private final CalendarQueryFactory queryFactory;
  private final DateTimeZone defaultZone;
  private final GeoLocator geolocator;
  private final Map<String, Truck> trucks;
  private static final Logger log = Logger.getLogger(GoogleCalendarScheduleSearch.class.getName());


  @Inject
  public GoogleCalendarScheduleSearch(CalendarService calendarService,
      CalendarQueryFactory queryFactory, DateTimeZone defaultZone, GeoLocator geolocator,
      Map<String, Truck> trucks) {
    this.calendarService = calendarService;
    this.queryFactory = queryFactory;
    this.defaultZone = defaultZone;
    this.geolocator = geolocator;
    this.trucks = trucks;
  }

  public List<TruckStop> findForTime(@Nullable String searchString, TimeRange range) {
    CalendarQuery query = queryFactory.create();
    query.setMinimumStartTime(new DateTime(range.getStartDateTime().toDate(),
        defaultZone.toTimeZone()));
    query.setMaximumStartTime(new DateTime(range.getEndDateTime().toDate(),
        defaultZone.toTimeZone()));
    query.setStringCustomParameter("singleevents", "true");
    if (!Strings.isNullOrEmpty(searchString)) {
      query.setFullTextQuery(searchString);
    }
    ImmutableList.Builder<TruckStop> builder = ImmutableList.builder();
    try {
      CalendarEventFeed resultFeed = calendarService.query(query, CalendarEventFeed.class);

      for (CalendarEventEntry entry : resultFeed.getEntries()) {
        try {
          builder.add(parseCalendarEntry(range, entry));
        } catch (IllegalStateException ise) {
          log.log(Level.WARNING, ise.getMessage(), ise);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
    return builder.build();
  }

  @VisibleForTesting
  TruckStop parseCalendarEntry(TimeRange range, CalendarEventEntry entry) {
    Truck truck = trucks.get(entry.getTitle().getPlainText());
    if (truck == null) {
      throw new IllegalStateException("Truck with ID: " + entry.getTitle().getPlainText() +
          " could not be resolved");
    }
    Where where = Iterables.getFirst(entry.getLocations(), null);
    if (where == null) {
      throw new IllegalStateException("No location specified for truck " + truck.getId());
    }
    final Location location = geolocator.locate(where.getValueString());
    if (location != null) {
      When time = Iterables.getFirst(entry.getTimes(), null);
      return new TruckStop(truck, toJoda(time.getStartTime()), toJoda(time.getEndTime()), location);
    } else {
      String message =
          MessageFormat.format("Location could not be resolved for {0}, {1} between {2} and {3}",
              truck.getId(), where.getValueString(), range.getStartDateTime(),
              range.getEndDateTime());
      throw new IllegalStateException(message);
    }
  }
}
