package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 3/12/13
 */
@Singleton
public class UpdateTruckStats extends HttpServlet {
  private static final Logger log = Logger.getLogger(UpdateTruckStats.class.getName());
  private final FoodTruckStopService stopService;
  private final TruckDAO truckDAO;
  private final Clock clock;

  @Inject
  public UpdateTruckStats(FoodTruckStopService foodTruckStopService, TruckDAO truckDAO, Clock clock) {
    this.stopService = foodTruckStopService;
    this.truckDAO = truckDAO;
    this.clock = clock;
  }
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String truckId = req.getParameter("truckId");
    boolean forceUpdate = "true".equals(req.getParameter("force"));
    if (!Strings.isNullOrEmpty(truckId)) {
      Truck truck = truckDAO.findById(truckId);
      updateTruck(truck, forceUpdate);
    } else {
      for (Truck truck : truckDAO.findAll()) {
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
      stopsThisYear = (lastUpdate.getYear() == year) ? stopsThisYear : 0;
    }
    for (TruckStop stop : stopService.findStopsForTruckSince(lastUpdate, truck.getId())) {
      final DateTime endTime = stop.getEndTime();
      if (endTime.getYear() == year) {
        stopsThisYear++;
      }
      if (lastSeen == null || endTime.isAfter(lastSeen)) {
        lastSeen = endTime;
        whereLastSeen = stop.getLocation();
      }
      totalStops++;
    }
    stats = Truck.Stats.builder()
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
