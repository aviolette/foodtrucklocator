package foodtruck.server.job;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;
import foodtruck.schedule.GoogleCalendarReader;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2018-12-26
 */
@Singleton
public class SeedGoogleCalendarSchedule extends AbstractCalendarServlet {

  private static final Logger log = Logger.getLogger(SeedGoogleCalendarSchedule.class.getName());
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final Calendar calendarClient;
  private final GoogleCalendarReader reader;

  @Inject
  public SeedGoogleCalendarSchedule(TempTruckStopDAO dao, Clock clock, TruckDAO truckDAO,  Calendar calendarClient,
      GoogleCalendarReader reader) {
    super(dao);
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.calendarClient = calendarClient;
    this.reader = reader;
  }

  @Override
  protected List<TempTruckStop> doSearch(String calendar, String truckId, @Nullable String defaultLocation) {

    Truck truck = Strings.isNullOrEmpty(truckId) ? null : truckDAO.findByIdOpt(truckId).orElseThrow(() -> new RuntimeException("Truck not found: " + truckId));
    ImmutableList.Builder<TempTruckStop> builder = ImmutableList.builder();
    try {
      String pageToken = null;
      int timezoneAdjustment = truck == null ? 0 : truck.getTimezoneAdjustment();
      Interval range = new Interval(clock.now(), clock.now().plusDays(30));
      do {
        Calendar.Events.List query = calendarClient.events()
            .list(calendar)
            .setSingleEvents(true)
            .setTimeMin(toGoogleDateTime(range.getStart()))
            .setTimeMax(toGoogleDateTime(range.getEnd()))
            .setPageToken(pageToken);
        Events events = query.execute();
        events.getItems().stream()
            .map(event -> reader.buildTruckStop(truck, timezoneAdjustment, event, defaultLocation))
            .filter(Objects::nonNull)
            .forEach(builder::add);
        pageToken = events.getNextPageToken();
      } while (pageToken != null);
    } catch (IOException e) {
      log.log(Level.SEVERE, "An error occurred while caching the schedule", e);
    }
    return builder.build();
  }


  private com.google.api.client.util.DateTime toGoogleDateTime(DateTime start) {
    return new com.google.api.client.util.DateTime(start.getMillis());
  }

}
