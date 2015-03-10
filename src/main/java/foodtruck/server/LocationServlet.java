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

import foodtruck.dao.LocationDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.DateOnlyFormatter;
import foodtruck.util.FriendlyDateOnlyFormat;

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
  private final GeoLocator geoLocator;
  private final DateTimeFormatter friendlyFormatter;

  @Inject
  public LocationServlet(LocationDAO locationDAO, Clock clock,
      @DateOnlyFormatter DateTimeFormatter dateFormatter,
      FoodTruckStopService truckStopService,
      GeoLocator geoLocator,
      @FriendlyDateOnlyFormat DateTimeFormatter friendlyFormatter, StaticConfig staticConfig) {
    super(staticConfig);
    this.locationDAO = locationDAO;
    this.clock = clock;
    this.dateFormatter = dateFormatter;
    this.truckStopService = truckStopService;
    this.geoLocator = geoLocator;
    this.friendlyFormatter = friendlyFormatter;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    String locationId = (requestURI.equals("/locations") || requestURI.equals("/locations/") ? null : requestURI.substring(11));
    Location location;
    long locId;
    String query = req.getParameter("q");
    if (Strings.isNullOrEmpty(query)) {
      try {
        locId = Long.parseLong(locationId);
      } catch (Exception e) {
        resp.setStatus(404);
        return;
      }
      location = locationDAO.findById(locId);
    } else {
      location = geoLocator.locate(query, GeolocationGranularity.NARROW);
      if (location != null && location.isResolved()) {
        String date = req.getParameter("date"), dateString="";
        if (!Strings.isNullOrEmpty(date)) {
          dateString = "?date=" + date;
        }
        resp.sendRedirect("/locations/" + location.getKey() + dateString);
        return;
      }
    }
    if (location == null || !location.isResolved()) {
      resp.setStatus(404);
      return;
    }
    final String timeRequest = req.getParameter("date");
    DateTime dateTime = null;
    String onDate = "";
    if (!Strings.isNullOrEmpty(timeRequest)) {
      try {
        dateTime = dateFormatter.parseDateTime(timeRequest);
        onDate = " on " + friendlyFormatter.print(dateTime);
      } catch (IllegalArgumentException ignored) {
      }
    }
    if (dateTime == null) {
      dateTime = clock.now();
    }
    req.setAttribute("stops", truckStopService.findStopsNearALocation(location, dateTime.toLocalDate()));
    req.setAttribute("thedate", dateTime);
    req.setAttribute("hasPopularityStats", staticConfig.showLocationGraphs() && location.isPopular());
    req.setAttribute("location", location);
    req.setAttribute("title", location.getName() + onDate);
    req.setAttribute("description", Strings.isNullOrEmpty(location.getDescription()) ? location.getName() : location.getDescription());
    String jsp = "/WEB-INF/jsp/location.jsp";
    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("containerType", "fixed");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
