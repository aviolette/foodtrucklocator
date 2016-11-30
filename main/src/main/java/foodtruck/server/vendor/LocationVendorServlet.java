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

import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 7/8/16
 */
@Singleton
public class LocationVendorServlet extends VendorServletSupport {
  private static final String JSP = "/WEB-INF/jsp/vendor/location.jsp";
  private final FoodTruckStopService service;
  private final Clock clock;

  @Inject
  public LocationVendorServlet(TruckDAO dao, UserService userService, FoodTruckStopService service, Clock clock,
      Provider<SessionUser> sessionUserProvider) {
    super(dao, userService, sessionUserProvider);
    this.service = service;
    this.clock = clock;
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Location location) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    DateTime dt = clock.now();
    if (location != null && location.getImageUrl() != null) {
      req.setAttribute("vendorIconUrl", location.getImageUrl());
      req.setAttribute("vendorIconDescription", location.getName());
    }
    req.setAttribute("stops", service.findStopsAtLocationOverRange(location, new Interval(dt, dt.plusDays(365))));
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }
}
