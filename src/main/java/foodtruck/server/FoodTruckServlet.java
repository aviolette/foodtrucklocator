package foodtruck.server;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
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
import foodtruck.dao.ScheduleDAO;
import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.server.api.JsonWriter;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.TimeFormatter;

/**
 * Servlet that serves up the main food truck page.
 * @author aviolette
 * @since Jul 12, 2011
 */
@Singleton
public class FoodTruckServlet extends HttpServlet {
  private final DateTimeFormatter timeFormatter;
  private final Clock clock;
  private final DateTimeFormatter dateFormatter;
  private final FoodTruckStopService stopService;
  private ScheduleDAO scheduleCacher;
  private JsonWriter writer;
  private ConfigurationDAO configDAO;

  @Inject
  public FoodTruckServlet(ConfigurationDAO configDAO,
      Clock clock, FoodTruckStopService service, JsonWriter writer, ScheduleDAO scheduleCacher,
      @TimeFormatter DateTimeFormatter timeFormatter) {
    this.clock = clock;
    this.configDAO = configDAO;
    this.timeFormatter = timeFormatter;
    this.dateFormatter = DateTimeFormat.forPattern("EEE MMM dd, YYYY");
    this.stopService = service;
    this.writer = writer;
    this.scheduleCacher = scheduleCacher;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String path = req.getRequestURI();
    final String serverName = req.getServerName();
    if (serverName.contains("chifoodtruckz.com") ||
        serverName.contains("chicagofoodtrucklocator.appspot.com")) {
      resp.setStatus(301);
      resp.setHeader("Location", "http://www.chicagofoodtruckfinder.com" + path);
      return;
    }
    if (!Strings.isNullOrEmpty(path)) {
      req.setAttribute("showScheduleFor", path.substring(1));
    }
    final String timeRequest = req.getParameter("time");
    DateTime dateTime = null;
    if (!Strings.isNullOrEmpty(timeRequest)) {
      try {
        dateTime = timeFormatter.parseDateTime(timeRequest);
      } catch (IllegalArgumentException ignored) {
      }
    }
    if (dateTime == null) {
      dateTime = clock.now();
    }
    req.setAttribute("center", getCenter(req.getCookies(), configDAO.find().getCenter()));
    String googleAnalytics = System.getProperty("foodtruck.google.analytics", null);
    if (googleAnalytics != null) {
      req.setAttribute("google_analytics_ua", googleAnalytics);
    }

    String payload = scheduleCacher.findSchedule(dateTime.toLocalDate());
    if (payload == null) {
      DailySchedule schedule = stopService.findStopsForDay(dateTime.toLocalDate());
      try {
        payload = writer.writeSchedule(schedule).toString();
      } catch (JSONException e) {
        // TODO: fix this
        throw new RuntimeException(e);
      }
    }
    final String mode = req.getParameter("mode");
    req.setAttribute("mobile", "mobile".equals(mode));
    req.setAttribute("requestDate", dateFormatter.print(dateTime));
    req.setAttribute("requestTime", timeFormatter.print(dateTime));
    req.setAttribute("requestTimeInMillis", dateTime.getMillis());
    resp.setHeader("Cache-Control", "no-cache");
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
    req.setAttribute("payload", payload);
    req.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(req, resp);
  }

  private Location getCenter(@Nullable Cookie[] cookies, Location defaultValue) {
    double lat = 0, lng = 0;
    if (cookies == null) {
      return defaultValue;
    }
    for (int i = 0; i < cookies.length; i++) {
      if ("latitude".equals(cookies[i].getName())) {
        lat = Double.valueOf(cookies[i].getValue());
      } else if ("longitude".equals(cookies[i].getName())) {
        lng = Double.valueOf(cookies[i].getValue());
      }
    }
    if (lat != 0 && lng != 0) {
      return Location.builder().lat(lat).lng(lng).build();
    }
    return defaultValue;
  }
}
