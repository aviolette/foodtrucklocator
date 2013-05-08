package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.WeeklySchedule;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 5/6/13
 */
@Singleton
public class WeeklyScheduleServlet extends FrontPageServlet {
  private final FoodTruckStopService stopService;
  private final Clock clock;

  @Inject
  public WeeklyScheduleServlet(FoodTruckStopService stopService, Clock clock, ConfigurationDAO configDAO) {
    super(configDAO);
    this.stopService = stopService;
    this.clock = clock;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    WeeklySchedule schedule = stopService.findPopularStopsForWeek(clock.firstDayOfWeek());
    String jsp = "/WEB-INF/jsp/weekly.jsp";
    req.setAttribute("weeklySchedule", schedule.sortFrom(getCenter(req.getCookies())));
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
