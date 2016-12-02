package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.time.Clock;
import foodtruck.time.HtmlDateFormatter;
import foodtruck.util.Link;

/**
 * @author aviolette
 * @since 11/26/12
 */
@Singleton
public class TruckStopServlet extends HttpServlet {
  private final TruckStopDAO truckStopDAO;
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final DateTimeFormatter timeFormatter;
  private final GeoLocator geoLocator;
  private final FoodTruckStopService stopService;
  private final LocationDAO locationDAO;
  private final Provider<UserService> userServiceProvider;

  @Inject
  public TruckStopServlet(TruckDAO truckDAO, TruckStopDAO truckStopDAO, Clock clock,
      GeoLocator geoLocator, FoodTruckStopService stopService, Provider<UserService> userServiceProvider,
      @HtmlDateFormatter DateTimeFormatter timeFormatter, LocationDAO locationDAO) {
    this.truckDAO = truckDAO;
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
    this.timeFormatter = timeFormatter;
    this.geoLocator = geoLocator;
    this.stopService = stopService;
    this.locationDAO = locationDAO;
    this.userServiceProvider = userServiceProvider;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    String truckId = requestURI.substring(14);
    if (Strings.isNullOrEmpty(truckId)) {
      resp.sendRedirect("/trucks");
      return;
    }
    int index = truckId.indexOf("/stops/");
    String actualTruckId = truckId.substring(0, index);
    String stopId = truckId.substring(index+7);
    final String jsp = "/WEB-INF/jsp/dashboard/editStop.jsp";
    req = new GuiceHackRequestWrapper(req, jsp);
    final Truck truck = truckDAO.findById(actualTruckId);
    DateTime startTime, endTime;
    String title, locationName = "";
    boolean locked = false;
    if ("new".equals(stopId)) {
      if (truck.getCategories().contains("Breakfast") || clock.now().getHourOfDay() > 11) {
        startTime = clock.now();
      } else {
        startTime = clock.now().withTime(11, 0, 0, 0);
      }
      endTime = startTime.plusHours(3);
      endTime = endTime.withMinuteOfHour(0);
      title = "New Stop";
    } else {
      TruckStop stop = stopService.findById(Long.parseLong(stopId));
      if (stop == null) {
        resp.sendError(404, "Stop could not be found");
        return;
      }
      locked = stop.isLocked();
      startTime = stop.getStartTime();
      endTime = stop.getEndTime();
      title = stop.getLocation().getName();
      locationName = title;
    }
    req.setAttribute("startTime", timeFormatter.print(startTime));
    req.setAttribute("endTime", timeFormatter.print(endTime));
    req.setAttribute("truck", truck);
    req.setAttribute("stopId", stopId);
    req.setAttribute("nav", "trucks");
    req.setAttribute("locked", locked);
    req.setAttribute("backUrl", "/admin/trucks/" + truckId);
    req.setAttribute("locationName", locationName);
    req.setAttribute("locations", locationNamesAsJsonArray());
    req.setAttribute("breadcrumbs", ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(truck.getName(), "/admin/trucks/" + actualTruckId),
        new Link(title, "/admin/trucks/" + actualTruckId + "/stops/" + stopId)));
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String stopId = req.getParameter("stopId");
    String truckId = req.getParameter("truckId");
    Truck truck = truckDAO.findById(truckId);
    TruckStop.Builder builder;
    if (!stopId.equalsIgnoreCase("new")) {
      try {
        TruckStop actual = truckStopDAO.findById(Long.parseLong(stopId));
        if (actual == null) {
          resp.sendError(404, "Stop could not be found");
          return;
        } else {
          builder = TruckStop.builder(actual);
        }
      } catch (NumberFormatException nfe) {
        resp.sendError(400, "Invalid stop ID specified");
        return;
      }
    } else {
      builder = TruckStop.builder().origin(StopOrigin.MANUAL);
    }
    builder.truck(truck);
    DateTime startTime = timeFormatter.parseDateTime(req.getParameter("startTime"));
    DateTime endTime = timeFormatter.parseDateTime(req.getParameter("endTime"));
    if (!endTime.isAfter(startTime)) {
      resp.sendError(400, "End time is not after start time.");
      return;
    }
    Location location = geoLocator.locate(req.getParameter("location"), GeolocationGranularity.BROAD);
    if (location == null || !location.isResolved()) {
      resp.sendError(400, "Location is not resolved.");
      return;
    }
    builder.startTime(startTime).endTime(endTime).locked("on".equals(req.getParameter("lockStop"))).location(location);
    UserService userService = userServiceProvider.get();
    stopService.update(builder.build(), userService.getCurrentUser().getEmail());
    String uri = req.getRequestURI().substring(0, req.getRequestURI().indexOf("/stops/"));
    resp.sendRedirect(uri);
  }

  private String locationNamesAsJsonArray() {
    List<String> locationNames = ImmutableList.copyOf(
        Iterables.transform(locationDAO.findAutocompleteLocations(), Location.TO_NAME));
    return new JSONArray(locationNames).toString();
  }
}
