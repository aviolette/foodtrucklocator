package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since 10/23/11
 */
@Singleton
public class TruckListServlet extends HttpServlet {
  private final FoodTruckStopService stopService;
  private final Clock clock;

  @Inject
  public TruckListServlet(FoodTruckStopService service, Clock clock) {
    this.stopService = service;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("nav", "trucks");
    req.setAttribute("tab", Objects.firstNonNull(req.getParameter("tab"), "home"));
    req.setAttribute("trucks", stopService.findCurrentAndPreviousStop(clock.currentDay()));
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/truckList.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);
  }
}
