package foodtruck.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.model.DailySchedule;
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
  private static final Logger log = Logger.getLogger(LocationServlet.class.getName());

  private final LocationDAO locationDAO;
  private final Clock clock;
  private final DateTimeFormatter dateFormatter;
  private final FoodTruckStopService truckStopService;
  private final DateTimeFormatter friendlyFormatter;

  @Inject
  public LocationServlet(LocationDAO locationDAO, Clock clock,
      @DateOnlyFormatter DateTimeFormatter dateFormatter,
      FoodTruckStopService truckStopService,
      @FriendlyDateOnlyFormat DateTimeFormatter friendlyFormatter, StaticConfig staticConfig) {
    super(staticConfig);
    this.locationDAO = locationDAO;
    this.clock = clock;
    this.dateFormatter = dateFormatter;
    this.truckStopService = truckStopService;
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
        if (locId == 0) {
          resp.setStatus(404);
          return;
        }
      } catch (Exception e) {
        resp.setStatus(404);
        return;
      }
      location = locationDAO.findById(locId);
    } else {
      location = locationDAO.findByAddress(query);
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
    boolean requestedTime = false;
    boolean showStops = true;
    if (!Strings.isNullOrEmpty(timeRequest)) {
      try {
        dateTime = dateFormatter.parseDateTime(timeRequest);
        onDate = " on " + friendlyFormatter.print(dateTime);
        requestedTime = true;
      } catch (IllegalArgumentException ignored) {
        log.warning("Invalidate date specified " + timeRequest);
        // lots of weird SQL stuff coming through in data parameter
        resp.setStatus(404);
        return;
      }
    } else {
      showStops = false;
      LocalDate startDate = clock.firstDayOfWeekFrom(clock.now());
      List<DailySchedule> truckStops = truckStopService.findStopsNearLocationOverRange(location,
          new Interval(startDate.toDateTimeAtStartOfDay(), startDate.plusDays(8).toDateTimeAtStartOfDay()));
      req.setAttribute("weeklyStops", truckStops);
    }
    if (dateTime == null) {
      dateTime = clock.now();
    }
    if (showStops) {
      req.setAttribute("stops", truckStopService.findStopsNearALocation(location, dateTime.toLocalDate()));
    }
    req.setAttribute("thedate", dateTime);
    req.setAttribute("requestedTime", requestedTime);
    req.setAttribute("tab", "location");
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
