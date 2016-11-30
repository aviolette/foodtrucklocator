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
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 9/20/16
 */
@Singleton
public class StatUpdateQueueServlet extends HttpServlet {
  private final DailyRollupDAO dailyRollupDAO;
  private final FifteenMinuteRollupDAO fifteenMinuteRollupDAO;
  private final WeeklyRollupDAO weeklyRollupDAO;
  private final Clock clock;

  @Inject
  public StatUpdateQueueServlet(DailyRollupDAO dailyRollupDAO, FifteenMinuteRollupDAO fifteenMinuteRollupDAO,
      Clock clock, WeeklyRollupDAO weeklyRollupDAO) {
    this.dailyRollupDAO = dailyRollupDAO;
    this.fifteenMinuteRollupDAO = fifteenMinuteRollupDAO;
    this.clock = clock;
    this.weeklyRollupDAO = weeklyRollupDAO;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String param = req.getParameter("statName");
    fifteenMinuteRollupDAO.updateCount(clock.now(), param);
    dailyRollupDAO.updateCount(clock.now(), param);
    weeklyRollupDAO.updateCount(clock.now(), param);
  }
}
