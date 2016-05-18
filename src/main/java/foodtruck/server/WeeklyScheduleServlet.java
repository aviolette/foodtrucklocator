package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.StaticConfig;
import foodtruck.model.WeeklySchedule;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.DateOnlyFormatter;

/**
 * @author aviolette
 * @since 5/6/13
 */
@Singleton
public class WeeklyScheduleServlet extends FrontPageServlet {
  private final FoodTruckStopService stopService;
  private final Clock clock;
  private final DateTimeFormatter formatter;

  @Inject
  public WeeklyScheduleServlet(FoodTruckStopService stopService, Clock clock,
      @DateOnlyFormatter DateTimeFormatter formatter, StaticConfig staticConfig) {
    super(staticConfig);
    this.stopService = stopService;
    this.clock = clock;
    this.formatter = formatter;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String dateString = req.getParameter("date");
    DateTime theDate;
    if (!Strings.isNullOrEmpty(dateString)) {
      theDate = formatter.parseDateTime(dateString);
    } else {
      theDate = clock.now();
    }
    final LocalDate startDate = clock.firstDayOfWeekFrom(theDate), currentFirstDay = clock.firstDayOfWeek();
    LocalDate prev = theDate.toLocalDate().minusDays(7);
    LocalDate next = theDate.toLocalDate().plusDays(7);
    req.setAttribute("next", formatter.print(next.toDateTimeAtStartOfDay()));
    req.setAttribute("prev", formatter.print(prev.toDateTimeAtStartOfDay()));
    WeeklySchedule schedule = stopService.findPopularStopsForWeek(startDate);
    String jsp = "/WEB-INF/jsp/weekly.jsp";
    req.setAttribute("weeklySchedule", schedule.sortFrom(getCenter(req.getCookies())));
    req.setAttribute("theDate", theDate);
    req.setAttribute("weekOf", startDate);
    req.setAttribute("title", "Weekly Schedule");
    req.setAttribute("description", "Weekly schedule of the most-popular food truck stops in chicago");
    req.setAttribute("tab", "location");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
