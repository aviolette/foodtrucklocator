package foodtruck.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ScheduleDAO;
import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.server.api.JsonWriter;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * Servlet that serves up the main food truck page.
 * @author aviolette
 * @since Jul 12, 2011
 */
@Singleton
public class FoodTruckServlet extends HttpServlet {
  private final Location mapCenter;
  private final DateTimeFormatter timeFormatter;
  private final Clock clock;
  private final DateTimeFormatter dateFormatter;
  private static final Logger log = Logger.getLogger(FoodTruckServlet.class.getName());
  private final FoodTruckStopService stopService;
  private ScheduleDAO scheduleCacher;
  private JsonWriter writer;

  @Inject
  public FoodTruckServlet(DateTimeZone zone, @Named("center") Location centerLocation,
      Clock clock, FoodTruckStopService service, JsonWriter writer, ScheduleDAO scheduleCacher) {
    this.clock = clock;
    this.mapCenter = centerLocation;
    this.timeFormatter = DateTimeFormat.forPattern("YYYYMMdd-HHmm").withZone(zone);
    this.dateFormatter = DateTimeFormat.forPattern("EEE MMM dd, YYYY");
    this.stopService = service;
    this.writer = writer;
    this.scheduleCacher = scheduleCacher;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String path = req.getRequestURI();
    if (req.getServerName().contains("chifoodtruckz.com")) {
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
    req.setAttribute("center", getCenter(req.getCookies(), mapCenter));
    String googleAnalytics = System.getProperty("foodtruck.google.analytics", null);
    if (googleAnalytics != null) {
      req.setAttribute("google_analytics_ua", googleAnalytics);
    }

    String payload = scheduleCacher.findSchedule(clock.currentDay());
    if (payload == null) {
      DailySchedule schedule = stopService.findStopsForDay(clock.currentDay());
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
    resp.setHeader("Cache-Control", "max-age=900");
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
