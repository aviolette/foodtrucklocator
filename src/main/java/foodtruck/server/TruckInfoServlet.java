package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

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
    }
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

}
