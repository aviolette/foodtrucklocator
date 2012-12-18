package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
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

  @Inject
  public TruckStopServlet(TruckDAO truckDAO, TruckStopDAO truckStopDAO, Clock clock, ConfigurationDAO configDAO,
      @TimeOnlyFormatter DateTimeFormatter timeFormatter, GeoLocator geolocator) {
    this.truckDAO = truckDAO;
    this.truckStopDAO = truckStopDAO;
    this.clock = clock;
    this.configDAO = configDAO;
    this.timeFormatter = timeFormatter;
    this.geolocator = geolocator;
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
      truckStop = new TruckStop(truck, start, end, configDAO.find().getCenter(),  null, false);
    }
    req.setAttribute("nav", "trucks");
    req.setAttribute("truckStop", truckStop);
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
    DateTime startTime = parseTime(req.getParameter("startTime"));
    DateTime endTime = parseTime(req.getParameter("endTime"));
    Location location = geolocator.locate(req.getParameter("location"), GeolocationGranularity.NARROW);
    long locationId = ModelEntity.UNINITIALIZED;
    if (req.getParameter("entityId") != null) {
      locationId = Long.parseLong(req.getParameter("entityId"));
    }
    boolean locked = false;
    TruckStop stop = new TruckStop(truck, startTime, endTime, location, locationId, locked);
    truckStopDAO.save(stop);
    resp.sendRedirect("/admin/trucks/" + truckID);
  }

  private DateTime parseTime(String time) {
    DateTime dt = timeFormatter.parseDateTime(time.trim().toLowerCase());
    DateTimeZone zone = dt.getZone();
    return clock.currentDay().toDateTime(dt.toLocalTime(), zone);
  }
}