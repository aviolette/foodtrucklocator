package foodtruck.server.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;

/**
 * @author aviolette@gmail.com
 * @since 1/24/12
 */
@Singleton
public class AdminTruckStopServlet extends HttpServlet {
  private final FoodTruckStopService stopService;

  @Inject
  public AdminTruckStopServlet(FoodTruckStopService stopService) {
    this.stopService = stopService;
  }

  @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    long stopId = Long.parseLong(requestURI.substring(requestURI.lastIndexOf("/") + 1));
    stopService.delete(stopId);
  }
}
