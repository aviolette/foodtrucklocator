package foodtruck.server.job;

import java.io.IOException;

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
    dao.updateCount(clock.now(), "app_error_count", count);
  }
}
