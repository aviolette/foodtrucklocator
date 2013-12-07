package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.DateOnlyFormatter;

/**
 * @author aviolette
 * @since 12/6/13
 */

@Singleton
public class LocationServlet extends FrontPageServlet {
  private final LocationDAO locationDAO;
  private final Clock clock;
  private final DateTimeFormatter dateFormatter;
  private final FoodTruckStopService truckStopService;

  @Inject
  public LocationServlet(ConfigurationDAO configDAO, LocationDAO locationDAO, Clock clock,
      @DateOnlyFormatter DateTimeFormatter dateFormatter, FoodTruckStopService truckStopService) {
    super(configDAO);
    this.locationDAO = locationDAO;
    this.clock = clock;
    this.dateFormatter = dateFormatter;
    this.truckStopService = truckStopService;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    String locationId = (requestURI.equals("/locations") || requestURI.equals("/locations/") ? null : requestURI.substring(11));
    Location location;
    long locId;
    try {
      locId = Long.parseLong(locationId);
    } catch (Exception e) {
      resp.setStatus(404);
      return;
    }
    if ((location = locationDAO.findById(locId)) == null || !location.isResolved()) {
      resp.setStatus(404);
      return;
    }
    final String timeRequest = req.getParameter("date");
    DateTime dateTime = null;
    if (!Strings.isNullOrEmpty(timeRequest)) {
      try {
        dateTime = dateFormatter.parseDateTime(timeRequest);
      } catch (IllegalArgumentException ignored) {
      }
    }
    if (dateTime == null) {
      dateTime = clock.now();
    }
    req.setAttribute("stops", truckStopService.findStopsNearALocation(location, dateTime.toLocalDate()));
    req.setAttribute("thedate", dateTime);
    req.setAttribute("location", location);
    String jsp = "/WEB-INF/jsp/location.jsp";
    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("containerType", "fixed");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
