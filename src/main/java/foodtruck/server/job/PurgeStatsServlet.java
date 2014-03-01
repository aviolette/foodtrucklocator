package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 12/9/12
 */
@Singleton
public class PurgeStatsServlet extends HttpServlet {
  private Clock clock;
  private FifteenMinuteRollupDAO dao;

  @Inject
  public void PurgeStatsServlet(FifteenMinuteRollupDAO dao, Clock clock) {
    this.dao = dao;
    this.clock = clock;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    dao.deleteBefore(clock.currentDay().minusDays(2));
  }
}
