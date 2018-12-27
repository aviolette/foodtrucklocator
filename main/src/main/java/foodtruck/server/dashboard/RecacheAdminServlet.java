package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2018-12-11
 */
@Singleton
public class RecacheAdminServlet extends HttpServlet {

  private final FoodTruckStopService service;
  private final Clock clock;

  @Inject
  public RecacheAdminServlet(FoodTruckStopService service, Clock clock) {
    this.service = service;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/recache.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LocalDate when = clock.currentDay();
    Interval interval = clock.currentDay().toInterval(clock.zone()).withEnd(when.plusDays(365).toDateTimeAtStartOfDay(clock.zone()));
    service.pullCustomCalendars(interval);
    resp.sendRedirect("/admin/recache");
  }
}
