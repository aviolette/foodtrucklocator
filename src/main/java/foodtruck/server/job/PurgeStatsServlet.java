package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ApplicationDAO;
import foodtruck.dao.DailyRollupDAO;
import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.model.Application;
import foodtruck.monitoring.Counter;
import foodtruck.monitoring.DailyScheduleCounter;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 12/9/12
 */
@Singleton
public class PurgeStatsServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(PurgeStatsServlet.class.getName());
  private Clock clock;
  private FifteenMinuteRollupDAO dao;
  private MemcacheService memcache;
  private ApplicationDAO appDAO;
  private DailyRollupDAO dailyDAO;
  private Counter dailyCounter;

  @Inject
  public PurgeStatsServlet(FifteenMinuteRollupDAO dao, Clock clock, MemcacheService service, ApplicationDAO appDAO,
      DailyRollupDAO dailyRollupDAO, @DailyScheduleCounter Counter dailyCounter) {
    this.dao = dao;
    this.clock = clock;
    this.memcache = service;
    this.appDAO = appDAO;
    this.dailyDAO = dailyRollupDAO;
    this.dailyCounter = dailyCounter;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    log.info("Purging stats");
    dao.deleteBefore(clock.currentDay().minusDays(2));
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
