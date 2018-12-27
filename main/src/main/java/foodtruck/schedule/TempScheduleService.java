package foodtruck.schedule;

import java.util.Objects;
import java.util.logging.Logger;

import com.google.appengine.api.taskqueue.Queue;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.dao.TruckDAO;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

/**
 * @author aviolette
 * @since 2018-12-11
 */
public class TempScheduleService {

  private static final Logger log = Logger.getLogger(TempScheduleService.class.getName());

  private final TempTruckStopDAO dao;
  private final Provider<Queue> queueProvider;
  private final TruckDAO truckDAO;

  @Inject
  public TempScheduleService(TempTruckStopDAO dao, Provider<Queue> queueProvider, TruckDAO truckDAO) {
    this.dao = dao;
    this.queueProvider = queueProvider;
    this.truckDAO = truckDAO;
  }

  public void rebuild() {
    dao.deleteAll();
    Queue queue = queueProvider.get();
    queue.add(withUrl("/cron/populate_imperial_oaks_stops"));
    queue.add(withUrl("/cron/populate_coastline_cove"));
    queue.add(withUrl("/cron/populate_skeleton_key"));
    queue.add(withUrl("/cron/populate_pollyanna_schedule"));
    queue.add(withUrl("/cron/populate_fat_shallot"));
    truckDAO.findTruckWithICalCalendars()
        .forEach(truck -> queue.add(
            withUrl("/cron/populate_ical_stops")
                .param("calendar", Objects.requireNonNull(truck.getIcalCalendar()))
                .param("truck", truck.getId())));
    truckDAO.findTrucksWithCalendars().forEach(truck -> queue.add(withUrl("/cron/populate_google_calendar_schedule")
        .param("calendar", Objects.requireNonNull(truck.getCalendarUrl()))
        .param("truck", truck.getId())));
    queue.add(withUrl("/cron/populate_plank_road_schedule"));
    queue.add(withUrl("/cron/populate_google_calendar_schedule")
        .param("calendar", "oswegobrewing.com_rg6gupgfqs5d3h97ur31ed88i0@group.calendar.google.com")
        .param("defaultLocation", "Oswego Brewing Co."));
  }
}
