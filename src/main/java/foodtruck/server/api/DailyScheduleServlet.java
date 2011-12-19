package foodtruck.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.LocalDate;

import foodtruck.dao.ScheduleDAO;
import foodtruck.model.DailySchedule;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * Returns the schedule for the current day.
 * @author aviolette@gmail.com
 * @since 12/7/11
 */
@Singleton
public class DailyScheduleServlet extends HttpServlet {

  private final FoodTruckStopService stopService;
  private final JsonWriter writer;
  private final Clock clock;
  private final ScheduleDAO scheduleCacher;

  @Inject
  public DailyScheduleServlet(FoodTruckStopService stopService, JsonWriter writer,
      Clock clock, ScheduleDAO scheduleCacher) {
    this.stopService = stopService;
    this.writer = writer;
    this.clock = clock;
    this.scheduleCacher = scheduleCacher;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    LocalDate day = clock.currentDay();
    String body;
    try {
      body = scheduleCacher.findSchedule(day);
      if (body == null) {
        DailySchedule schedule = stopService.findStopsForDay(day);
        body = writer.writeSchedule(schedule).toString();
      }
      resp.setHeader("Content-Type", "application/json");
      resp.setStatus(200);
    } catch (Exception e) {
      resp.setStatus(400);
      resp.setHeader("Content-Type", "text/plain");
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      body = writer.toString();
    }
    resp.getWriter().println(body);
  }
}
