package foodtruck.server.front;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.MenuDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.DailySchedule;
import foodtruck.model.Truck;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.server.CodedServletException;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2/18/17.
 */
@Singleton
public class TruckServlet extends HttpServlet {

  private static final String JSP = "/WEB-INF/jsp/truck.jsp";
  private final TruckDAO trucks;
  private final FoodTruckStopService stops;
  private final Clock clock;
  private final DateTimeZone zone;
  private final DailyDataDAO dailyDataDAO;
  private final MenuDAO menuDAO;

  @Inject
  public TruckServlet(TruckDAO trucks, FoodTruckStopService stops, Clock clock, DateTimeZone zone,
      DailyDataDAO dailyDataDAO, MenuDAO menuDAO) {
    this.trucks = trucks;
    this.stops = stops;
    this.clock = clock;
    this.zone = zone;
    this.dailyDataDAO = dailyDataDAO;
    this.menuDAO = menuDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String path[] = req.getRequestURI().split("/");
    String truckId = path[path.length - 1];
    Truck truck = trucks.findByIdOpt(truckId)
        .orElseThrow(() -> new CodedServletException(404, "Truck cannot be found: " + truckId));
    req.setAttribute("truck", truck);
    List<DailySchedule> schedules = getSchedules(truck, clock.currentDay());
    req.setAttribute("additionalCss", "/css/truck-page.css");
    req.setAttribute("stops", schedules);
    req.setAttribute("hasStops", schedules.stream().anyMatch(DailySchedule::isHasStops));
    req.setAttribute("title", truck.getName());
    req.setAttribute("menu", menuDAO.findByTruck(truckId));
    req.setAttribute("dailyData",
        dailyDataDAO.findByTruckAndDay(truck.getId(), clock.currentDay()));
    req.setAttribute("description",
        Strings.isNullOrEmpty(truck.getDescription()) ? truck.getName() : truck.getDescription());
    req = new GuiceHackRequestWrapper(req, JSP);
    req.setAttribute("tab", "trucks");
    req.getRequestDispatcher(JSP).forward(req, resp);
  }

  private List<DailySchedule> getSchedules(Truck truck, LocalDate firstDay) {
    return stops.findSchedules(truck.getId(),
        new Interval(firstDay.toDateTimeAtStartOfDay(zone), firstDay.toDateTimeAtStartOfDay(zone)
            .plusDays(60)));
  }
}
