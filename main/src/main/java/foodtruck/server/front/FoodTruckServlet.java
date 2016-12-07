package foodtruck.server.front;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.schedule.ScheduleCacher;
import foodtruck.server.resources.json.DailyScheduleWriter;
import foodtruck.time.Clock;
import foodtruck.time.TimeFormatter;

/**
 * Servlet that serves up the main food truck page.
 * @author aviolette
 * @since Jul 12, 2011
 */
@Singleton
public class FoodTruckServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(FoodTruckServlet.class.getName());
  private final DateTimeFormatter timeFormatter;
  private final Clock clock;
  private final DateTimeFormatter dateFormatter;
  private final FoodTruckStopService stopService;
  private final DailyScheduleWriter writer;
  private final ScheduleCacher scheduleCacher;
  private final StaticConfig staticConfig;

  @Inject
  public FoodTruckServlet(Clock clock, FoodTruckStopService service, DailyScheduleWriter writer,
      ScheduleCacher scheduleCacher, @TimeFormatter DateTimeFormatter timeFormatter, StaticConfig staticConfig) {
    this.clock = clock;
    this.timeFormatter = timeFormatter;
    this.dateFormatter = DateTimeFormat.forPattern("EEE MMM dd, YYYY");
    this.stopService = service;
    this.writer = writer;
    this.scheduleCacher = scheduleCacher;
    this.staticConfig = staticConfig;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
    req.setAttribute("center", getCenter(req.getCookies()));
    String payload = timeSpecified ? null : scheduleCacher.findSchedule();
    payload = getSchedule(dateTime, timeSpecified, payload);
    final String mode = req.getParameter("mode");
    req.setAttribute("mobile", "mobile".equals(mode));
    req.setAttribute("mode", mode);
    req.setAttribute("requestDate", dateFormatter.print(dateTime));
    req.setAttribute("requestTime", timeFormatter.print(dateTime));
    req.setAttribute("requestTimeInMillis", dateTime.getMillis());
    req.setAttribute("tab", "map");
    req.setAttribute("appKey", staticConfig.getFrontDoorAppKey());
    req.setAttribute("defaultCity", staticConfig.getCityState());
    req.setAttribute("description", "Find food trucks on the streets of " + staticConfig.getCity() +
        " by time and location.  Results are updated in real-time throughout the day.");
    resp.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    resp.setHeader(HttpHeaders.PRAGMA, "no-cache");
    resp.setHeader(HttpHeaders.EXPIRES, "Thu, 01 Jan 1970 00:00:00 GMT");
    req.setAttribute("payload", payload);
    String jsp = "/WEB-INF/jsp/index.jsp";
    req.getRequestDispatcher(jsp)
        .forward(req, resp);
  }


  Location getCenter(@Nullable Cookie[] cookies) {
    double lat = 0, lng = 0;
    if (cookies == null) {
      return staticConfig.getCenter();
    }
    for (Cookie cooky : cookies) {
      if ("latitude".equals(cooky.getName())) {
        lat = Double.valueOf(cooky.getValue());
      } else if ("longitude".equals(cooky.getName())) {
        lng = Double.valueOf(cooky.getValue());
      }
    }
    if (lat != 0 && lng != 0) {
      return Location.builder()
          .lat(lat)
          .lng(lng)
          .build();
    }
    return staticConfig.getCenter();
  }


  private String getSchedule(DateTime dateTime, boolean timeSpecified, String payload) {
    if (payload == null || !staticConfig.isScheduleCachingOn()) {
      DailySchedule schedule = stopService.findStopsForDay(dateTime.toLocalDate());
      try {
        payload = writer.asJSON(schedule)
            .toString();
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
    return payload;
  }
}
