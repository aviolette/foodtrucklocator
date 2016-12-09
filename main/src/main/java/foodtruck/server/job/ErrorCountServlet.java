package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.DailyRollupDAO;
import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.mail.SystemNotificationService;
import foodtruck.monitoring.CounterPublisher;
import foodtruck.time.Clock;

/**
 * Detects errors that have been logged and sends out a system notification.
 * @author aviolette
 * @since 7/1/14
 */
@Singleton
public class ErrorCountServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(ErrorCountServlet.class.getName());
  private static final String APP_ERROR_COUNT = "app_error_count";
  private final Clock clock;
  private final SystemNotificationService notifier;
  private final CounterPublisher publisher;

  @Inject
  public ErrorCountServlet(Clock clock, SystemNotificationService notifier, CounterPublisher publisher) {
    this.clock = clock;
    this.notifier = notifier;
    this.publisher = publisher;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    LogQuery query = LogQuery.Builder
        .withStartTimeMillis(clock.now().minusMinutes(15).getMillis());
    query.endTimeMillis(clock.now().getMillis());
    query.minLogLevel(LogService.LogLevel.ERROR);
    query.includeAppLogs(true);
    final Iterable<RequestLogs> results = LogServiceFactory.getLogService().fetch(query);
    int count = Iterables.size(results);
    log.log(Level.INFO, "There were {0} errors in the last 15 minutes", count);
    if (count > 0) {
      publisher.increment(APP_ERROR_COUNT, count);
      StringBuilder builder = new StringBuilder();
      for (RequestLogs logs : results) {
        builder.append(Joiner.on("\n\n").join(FluentIterable.from(logs.getAppLogLines())
            .transform(new Function<AppLogLine, String>() {
              @Nullable @Override public String apply(@Nullable AppLogLine logLine) {
                return logLine.getLogMessage();
              }
            })));
        builder.append("\n\n");
      }
      notifier.systemNotifyWarnError(builder.toString());
    }
  }
}
