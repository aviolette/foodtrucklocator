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

  @Inject
  public void PurgeStatsServlet(FifteenMinuteRollupDAO dao, Clock clock, MemcacheService service, ApplicationDAO appDAO,
      DailyRollupDAO dailyRollupDAO) {
    this.dao = dao;
    this.clock = clock;
    this.memcache = service;
    this.appDAO = appDAO;
    this.dailyDAO = dailyRollupDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    dao.deleteBefore(clock.currentDay().minusDays(2));
    // Need to roll these stats up, but for now just cancel
    for (Application application : appDAO.findAll()) {
      String key = "service.counts.daily." + application.getKey();
      try {
        dailyDAO.updateCount(clock.now().minusHours(1), key, (Long)memcache.get(key));
      } catch (Exception e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
      memcache.put(key, 0L);
    }
  }
}
