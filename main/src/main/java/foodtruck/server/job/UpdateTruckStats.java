package foodtruck.server.job;

import java.io.IOException;
import java.util.Set;
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
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;

/**
 * Called to update stats about a truck.  For instance, how many stops it made in the last year.
 * @author aviolette
 * @since 3/12/13
 */
@Singleton
public class UpdateTruckStats extends HttpServlet {
  private static final Logger log = Logger.getLogger(UpdateTruckStats.class.getName());
  private final FoodTruckStopService stopService;
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final WeeklyRollupDAO rollupDAO;

  @Inject
  public UpdateTruckStats(FoodTruckStopService foodTruckStopService, TruckDAO truckDAO, Clock clock, WeeklyRollupDAO rollupDAO) {
    this.stopService = foodTruckStopService;
    this.truckDAO = truckDAO;
    this.clock = clock;
    this.rollupDAO = rollupDAO;
  }
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String truckId = req.getParameter("truckId");
    boolean forceUpdate = "true".equals(req.getParameter("force"));
    if (!Strings.isNullOrEmpty(truckId)) {
      Truck truck = truckDAO.findById(truckId);
      updateTruck(truck, forceUpdate);
    } else {
      String range = req.getParameter("range");
      String[] flRange = null;
      if (!Strings.isNullOrEmpty(range)) {
        log.info("Using range: " + range);
        flRange = range.split(",");
      } else {
        log.info("Range not specified");
      }
      for (Truck truck : truckDAO.findAll()) {
        if (flRange != null) {
          truckId = truck.getId();
          if (truckId.charAt(0) < flRange[0].charAt(0) || truckId.charAt(0) > flRange[1].charAt(0)) {
            log.fine("CONTINUING FOR " + truckId);
            continue;
          } else {
            log.fine("TRUCK MATCHES RANGE " + truckId);
          }
        }
        updateTruck(truck, forceUpdate);
      }
    }
    log.info("Update of truck stats complete");
  }

  private void updateTruck(Truck truck, boolean forceUpdate) {
    log.info("Updating truck stats for " + truck.getId());
    // a date before this project started
    DateTime lastUpdate = new DateTime(2010, 1, 1, 0, 0, 0, 0);
    DateTime now = clock.now();
    long totalStops = 0, stopsThisYear =0;
    int year = now.getYear();
    DateTime lastSeen = null;
    Location whereLastSeen = null;
    Truck.Stats stats = truck.getStats();
    if (stats != null && !forceUpdate) {
      lastUpdate = stats.getLastUpdated();
      totalStops = stats.getTotalStops();
      lastSeen = stats.getLastSeen();
      stopsThisYear = (lastUpdate.getYear() == year) ? stats.getStopsThisYear() : 0;
    }
    String statName = "count." + truck.getId();
    if (forceUpdate) {
      rollupDAO.deleteStat(statName);
    }
    Set<Long> timestamps = Sets.newHashSet();
    for (TruckStop stop : stopService.findStopsForTruckSince(lastUpdate, truck.getId())) {
      final DateTime endTime = stop.getEndTime();
      if (endTime.getYear() == year) {
        stopsThisYear++;
      }
      if (lastSeen == null || endTime.isAfter(lastSeen)) {
        lastSeen = endTime;
        whereLastSeen = stop.getLocation();
      }
      timestamps.add(stop.getStartTime().withTimeAtStartOfDay().getMillis());
      totalStops++;
    }
    for (Long timestamp : timestamps) {
      rollupDAO.updateCount(new DateTime(timestamp, clock.zone()), statName, 1);
    }
    Truck.Stats.Builder builder = stats == null ? Truck.Stats.builder() : Truck.Stats.builder(stats);
    // fix a bug where this was getting trashed
    if (stats == null || stats.getFirstSeen() == null) {
      log.info("Correcting first-seen for " + truck.getId());
      TruckStop stop = stopService.findFirstStop(truck);
      if (stop != null) {
        builder.firstSeen(stop.getStartTime());
        builder.whereFirstSeen(stop.getLocation());
      }
    }
    stats = builder
        .lastUpdate(now)
        .totalStops(totalStops)
        .lastSeen(lastSeen)
        .stopsThisYear(stopsThisYear)
        .whereLastSeen(whereLastSeen)
        .build();
    truck = Truck.builder(truck).stats(stats).build();
    truckDAO.save(truck);
  }
}
