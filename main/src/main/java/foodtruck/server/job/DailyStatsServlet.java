package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Truck;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2019-01-02
 */

@Singleton
public class DailyStatsServlet extends HttpServlet {

  private final TruckDAO truckDAO;
  private final TruckStopDAO stopDAO;
  private final Clock clock;

  @Inject
  public DailyStatsServlet(Clock clock, TruckStopDAO stopDAO, TruckDAO truckDAO) {
    this.stopDAO = stopDAO;
    this.truckDAO = truckDAO;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String start = req.getParameter("start");
    LocalDate startDate = clock.currentDay().minusDays(1);
    if (!Strings.isNullOrEmpty(start)) {
      startDate = ISODateTimeFormat.basicDate().parseLocalDate(start);
    }

    stopDAO.findOverRange(null, new Interval(startDate.toDateTimeAtStartOfDay(clock.zone()), clock.currentDay()
        .toDateTimeAtStartOfDay(clock.zone())))
        .forEach(stop -> truckDAO.findByIdOpt(stop.getTruck().getId())
            .ifPresent(truck -> {
              Truck.Stats stats = truck.getStats();
              Truck.Stats.Builder statsBuilder = new Truck.Stats.Builder(stats);
              if (stats.getFirstSeen() == null) {
                statsBuilder.firstSeen(stop.getStartTime());
                statsBuilder.whereFirstSeen(stop.getLocation());
              }
              statsBuilder.totalStops(stats.getTotalStops() + 1);
              if (stats.getLastSeen() == null || stats.getLastSeen().isBefore(stop.getEndTime())) {
                statsBuilder.lastSeen(stop.getEndTime());
                statsBuilder.whereLastSeen(stop.getLocation());
              }
              truckDAO.save(Truck.builder(truck)
                  .stats(statsBuilder.build())
                  .build());
            }));
  }
}
