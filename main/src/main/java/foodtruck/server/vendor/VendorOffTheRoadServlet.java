package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.model.Truck;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 10/31/13
 */
@Singleton
public class VendorOffTheRoadServlet extends HttpServlet {
  private final FoodTruckStopService stopService;
  private final Clock clock;

  @Inject
  protected VendorOffTheRoadServlet(FoodTruckStopService foodTruckStopService, Clock clock) {
    this.stopService = foodTruckStopService;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    stopService.offRoad(truck.getId(), clock.currentDay());
  }
}
