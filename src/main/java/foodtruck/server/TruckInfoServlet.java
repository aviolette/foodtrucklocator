package foodtruck.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckDAO;
import foodtruck.model.DailySchedule;
import foodtruck.model.DayOfWeek;
import foodtruck.model.Truck;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.MoreStrings;

/**
 * @author aviolette@gmail.com
 * @since 6/29/12
 */
@Singleton
public class TruckInfoServlet extends HttpServlet {
  private final TruckDAO truckDAO;
  private final FoodTruckStopService truckStopService;
  private final Clock clock;

  @Inject
  public TruckInfoServlet(TruckDAO truckDAO, FoodTruckStopService truckService, Clock clock) {
    this.truckDAO = truckDAO;
    this.truckStopService = truckService;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String jsp = "/WEB-INF/jsp/trucks.jsp";
    final String path = req.getRequestURI();
    final String keyIndex = path.substring(path.lastIndexOf("/") + 1);
    boolean truckList = true;
    if (!Strings.isNullOrEmpty(keyIndex) && !keyIndex.startsWith("trucks")) {
      jsp = "/WEB-INF/jsp/truckView.jsp";
      truckList = false;
    }
    req = new GuiceHackRequestWrapper(req, jsp);
    if (truckList) {
      req.setAttribute("trucks", truckDAO.findActiveTrucks());
    } else {
      Truck truck = truckDAO.findById(keyIndex);
      if (truck == null) {
        resp.sendError(404);
        return;
      }
      req.setAttribute("truck", truck);
      DateTime current = clock.now();
      int dayOfWeek = current.getDayOfWeek();
      final DateTime mondayPrior = current.minusDays(6 + dayOfWeek);
      final DateTime nextSunday = current.plusDays(7-dayOfWeek+1);
      List<DailySchedule> schedules = truckStopService.findSchedules(truck.getId(),
          mondayPrior, nextSunday);
      PriorAndCurrentSchedule schedule[] = new PriorAndCurrentSchedule[7];
      final DateTime mondayCurrent = current.minusDays(dayOfWeek - 1);
      for (int day=0; day < 7; day++) {
        schedule[day] = findBoth(mondayCurrent.plusDays(day).toLocalDate(),
            mondayPrior.plusDays(day).toLocalDate(), schedules, day);
      }
      req.setAttribute("schedule", Arrays.asList(schedule));
    }
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  private PriorAndCurrentSchedule findBoth(LocalDate current, LocalDate prior,
      List<DailySchedule> schedules, int day) {
    DailySchedule currentSchedule = null, priorSchedule = null;
    for (DailySchedule schedule : schedules) {
      if (current.equals(schedule.getDay())) {
        currentSchedule = schedule;
      } else if (prior.equals(schedule.getDay())) {
        priorSchedule = schedule;
      }
    }
    return new PriorAndCurrentSchedule(currentSchedule, priorSchedule, DayOfWeek.fromConstant(day));
  }

  public static class PriorAndCurrentSchedule {
    private final DailySchedule currentSchedule;
    private final DailySchedule priorSchedule;
    private final DayOfWeek dayOfWeek;

    public PriorAndCurrentSchedule(DailySchedule currentSchedule, DailySchedule priorSchedule,
        DayOfWeek dayOfWeek) {
      this.currentSchedule = currentSchedule;
      this.priorSchedule = priorSchedule;
      this.dayOfWeek = dayOfWeek;
    }

    public DailySchedule getCurrent() {
      return currentSchedule;
    }

    public DailySchedule getPrior() {
      return priorSchedule;
    }

    public String getName() {
      return MoreStrings.capitalize(dayOfWeek.toString());
    }
  }
}
