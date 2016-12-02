package foodtruck.server.vendor;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 10/31/13
 */
@Singleton
public class VendorOffTheRoadServlet extends VendorServletSupport {
  private final FoodTruckStopService stopService;
  private final Clock clock;

  @Inject
  protected VendorOffTheRoadServlet(TruckDAO dao, FoodTruckStopService foodTruckStopService, Clock clock,
      UserService userService, Provider<SessionUser> sessionUserProvider) {
    super(dao, userService, sessionUserProvider);
    this.stopService = foodTruckStopService;
    this.clock = clock;
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    stopService.offRoad(truck.getId(), clock.currentDay());
  }
}
