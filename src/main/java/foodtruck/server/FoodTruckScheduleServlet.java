package foodtruck.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.model.TruckSchedule;
import foodtruck.truckstops.FoodTruckStopService;

/**
 * @author aviolette@gmail.com
 * @since 9/8/11
 */
@Singleton
public class FoodTruckScheduleServlet extends HttpServlet {
  private final FoodTruckStopService stopService;
  private final JsonWriter writer;
  private final DateTimeZone zone;

  @Inject
  public FoodTruckScheduleServlet(FoodTruckStopService stopService, JsonWriter writer, DateTimeZone zone) {
    this.stopService = stopService;
    this.writer = writer;
    this.zone = zone;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String truckId = req.getPathInfo().substring(1);
    LocalDate day = new LocalDate(zone);
    String value;
    try {
      TruckSchedule schedule = stopService.findStopsForDay(truckId, day);
      value = writer.writeSchedule(schedule).toString();
      resp.setHeader("Content-Type", "application/json");
      resp.setStatus(200);
    } catch (Exception e) {
      resp.setStatus(400);
      resp.setHeader("Content-Type", "text/plain");
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      value = writer.toString();
    }
    resp.getWriter().println(value);
  }

}
