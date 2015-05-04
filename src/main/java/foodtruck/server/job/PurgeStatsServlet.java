package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ApplicationDAO;
import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.model.Application;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 12/9/12
 */
@Singleton
public class PurgeStatsServlet extends HttpServlet {
  private Clock clock;
  private FifteenMinuteRollupDAO dao;
  private MemcacheService memcache;
  private ApplicationDAO appDAO;

  @Inject
  public void PurgeStatsServlet(FifteenMinuteRollupDAO dao, Clock clock, MemcacheService service, ApplicationDAO appDAO) {
    this.dao = dao;
    this.clock = clock;
    this.memcache = service;
    this.appDAO = appDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    dao.deleteBefore(clock.currentDay().minusDays(2));
    // Need to roll these stats up, but for now just cancel
    for (Application application : appDAO.findAll()) {
      memcache.put("service.access.daily."+application.getKey(), 0L);
    }
  }
}
