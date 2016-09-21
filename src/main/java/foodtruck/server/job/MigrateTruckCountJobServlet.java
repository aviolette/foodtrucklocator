package foodtruck.server.job;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.dao.DailyTruckStopDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.WeeklyTruckStopDAO;
import foodtruck.model.TruckStop;

import static foodtruck.server.job.PurgeStatsServlet.TRUCK_STOPS;
import static foodtruck.server.job.PurgeStatsServlet.UNIQUE_TRUCKS;

/**
 * One-off to migrate truck count
 *
 * @author aviolette
 * @since 9/21/16
 */
@SuppressWarnings("Duplicates")
@Singleton
public class MigrateTruckCountJobServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(MigrateTruckCountJobServlet.class.getName());

  private final TruckStopDAO truckStopDAO;
  private final DailyTruckStopDAO dailyDAO;
  private final WeeklyTruckStopDAO weeklyRollupDAO;

  @Inject
  public MigrateTruckCountJobServlet(TruckStopDAO truckStopDAO, DailyTruckStopDAO dailyRollupDAO,
      WeeklyTruckStopDAO weeklyRollupDAO) {
    this.truckStopDAO = truckStopDAO;
    this.dailyDAO = dailyRollupDAO;
    this.weeklyRollupDAO = weeklyRollupDAO;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    DateTime initialStart = new DateTime(Long.parseLong(req.getParameter("startTime")));
    int days = Integer.parseInt(req.getParameter("days"));
    for (int i = 0; i < days; i++) {
      try {
        DateTime startTime = initialStart.plusDays(i);
        DateTime endTime = startTime.plusDays(1);
        log.log(Level.INFO, "Migrating truck stops over range: {0} ", new Interval(startTime, endTime));
        List<TruckStop> truckStops = truckStopDAO.findOverRange(null, new Interval(startTime, endTime));
        //noinspection unchecked
        int vendorCount = FluentIterable.from(truckStops)
            .transform(TruckStop.TO_TRUCK_NAME)
            .toSet()
            .size();
        dailyDAO.updateCount(startTime.plusMinutes(1), TRUCK_STOPS, truckStops.size());
        dailyDAO.updateCount(startTime.plusMinutes(1), UNIQUE_TRUCKS, vendorCount);
        weeklyRollupDAO.updateCount(startTime.plusMinutes(1), TRUCK_STOPS, truckStops.size());
        weeklyRollupDAO.updateCount(startTime.plusMinutes(1), UNIQUE_TRUCKS, vendorCount);
      } catch (Throwable t) {
        log.log(Level.WARNING, t.getMessage(), t);
      }
    }
  }
}
