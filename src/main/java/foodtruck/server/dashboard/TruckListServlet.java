package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.model.Trucks;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since 10/23/11
 */
@Singleton
public class TruckListServlet extends HttpServlet {
  private final Trucks trucks;
  private final FoodTruckStopService stopService;
  private final Clock clock;

  @Inject
  public TruckListServlet(Trucks trucks, FoodTruckStopService service, Clock clock) {
    this.trucks = trucks;
    this.stopService = service;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("nav", "trucks");
    req.setAttribute("trucks", stopService.findCurrentAndPreviousStop(clock.currentDay()));
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/truckList.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);
  }
}
