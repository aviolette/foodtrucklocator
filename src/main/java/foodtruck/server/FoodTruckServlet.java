package foodtruck.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.model.Configuration;
import foodtruck.model.DailySchedule;
import foodtruck.model.StaticConfig;
import foodtruck.schedule.ScheduleCacher;
import foodtruck.server.resources.json.DailyScheduleWriter;
import foodtruck.server.resources.json.LocationCollectionWriter;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.TimeFormatter;

/**
 * Servlet that serves up the main food truck page.
 * @author aviolette
 * @since Jul 12, 2011
 */
@Singleton
public class FoodTruckServlet extends FrontPageServlet {
  private static final Logger log = Logger.getLogger(FoodTruckServlet.class.getName());
  private final DateTimeFormatter timeFormatter;
  private final Clock clock;
  private final DateTimeFormatter dateFormatter;
  private final FoodTruckStopService stopService;
  private final DailyScheduleWriter writer;
  private final ScheduleCacher scheduleCacher;
  private final LocationDAO locationDAO;
  private final LocationCollectionWriter locationCollectionWriter;

  @Inject
  public FoodTruckServlet(ConfigurationDAO configDAO,
      Clock clock, FoodTruckStopService service, DailyScheduleWriter writer, ScheduleCacher scheduleCacher,
      @TimeFormatter DateTimeFormatter timeFormatter, LocationDAO locationDAO,
      LocationCollectionWriter locationCollectionWriter, StaticConfig staticConfig) {
    super(configDAO, staticConfig);
    this.clock = clock;
    this.timeFormatter = timeFormatter;
    this.dateFormatter = DateTimeFormat.forPattern("EEE MMM dd, YYYY");
    this.stopService = service;
    this.writer = writer;
    this.scheduleCacher = scheduleCacher;
    this.locationDAO = locationDAO;
    this.locationCollectionWriter = locationCollectionWriter;
  }

  @Override
  protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String path = req.getRequestURI();
    final String serverName = req.getServerName();
    if (serverName.equals("chicagofoodtrucklocator.appspot.com")) {
      resp.setStatus(301);
      resp.setHeader("Location", "http://www.chicagofoodtruckfinder.com" + path);
      return;
    }
    if (!Strings.isNullOrEmpty(path)) {
      req.setAttribute("showScheduleFor", path.substring(1));
    }
    final String timeRequest = req.getParameter("time");
    DateTime dateTime = null;
    final boolean timeSpecified = !Strings.isNullOrEmpty(timeRequest);
    if (timeSpecified) {
      try {
        dateTime = timeFormatter.parseDateTime(timeRequest);
      } catch (IllegalArgumentException ignored) {
      }
    }
    if (dateTime == null) {
      dateTime = clock.now();
    }
    final Configuration configuration = configurationDAO.find();
    req.setAttribute("center", getCenter(req.getCookies()));
    String payload = timeSpecified ? null : scheduleCacher.findSchedule();
    if (payload == null || !configuration.isScheduleCachingOn()) {
      DailySchedule schedule = stopService.findStopsForDayAfter(dateTime);
      try {
        payload = writer.asJSON(schedule).toString();
        if (!timeSpecified) {
          scheduleCacher.saveSchedule(payload);
        }
      } catch (JSONException e) {
        // TODO: fix this
        throw new RuntimeException(e);
      }
      log.log(Level.INFO, "Loaded page from datastore");
    } else {
      log.log(Level.INFO, "Loaded payload from cache");
    }
    final boolean includeDesignatedStops = "true".equals(req.getParameter("designatedStops"));
    if (includeDesignatedStops) {
      req.setAttribute("designatedStops", locationCollectionWriter.asJSON(locationDAO.findDesignatedStops()).toString());
    } else {
      req.setAttribute("designatedStops", "[]");
    }
    final String mode = req.getParameter("mode");
    req.setAttribute("mobile", "mobile".equals(mode));
    req.setAttribute("mode", mode);
    req.setAttribute("requestDate", dateFormatter.print(dateTime));
    req.setAttribute("requestTime", timeFormatter.print(dateTime));
    req.setAttribute("requestTimeInMillis", dateTime.getMillis());
    req.setAttribute("tab", "map");
    req.setAttribute("appKey", configuration.getFrontDoorAppKey());
    req.setAttribute("description", "Find food trucks on the streets of Chicago by time and location.  Results are updated in real-time throughout the day.");
    resp.setHeader("Cache-Control", "no-cache");
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
    req.setAttribute("payload", payload);
    final String jsp = "/WEB-INF/jsp/index.jsp";
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
