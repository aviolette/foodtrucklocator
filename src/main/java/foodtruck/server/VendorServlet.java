package foodtruck.server;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.TruckSchedule;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 10/15/13
 */
@Singleton
public class VendorServlet extends VendorServletSupport {
  private static final String JSP = "/WEB-INF/jsp/vendor/vendordash.jsp";
  private final FoodTruckStopService truckStopService;
  private final Clock clock;

  @Inject
  public VendorServlet(TruckDAO dao, FoodTruckStopService truckStopService, Clock clock) {
    super(dao);
    this.truckStopService = truckStopService;
    this.clock = clock;
  }

  @Override protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp, @Nullable String truckId)
      throws ServletException, IOException {
    final TruckSchedule stopsForDay = truckStopService.findStopsForDay(truckId, clock.currentDay());
    req.setAttribute("schedule", stopsForDay);
    req.setAttribute("hasStops", !stopsForDay.getStops().isEmpty());
    req.setAttribute("tab", "vendorhome");
    req.getRequestDispatcher(JSP).forward(req, resp);
  }
}
