package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.calendar.Calendar;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.HtmlDateFormatter;

/**
 * @author aviolette
 * @since 1/8/15
 */
@Singleton
public class CompoundEventServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(CompoundEventServlet.class.getName());
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/compoundEvent.jsp";
  private final TruckDAO truckDAO;
  private final LocationDAO locationDAO;
  private final DateTimeFormatter timeFormatter;
  private final Clock clock;
  private final Provider<Calendar> calendarProvider;
  private final ConfigurationDAO configDAO;
  private final FoodTruckStopService service;

  @Inject
  public CompoundEventServlet(TruckDAO truckDAO, LocationDAO locationDAO, ConfigurationDAO configDAO,
      @HtmlDateFormatter DateTimeFormatter formatter, Clock clock, Provider<Calendar> calendarProvider,
      FoodTruckStopService service) {
    this.truckDAO = truckDAO;
    this.locationDAO = locationDAO;
    this.timeFormatter = formatter;
    this.clock = clock;
    this.service = service;
    this.calendarProvider = calendarProvider;
    this.configDAO = configDAO;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String startTimeParam = req.getParameter("startTime"),
        endTimeParam = req.getParameter("endTime"),
        locationID = req.getRequestURI().substring(req.getRequestURI().lastIndexOf('/') + 1);
    String[] trucks = req.getParameterValues("trucks");
    Location location = locationDAO.findById(Long.valueOf(locationID));
    if (location == null) {
      resp.sendError(404);
      return;
    }
    DateTime startTime = timeFormatter.parseDateTime(startTimeParam),
        endTime = timeFormatter.parseDateTime(endTimeParam);
    Calendar calendar = calendarProvider.get();
    String calendarID = configDAO.find().getGoogleCalendarAddress();
    for (String truckId : trucks) {
      TruckStop stop = TruckStop.builder()
          .location(location)
          .startTime(startTime)
          .endTime(endTime)
          .truck(truckDAO.findById(truckId))
          .build();
      service.saveWithBackingStop(stop, calendar, calendarID);
    }
    resp.sendRedirect("/admin/trucks");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String locationID = req.getRequestURI().substring(req.getRequestURI().lastIndexOf('/') + 1);
    Location location = locationDAO.findById(Long.valueOf(locationID));
    if (location == null) {
      resp.sendError(404);
      return;
    }
    req.setAttribute("startTime", timeFormatter.print(clock.now()));
    req.setAttribute("endTime", timeFormatter.print(clock.now().plusHours(2)));
    req.setAttribute("location", location);
    req.setAttribute("trucks", truckDAO.findActiveTrucks());
    req.setAttribute("nav", "location");
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }
}
