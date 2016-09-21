package foodtruck.server.job;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.dao.ApplicationDAO;
import foodtruck.dao.DailyRollupDAO;
import foodtruck.dao.DailyTruckStopDAO;
import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.dao.TimeSeriesDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.WeeklyTruckStopDAO;
import foodtruck.model.Application;
import foodtruck.model.TruckStop;
import foodtruck.monitoring.Counter;
import foodtruck.monitoring.DailyScheduleCounter;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 12/9/12
 */
@Singleton
public class PurgeStatsServlet extends HttpServlet {
  public static final String TRUCK_STOPS = "trucks_on_the_road";
  public static final String UNIQUE_TRUCKS = "unique_trucks_on_the_road";
  private static final Logger log = Logger.getLogger(PurgeStatsServlet.class.getName());
  private final TruckStopDAO truckStopDAO;
  private final Clock clock;
  private final FifteenMinuteRollupDAO dao;
  private final MemcacheService memcache;
  private final ApplicationDAO appDAO;
  private final DailyRollupDAO dailyDAO;
  private final Counter dailyCounter;
  private final TimeSeriesDAO weeklyTruckStopDAO;
  private final TimeSeriesDAO dailyTruckStopDAO;

  @Inject
  public PurgeStatsServlet(FifteenMinuteRollupDAO dao, Clock clock, MemcacheService service, ApplicationDAO appDAO,
      DailyRollupDAO dailyRollupDAO, @DailyScheduleCounter Counter dailyCounter, TruckStopDAO truckStopDAO,
      WeeklyTruckStopDAO weeklyRollupDAO, DailyTruckStopDAO dailyTruckStopDAO) {
    this.dao = dao;
    this.clock = clock;
    this.memcache = service;
    this.appDAO = appDAO;
    this.dailyDAO = dailyRollupDAO;
    this.dailyCounter = dailyCounter;
    this.truckStopDAO = truckStopDAO;
    this.weeklyTruckStopDAO = weeklyRollupDAO;
    this.dailyTruckStopDAO = dailyTruckStopDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    log.info("Purging stats");

    purge15minStats();
    updateApplicationStatsCount();
    updateTrucksOnRoadCount();
  }

  private void purge15minStats() {
    dao.deleteBefore(clock.currentDay().minusDays(2));
  }

  private void updateTrucksOnRoadCount() {
    DateTime startTime = clock.currentDay()
        .minusDays(1)
        .toDateTimeAtStartOfDay();
    DateTime endTime = clock.currentDay()
        .toDateTimeAtStartOfDay();
    List<TruckStop> truckStops = truckStopDAO.findOverRange(null, new Interval(startTime, endTime));
    //noinspection unchecked
    int vendorCount = FluentIterable.from(truckStops)
        .transform(TruckStop.TO_TRUCK_NAME)
        .toSet()
        .size();
    dailyTruckStopDAO.updateCount(startTime.plusMinutes(1), TRUCK_STOPS, truckStops.size());
    dailyTruckStopDAO.updateCount(startTime.plusMinutes(1), UNIQUE_TRUCKS, vendorCount);
    weeklyTruckStopDAO.updateCount(startTime.plusMinutes(1), TRUCK_STOPS, truckStops.size());
    weeklyTruckStopDAO.updateCount(startTime.plusMinutes(1), UNIQUE_TRUCKS, vendorCount);
  }


  private void updateApplicationStatsCount() {
    // Need to roll these stats up, but for now just cancel
    for (Application application : appDAO.findAll()) {
      String key = "service.count.daily." + application.getKey();
      try {
        long count = dailyCounter.getCount((String) application.getKey());
        log.log(Level.INFO, "Updating {0} with {1}", new Object[] {key, count});
        dailyDAO.updateCount(clock.now().minusHours(1), key, count);
      } catch (Exception e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
      dailyCounter.clear((String)application.getKey());
      memcache.delete(key);
    }
  }
}
