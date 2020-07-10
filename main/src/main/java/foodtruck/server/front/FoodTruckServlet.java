package foodtruck.server.front;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.annotations.DefaultCityState;
import foodtruck.annotations.FrontDoorAppKey;
import foodtruck.annotations.MapCenter;
import foodtruck.model.Location;
import foodtruck.monitoring.CounterPublisher;
import foodtruck.schedule.ScheduleCacher;
import foodtruck.time.Clock;

/**
 * Servlet that serves up the main food truck page.
 *
 * @author aviolette
 * @since Jul 12, 2011
 */
@Singleton
public class FoodTruckServlet extends HttpServlet {

  private final Clock clock;
  private final ScheduleCacher scheduleCacher;
  private final Location mapCenter;
  private final CounterPublisher publisher;
  private final String cityState;
  private final String appKey;

  @Inject
  public FoodTruckServlet(Clock clock, ScheduleCacher scheduleCacher, @MapCenter Location location, CounterPublisher publisher, @DefaultCityState String cityState,
      @FrontDoorAppKey String appKey) {
    this.clock = clock;
    this.scheduleCacher = scheduleCacher;
    this.mapCenter = location;
    this.publisher = publisher;
    this.cityState = cityState;
    this.appKey = appKey;
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
    req.setAttribute("additionalCss", "/css/front-page-1.3.1.css");
    req.setAttribute("center", mapCenter);
    final String mode = req.getParameter("mode");
    req.setAttribute("mobile", "mobile".equals(mode));
    req.setAttribute("mode", mode);
    req.setAttribute("requestTimeInMillis", clock.now()
        .getMillis());
    req.setAttribute("tab", "map");
    req.setAttribute("suffix", "");

    publisher.increment("daily_schedule_request", 1, clock.nowInMillis(), ImmutableMap.of("APP_KEY", appKey));
    req.setAttribute("appKey", appKey);
    req.setAttribute("defaultCity", cityState);
    req.setAttribute("description", "Find food trucks on the streets of Chicago" +
        " by time and location.  Results are updated in real-time throughout the day.");
    resp.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    resp.setHeader(HttpHeaders.PRAGMA, "no-cache");
    resp.setHeader(HttpHeaders.EXPIRES, "Thu, 01 Jan 1970 00:00:00 GMT");
    req.setAttribute("payload", scheduleCacher.findSchedule());
    req.getRequestDispatcher("/WEB-INF/jsp/index.jsp")
        .forward(req, resp);
  }
}
