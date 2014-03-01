package foodtruck.server.job;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;

import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Slots;
import foodtruck.util.WeeklyRollup;

/**
 * @author aviolette
 * @since 2/28/14
 */
@Singleton
public class MigrateTimeSeries extends HttpServlet {
  private final FoodTruckStopService service;
  private final WeeklyRollupDAO rollupDAO;
  private static final Logger log = Logger.getLogger(MigrateTimeSeries.class.getName());
  private final Slots slotter;

  @Inject
  public MigrateTimeSeries(FoodTruckStopService foodTruckStopService, WeeklyRollupDAO rollupDAO,
      @WeeklyRollup foodtruck.util.Slots slotter) {
    this.service = foodTruckStopService;
    this.rollupDAO = rollupDAO;
    this.slotter = slotter;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String truckId = req.getParameter("truckId"), statName = "count." + truckId;
    log.log(Level.INFO, "Starting migration for {0}", truckId);
    Map<Long, Long> tsToCount = Maps.newHashMap();
    for (TruckStop stop : service.findStopsForTruckSince(new DateTime(2010, 11, 11, 11, 11), truckId)) {
//      rollupDAO.updateCount(stop.getStartTime(), statName);
      long slot = slotter.getSlot(stop.getStartTime().getMillis()), value = 1;
      if (tsToCount.containsKey(slot)) {
        value = tsToCount.get(slot) + 1;
      }
      tsToCount.put(slot, value);
    }
    for (Map.Entry<Long, Long> entry : tsToCount.entrySet()) {
      rollupDAO.updateCount(entry.getKey(),  statName, entry.getValue());
    }
    log.log(Level.INFO, "Finished migration for {0}", truckId);
  }
}
