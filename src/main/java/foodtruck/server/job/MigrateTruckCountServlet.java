package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 9/19/16
 */
@Singleton
public class MigrateTruckCountServlet extends HttpServlet {
  private static final int INTERVAL_IN_DAYS = 10;
  private final Clock clock;
  private final Provider<Queue> queueProvider;

  @Inject
  public MigrateTruckCountServlet(Clock clock, Provider<Queue> queueProvider) {
    this.clock = clock;
    this.queueProvider = queueProvider;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setHeader("Content-Type", "text/html");
    resp.getOutputStream()
        .print(
            "<html><body><form method='POST' action=''><p>Are you sure?</p><input type='submit' value='Submit'/></form></body></html>");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    DateTime finalEnd = clock.currentDay()
        .toDateTimeAtStartOfDay();

    DateTime startTime = new DateTime(2011, 8, 1, 1, 1, clock.zone());
    int days = INTERVAL_IN_DAYS;
    Queue queue = queueProvider.get();
    while (startTime.isBefore(finalEnd)) {
      queue.add(TaskOptions.Builder.withUrl("/cron/update_trucks_count_over_range")
          .param("startTime", String.valueOf(startTime.getMillis()))
          .param("days", String.valueOf(days)));
      startTime = startTime.plusDays(INTERVAL_IN_DAYS);
      Duration duration = new Duration(startTime.plusDays(days), finalEnd);
      if (duration.getStandardDays() < 0) {
        days = Ints.checkedCast(Math.abs(duration.getStandardDays()));
      } else {
        days = Ints.checkedCast(Math.min(INTERVAL_IN_DAYS, duration.getStandardDays()));
      }
    }
    resp.sendRedirect("/admin/trucks");
  }
}
