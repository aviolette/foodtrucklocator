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

import foodtruck.dao.DailyRollupDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.model.TruckStop;

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
  private final DailyRollupDAO dailyDAO;
  private final WeeklyRollupDAO weeklyRollupDAO;

  @Inject
  public MigrateTruckCountJobServlet(TruckStopDAO truckStopDAO, DailyRollupDAO dailyRollupDAO,
      WeeklyRollupDAO weeklyRollupDAO) {
    this.truckStopDAO = truckStopDAO;
    this.dailyDAO = dailyRollupDAO;
    this.weeklyRollupDAO = weeklyRollupDAO;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    DateTime startTime = new DateTime(Long.parseLong(req.getParameter("startTime")));
    DateTime endTime = new DateTime(Long.parseLong(req.getParameter("endTime")));
    log.log(Level.INFO, "Migrating truck stops over range: {0} ", new Interval(startTime, endTime));
    List<TruckStop> truckStops = truckStopDAO.findOverRange(null, new Interval(startTime, endTime));
    //noinspection unchecked
    int vendorCount = FluentIterable.from(truckStops)
        .transform(TruckStop.TO_TRUCK_NAME)
        .toSet()
        .size();
    dailyDAO.updateCount(startTime.plusMinutes(1), "truckstops", truckStops.size());
    dailyDAO.updateCount(startTime.plusMinutes(1), "vendor_stops", truckStops.size());
    weeklyRollupDAO.updateCount(startTime.plusMinutes(1), "vendor_stops", vendorCount);
    weeklyRollupDAO.updateCount(startTime.plusMinutes(1), "truckstops", vendorCount);
  }
}
