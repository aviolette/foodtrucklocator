package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.ModelEntity;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.util.Clock;
import foodtruck.util.TimeOnlyFormatter;

/**
 * @author aviolette
 * @since 11/26/12
 */
@Singleton
public class TruckStopServlet extends HttpServlet {
  private final TruckStopDAO truckStopDAO;
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final ConfigurationDAO configDAO;
  private final DateTimeFormatter timeFormatter;
  private final GeoLocator geolocator;
  private final LocationDAO locationDAO;

  @Inject
  public TruckStopServlet(TruckDAO truckDAO, TruckStopDAO truckStopDAO, Clock clock,
      ConfigurationDAO configDAO, LocationDAO locationDAO,
      @TimeOnlyFormatter DateTimeFormatter timeFormatter, GeoLocator geolocator) {
    this.truckDAO = truckDAO;
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
    this.configDAO = configDAO;
    this.timeFormatter = timeFormatter;
    this.geolocator = geolocator;
    this.locationDAO = locationDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String truckId = getTruckId(req);
    String eventId = req.getParameter("event");
    Truck truck = truckDAO.findById(truckId);
    TruckStop truckStop;
    if (!Strings.isNullOrEmpty(eventId)) {
      truckStop = truckStopDAO.findById(Long.parseLong(eventId));
      // TODO: 404 if null
    } else {
      DateTime start = clock.now();
      if (start.toLocalTime().isBefore(new LocalTime(11, 30))) {
        start = start.withTime(11, 30, 0, 0);
      }
      DateTime end = start.plusHours(2);
      truckStop = TruckStop.builder().truck(truck).startTime(start).endTime(end).location(configDAO.find().getCenter()).build();
    }
    req.setAttribute("nav", "trucks");
    req.setAttribute("truckStop", truckStop);
    List<String> locationNames = ImmutableList.copyOf(
        Iterables.transform(locationDAO.findAutocompleteLocations(), Location.TO_NAME));
    req.setAttribute("locations", new JSONArray(locationNames).toString());
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/event.jsp").forward(req, resp);
  }

  private String getTruckId(HttpServletRequest req) {
    String requestURI = req.getRequestURI();
    String truckId = requestURI.substring(14);
    truckId = truckId.substring(0, truckId.indexOf('/'));
    return truckId;
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String truckID = getTruckId(req);
    Truck truck = truckDAO.findById(truckID);
    DateTime startTime = parseTime(req.getParameter("startTime"), null);
    DateTime endTime = parseTime(req.getParameter("endTime"), startTime);
    Location location = geolocator.locate(req.getParameter("location"), GeolocationGranularity.NARROW);
    long locationId = ModelEntity.UNINITIALIZED;
    if (req.getParameter("entityId") != null) {
      locationId = Long.parseLong(req.getParameter("entityId"));
    }
    boolean locked = false;
    TruckStop stop = TruckStop.builder().truck(truck).startTime(startTime).endTime(endTime)
        .location(location).key(locationId).locked(locked).build();
    truckStopDAO.save(stop);
    resp.sendRedirect("/admin/trucks/" + truckID);
  }

  private DateTime parseTime(String time, @Nullable DateTime context) {
    DateTime dt = timeFormatter.parseDateTime(time.trim().toLowerCase());
    DateTimeZone zone = dt.getZone();
    DateTime theTime = clock.currentDay().toDateTime(dt.toLocalTime(), zone);
    if (context == null) {
      return theTime;
    }
    return theTime.isBefore(context) ? theTime.plusDays(1) : theTime;
  }
}
