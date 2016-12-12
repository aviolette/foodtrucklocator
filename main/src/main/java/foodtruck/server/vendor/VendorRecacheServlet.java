package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.model.Truck;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 10/23/13
 */
@Singleton
public class VendorRecacheServlet extends HttpServlet {
  private final FoodTruckStopService foodTruckStopService;
  private final Clock clock;

  @Inject
  protected VendorRecacheServlet(FoodTruckStopService foodTruckStopService, Clock clock) {
    this.foodTruckStopService = foodTruckStopService;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LocalDate when = clock.currentDay();
    final Interval interval = when.toInterval(clock.zone())
        .withEnd(when.plusDays(7)
            .toDateTimeAtStartOfDay());
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    foodTruckStopService.pullCustomCalendarFor(interval, truck);
  }
}
