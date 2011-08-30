package foodtruck.schedule;

import java.io.IOException;
import java.util.List;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
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
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import static foodtruck.schedule.TimeUtils.toJoda;

/**
 * Uses a google calendar feed to load a truck's schedule.
 * @author aviolette@gmail.com
 * @since 8/27/11
 */
public class GoogleCalendarStrategy implements ScheduleStrategy {
  private final CalendarService calendarService;
  private final CalendarQueryFactory queryFactory;
  private final DateTimeZone defaultZone;
  private final GeoLocator geolocator;

  @Inject
  public GoogleCalendarStrategy(CalendarService calendarService,
      CalendarQueryFactory queryFactory, DateTimeZone defaultZone, GeoLocator geolocator) {
    this.calendarService = calendarService;
    this.queryFactory = queryFactory;
    this.defaultZone = defaultZone;
    this.geolocator = geolocator;
  }

  @Override public List<TruckStop> findForTime(Truck truck, TimeRange range) {
    CalendarQuery query = queryFactory.create();
    query.setMinimumStartTime(new DateTime(range.getStartDateTime().toDate(),
        defaultZone.toTimeZone()));
    query.setMaximumStartTime(new DateTime(range.getEndDateTime().toDate(),
        defaultZone.toTimeZone()));
    query.setStringCustomParameter("singleevents", "true");
    query.setFullTextQuery(truck.getId());
    ImmutableList.Builder<TruckStop> builder = ImmutableList.builder();
    try {
      CalendarEventFeed resultFeed = calendarService.query(query, CalendarEventFeed.class);

      for (CalendarEventEntry entry : resultFeed.getEntries()) {
        Where where = Iterables.getFirst(entry.getLocations(), null);
        if (where == null) {
          throw new IllegalStateException("No location specified");
        }
        When time = Iterables.getFirst(entry.getTimes(), null);
        builder.add(new TruckStop(truck, toJoda(time.getStartTime()),
            toJoda(time.getEndTime()), geolocator.locate(where.getValueString())));
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
    return builder.build();
  }
}
