package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.DailyRollupDAO;
import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.monitoring.CounterPublisher;
import foodtruck.monitoring.StackDriver;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 9/20/16
 */
@Singleton
public class StatUpdateQueueServlet extends HttpServlet {

  private final CounterPublisher publisher;

  @Inject
  public StatUpdateQueueServlet(@StackDriver CounterPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String param = req.getParameter("statName");
    int amount = 1;
    try {
      amount = Integer.parseInt(req.getParameter("amount"));
    } catch (NumberFormatException | NullPointerException ignored) {
    }
    publisher.increment(param, amount);
  }
}
