package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.TruckStatus;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;

/**
 * @author aviolette@gmail.com
 * @since 10/23/11
 */
@Singleton
public class TruckListServlet extends HttpServlet {
  private final FoodTruckStopService stopService;
  private final Clock clock;
  private final TruckDAO truckDAO;

  @Inject
  public TruckListServlet(FoodTruckStopService service, Clock clock, TruckDAO truckDAO) {
    this.stopService = service;
    this.clock = clock;
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("nav", "trucks");
    String tab = MoreObjects.firstNonNull(req.getParameter("tab"), "home");
    req.setAttribute("tab", tab);
    List<TruckStatus> currentAndPreviousStop = stopService.findCurrentAndPreviousStop(clock.currentDay());
    int activeStops = currentAndPreviousStop.stream()
        .mapToInt(TruckStatus::getTotalStops)
        .sum();
    int totalTrucks = currentAndPreviousStop.stream()
        .mapToInt(status -> status.isActive() ? 1 : 0)
        .sum();
    req.setAttribute("totalTrucks", totalTrucks);
    req.setAttribute("activeStops", activeStops);
    req.setAttribute("trucks", currentAndPreviousStop);
    if (tab.equals("inactiveTrucks")) {
      req.setAttribute("inactiveTrucks", truckDAO.findInactiveTrucks());
    }
    req.setAttribute("extraScripts", ImmutableList.of("/script/dashboard-trucks.js"));
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/trucks.jsp").forward(req, resp);
  }
}
