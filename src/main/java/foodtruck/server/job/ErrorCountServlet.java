package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 7/1/14
 */
@Singleton
public class ErrorCountServlet extends HttpServlet {
  private final Clock clock;
  private final FifteenMinuteRollupDAO dao;
  private static final Logger log = Logger.getLogger(ErrorCountServlet.class.getName());

  @Inject
  public ErrorCountServlet(Clock clock, FifteenMinuteRollupDAO dao) {
    this.clock = clock;
    this.dao = dao;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    LogQuery query = LogQuery.Builder
        .withStartTimeMillis(clock.now().minusMinutes(15).getMillis());
    query.endTimeMillis(clock.now().getMillis());
    query.minLogLevel(LogService.LogLevel.ERROR);
    int count = Iterables.size(LogServiceFactory.getLogService().fetch(query));
    log.log(Level.INFO, "There were {0} errors in the last 15 minutes", count);
    dao.updateCount(clock.now(), "app_error_count", count);
  }
}
