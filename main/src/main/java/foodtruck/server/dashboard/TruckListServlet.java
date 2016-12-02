package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
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
    req.setAttribute("trucks", stopService.findCurrentAndPreviousStop(clock.currentDay()));
    if (tab.equals("inactiveTrucks")) {
      req.setAttribute("inactiveTrucks", truckDAO.findInactiveTrucks());
    }
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/truckList.jsp").forward(req, resp);
  }
}
