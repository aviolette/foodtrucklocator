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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.Location;
import foodtruck.truckstops.FoodTruckStopService;

/**
 * Servlet that serves up the main food truck page.
 * @author aviolette
 * @since Jul 12, 2011
 */
@Singleton
public class FoodTruckServlet extends HttpServlet {
  private final FoodTruckStopService foodTruckService;
  private final DateTimeZone zone;
  private final Location mapCenter;
  private final DateTimeFormatter timeFormatter;

  @Inject
  public FoodTruckServlet(FoodTruckStopService foodTruckService, DateTimeZone zone,
      @Named("center") Location centerLocation) {
    this.foodTruckService = foodTruckService;
    this.zone = zone;
    this.mapCenter = centerLocation;
    this.timeFormatter = DateTimeFormat.forPattern("YYYYMMdd-HHmm").withZone(zone);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
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
    }
    req.setAttribute("trucks", foodTruckService.findFoodTruckGroups(dateTime));
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
