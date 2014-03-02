package foodtruck.server.job;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;

import foodtruck.dao.TruckDAO;
import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.model.Location;
import foodtruck.model.SystemStats;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.ClockImpl;
import foodtruck.util.Slots;
import foodtruck.util.WeeklyRollup;

/**
 * This is really awful one-off code for migrating the truck stop data into timeseries data.
 * @author aviolette
 * @since 2/28/14
 */
@Singleton
public class MigrateTimeSeries extends HttpServlet {
  private final FoodTruckStopService service;
  private final WeeklyRollupDAO rollupDAO;
  private static final Logger log = Logger.getLogger(MigrateTimeSeries.class.getName());
  private final Slots slotter;
  private final TruckDAO truckDAO;
  private final Clock clock;

  @Inject
  public MigrateTimeSeries(FoodTruckStopService foodTruckStopService, WeeklyRollupDAO rollupDAO,
      @WeeklyRollup foodtruck.util.Slots slotter, TruckDAO dao, Clock clock) {
    this.service = foodTruckStopService;
    this.rollupDAO = rollupDAO;
    this.slotter = slotter;
    this.truckDAO = dao;
    this.clock = clock;
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if ("true".equals(req.getParameter("delete"))) {
      rollupDAO.deleteBefore(clock.currentDay().plusDays(1));
    } else if ("true".equals(req.getParameter("compress"))) {
      log.info("Compressing data");
      SystemStats prev = null;
      for (SystemStats stat : rollupDAO.findAll()) {
        if (prev != null) {
          if (prev.getTimeStamp() == stat.getTimeStamp()) {
            log.log(Level.INFO, "Combining: {0}", stat.getTimeStamp());
            rollupDAO.delete((Long) prev.getKey());
            rollupDAO.save(stat.merge(prev));
          }
        }
        prev = stat;
      }
      log.info("Done with compression");
    }
    resp.sendRedirect("/cron/migrateWeeklyStats");
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String truckId = req.getParameter("truckId"), statName = "count." + truckId;
    if (!Strings.isNullOrEmpty(truckId)) {
      DateTime firstSeen = null;
      Location whereFirstSeen = null;
      log.log(Level.INFO, "Starting migration for {0}", truckId);
      Set<Long> timestamps = Sets.newHashSet();
      for (TruckStop stop : service.findStopsForTruckSince(new DateTime(2010, 11, 11, 11, 11), truckId)) {
        if (firstSeen == null || stop.getStartTime().isBefore(firstSeen)) {
          firstSeen = stop.getStartTime();
          whereFirstSeen = stop.getLocation();
        }
        timestamps.add(stop.getStartTime().toDateMidnight().getMillis());
      }
      log.log(Level.INFO, "Finished migration for {0}", truckId);
      if (firstSeen != null) {
        Truck truck = truckDAO.findById(truckId);
        Truck.Stats stats = new Truck.Stats.Builder(truck.getStats()).firstSeen(firstSeen).whereFirstSeen(whereFirstSeen).build();
        truck = Truck.builder(truck).stats(stats).build();
        truckDAO.save(truck);
      }
      for (Long timestamp : timestamps) {
        rollupDAO.updateCount(slotter.getSlot(timestamp),  statName, 1);
      }
      boolean pickNext = false;
      String nextTruckId = null;
      for (Truck truck : truckDAO.findAll()) {
        if (pickNext) {
          nextTruckId = truck.getId();
          break;
        } else if (truckId.equals(truck.getId())) {
          pickNext = true;
        }
      }
      req.setAttribute("nextTruckId", nextTruckId);
    }
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/migrate_trucks.jsp").forward(req, resp);

  }
}
