package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.Location;

/**
 * Servlet that serves up the main food truck page.
 * @author aviolette
 * @since Jul 12, 2011
 */
@Singleton
public class FoodTruckServlet extends HttpServlet {
  private final DateTimeZone zone;
  private final Location mapCenter;
  private final DateTimeFormatter timeFormatter;

  @Inject
  public FoodTruckServlet( DateTimeZone zone, @Named("center") Location centerLocation) {
    this.zone = zone;
    this.mapCenter = centerLocation;
    this.timeFormatter = DateTimeFormat.forPattern("YYYYMMdd-HHmm").withZone(zone);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String path = req.getRequestURI();
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
      dateTime = new DateTime(zone);
      // peg the time at something that actually returns results
      if (dateTime.toLocalTime().isBefore(new LocalTime(11, 30))) {
        dateTime = dateTime.withTime(11, 30, 0, 0);
      }
    }
    req.setAttribute("center", mapCenter);
    String googleAnalytics = System.getProperty("foodtruck.google.analytics", null);
    if (googleAnalytics != null) {
      req.setAttribute("google_analytics_ua", googleAnalytics);
    }
    req.setAttribute("requestTime", timeFormatter.print(dateTime));
    req.setAttribute("requestTimeInMillis", dateTime.getMillis());
    req.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(req, resp);
  }
}
