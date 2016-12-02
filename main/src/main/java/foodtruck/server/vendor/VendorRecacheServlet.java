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

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 10/23/13
 */
@Singleton
public class VendorRecacheServlet extends VendorServletSupport {
  private final FoodTruckStopService foodTruckStopService;
  private final Clock clock;

  @Inject
  protected VendorRecacheServlet(TruckDAO dao, FoodTruckStopService foodTruckStopService, Clock clock,
      UserService userService, Provider<SessionUser> sessionUserProvider) {
    super(dao, userService, sessionUserProvider);
    this.foodTruckStopService = foodTruckStopService;
    this.clock = clock;
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    LocalDate when = clock.currentDay();
    final Interval interval = when.toInterval(clock.zone())
        .withEnd(when.plusDays(7)
            .toDateTimeAtStartOfDay());
    foodTruckStopService.pullCustomCalendarFor(interval, truck);
  }
}
