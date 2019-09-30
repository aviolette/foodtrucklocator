package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.model.Location;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 7/8/16
 */
@Singleton
public class LocationVendorServlet extends HttpServlet {
  private static final String JSP = "/WEB-INF/jsp/vendor/location.jsp";
  private final FoodTruckStopService service;
  private final Clock clock;

  @Inject
  public LocationVendorServlet(FoodTruckStopService service, Clock clock) {
    this.service = service;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    DateTime dt = clock.timeAt(0, 0);
    Location location = (Location) req.getAttribute(VendorPageFilter.LOCATION);
    req.setAttribute("locationId", location.getKey());
    req.setAttribute("stops", service.findStopsAtLocationOverRange(location, new Interval(dt, dt.plusDays(365))));
    req.getRequestDispatcher(JSP).forward(req, resp);
  }
}
